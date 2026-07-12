package ingprompt.patricia.notification.application.service;

import ingprompt.patricia.notification.application.port.out.GlobalNotificationRepositoryOutPort;
import ingprompt.patricia.notification.application.port.out.GlobalReadMarkerRepositoryOutPort;
import ingprompt.patricia.notification.application.port.out.MobilePushPort;
import ingprompt.patricia.notification.application.port.out.NotificationPushPort;
import ingprompt.patricia.notification.application.port.out.UnreadCounterOutPort;
import ingprompt.patricia.notification.application.port.out.UserNotificationRepositoryOutPort;
import ingprompt.patricia.notification.domain.enums.NotificationScope;
import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.domain.model.GlobalNotification;
import ingprompt.patricia.notification.domain.model.NotificationView;
import ingprompt.patricia.notification.domain.model.UserNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private UserNotificationRepositoryOutPort userRepository;
    @Mock
    private GlobalNotificationRepositoryOutPort globalRepository;
    @Mock
    private GlobalReadMarkerRepositoryOutPort readMarkerRepository;
    @Mock
    private UnreadCounterOutPort unreadCounter;
    @Mock
    private NotificationPushPort webPush;
    @Mock
    private MobilePushPort mobilePush;

    private NotificationService service;

    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new NotificationService(userRepository, globalRepository, readMarkerRepository,
                unreadCounter, webPush, mobilePush, 90L, 30);
        userId = UUID.randomUUID();
    }

    // ---- notifyUser ----

    @Test
    void notifyUser_new_savesIncrementsAndPushesBothChannels() {
        when(userRepository.existsBySourceEventAndRecipient("evt-1", userId)).thenReturn(false);

        service.notifyUser(userId, NotificationType.NEW_MATCH_REQUEST, "msg", Map.of("requesterId", "x"), "evt-1");

        verify(userRepository).save(any(UserNotification.class));
        verify(unreadCounter).incrementIfPresent(userId);
        verify(webPush).pushToUser(eq(userId), any(NotificationView.class));
        verify(mobilePush).pushToUser(eq(userId), any(NotificationView.class));
    }

    @Test
    void notifyUser_duplicate_isSkipped() {
        when(userRepository.existsBySourceEventAndRecipient("evt-1", userId)).thenReturn(true);

        service.notifyUser(userId, NotificationType.NEW_MATCH_REQUEST, "msg", Map.of(), "evt-1");

        verify(userRepository, never()).save(any());
        verify(webPush, never()).pushToUser(any(), any());
        verify(mobilePush, never()).pushToUser(any(), any());
    }

    // ---- notifyUsers ----

    @Test
    void notifyUsers_fansOutToEachRecipient() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        when(userRepository.existsBySourceEventAndRecipient(any(), any())).thenReturn(false);

        service.notifyUsers(Set.of(a, b), NotificationType.NEW_EVENT_IN_PARCHE, "msg", Map.of(), "evt-2");

        verify(userRepository, times(2)).save(any(UserNotification.class));
        verify(webPush).pushToUser(eq(a), any());
        verify(webPush).pushToUser(eq(b), any());
    }

    @Test
    void notifyUsers_null_isNoOp() {
        service.notifyUsers(null, NotificationType.NEW_EVENT_IN_PARCHE, "msg", Map.of(), "evt-2");
        verifyNoInteractions(userRepository, webPush, mobilePush);
    }

    // ---- notifyEveryone ----

    @Test
    void notifyEveryone_new_savesAndPushesWebOnly() {
        when(globalRepository.existsBySourceEvent("evt-3")).thenReturn(false);

        service.notifyEveryone(NotificationType.NEW_PUBLIC_PARCHE, "msg", Map.of("parcheId", "p"), "evt-3");

        verify(globalRepository).save(any(GlobalNotification.class));
        verify(webPush).pushToAll(any(NotificationView.class));
        verifyNoInteractions(mobilePush); // broadcasts are web-only (Expo has no topic)
    }

    @Test
    void notifyEveryone_duplicate_isSkipped() {
        when(globalRepository.existsBySourceEvent("evt-3")).thenReturn(true);

        service.notifyEveryone(NotificationType.NEW_PUBLIC_PARCHE, "msg", Map.of(), "evt-3");

        verify(globalRepository, never()).save(any());
        verify(webPush, never()).pushToAll(any());
    }

    // ---- getFeed ----

    @Test
    void getFeed_mergesTargetedAndGlobal_newestFirst_withGlobalUnread() {
        Instant now = Instant.now();
        UserNotification targeted = UserNotification.rehydrate(UUID.randomUUID(), userId,
                NotificationType.NEW_MATCH_REQUEST, "t", Map.of(), NotificationState.UNREAD,
                now, "s1", now.plusSeconds(3600));
        GlobalNotification global = GlobalNotification.rehydrate(UUID.randomUUID(),
                NotificationType.NEW_PUBLIC_PARCHE, "g", Map.of(), now.minusSeconds(60), "s2", now.plusSeconds(3600));

        when(readMarkerRepository.findLastReadAt(userId)).thenReturn(Optional.of(now.minusSeconds(120)));
        when(userRepository.findRecentByRecipient(userId, 30)).thenReturn(List.of(targeted));
        when(globalRepository.findRecent(30)).thenReturn(List.of(global));

        List<NotificationView> feed = service.getFeed(userId, 0); // 0 -> default limit 30

        assertThat(feed).hasSize(2);
        assertThat(feed.get(0).scope()).isEqualTo(NotificationScope.TARGETED);   // newest
        assertThat(feed.get(1).scope()).isEqualTo(NotificationScope.GLOBAL);
        assertThat(feed.get(1).state()).isEqualTo(NotificationState.UNREAD);     // created after marker
    }

    @Test
    void getFeed_globalBeforeMarker_isRead() {
        Instant now = Instant.now();
        GlobalNotification global = GlobalNotification.rehydrate(UUID.randomUUID(),
                NotificationType.NEW_EVENT_FOR_PUBLIC, "g", Map.of(), now.minusSeconds(600), "s", now.plusSeconds(3600));
        when(readMarkerRepository.findLastReadAt(userId)).thenReturn(Optional.of(now)); // marker after global
        when(userRepository.findRecentByRecipient(userId, 30)).thenReturn(List.of());
        when(globalRepository.findRecent(30)).thenReturn(List.of(global));

        List<NotificationView> feed = service.getFeed(userId, 0);

        assertThat(feed).hasSize(1);
        assertThat(feed.get(0).state()).isEqualTo(NotificationState.READ);
    }

    // ---- getUnreadCount ----

    @Test
    void getUnreadCount_redisHit_addsGlobals() {
        when(unreadCounter.get(userId)).thenReturn(Optional.of(5L));
        when(readMarkerRepository.findLastReadAt(userId)).thenReturn(Optional.empty());
        when(globalRepository.countCreatedAfter(any())).thenReturn(2L);

        assertThat(service.getUnreadCount(userId)).isEqualTo(7L);
        verify(unreadCounter, never()).set(any(), anyLong());
    }

    @Test
    void getUnreadCount_redisMiss_rebuildsFromMongo() {
        when(unreadCounter.get(userId)).thenReturn(Optional.empty());
        when(userRepository.countUnread(userId)).thenReturn(3L);
        when(readMarkerRepository.findLastReadAt(userId)).thenReturn(Optional.empty());
        when(globalRepository.countCreatedAfter(any())).thenReturn(1L);

        assertThat(service.getUnreadCount(userId)).isEqualTo(4L);
        verify(unreadCounter).set(userId, 3L);
    }

    // ---- markRead ----

    @Test
    void markRead_unread_flipsAndDecrements() {
        UUID id = UUID.randomUUID();
        UserNotification unread = UserNotification.rehydrate(id, userId, NotificationType.NEW_MATCH_REQUEST,
                "m", Map.of(), NotificationState.UNREAD, Instant.now(), "s", Instant.now().plusSeconds(3600));
        when(userRepository.findByIdAndRecipient(id, userId)).thenReturn(Optional.of(unread));

        service.markRead(userId, id);

        verify(userRepository).save(unread);
        verify(unreadCounter).decrementIfPresent(userId);
    }

    @Test
    void markRead_missing_isNoOp() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndRecipient(id, userId)).thenReturn(Optional.empty());

        service.markRead(userId, id);

        verify(userRepository, never()).save(any());
        verify(unreadCounter, never()).decrementIfPresent(any());
    }

    @Test
    void markRead_alreadyRead_doesNotDecrement() {
        UUID id = UUID.randomUUID();
        UserNotification read = UserNotification.rehydrate(id, userId, NotificationType.NEW_MATCH_REQUEST,
                "m", Map.of(), NotificationState.READ, Instant.now(), "s", Instant.now().plusSeconds(3600));
        when(userRepository.findByIdAndRecipient(id, userId)).thenReturn(Optional.of(read));

        service.markRead(userId, id);

        verify(userRepository, never()).save(any());
        verify(unreadCounter, never()).decrementIfPresent(any());
    }

    // ---- markAllRead ----

    @Test
    void markAllRead_clearsTargetedAndBroadcastBacklog() {
        service.markAllRead(userId);

        verify(userRepository).markAllRead(userId);
        verify(unreadCounter).reset(userId);
        verify(readMarkerRepository).setLastReadAt(eq(userId), any(Instant.class));
    }
}
