package ingprompt.patricia.notification.application.service;

import ingprompt.patricia.notification.application.port.in.MarkNotificationReadCase;
import ingprompt.patricia.notification.application.port.in.NotificationQueryCase;
import ingprompt.patricia.notification.application.port.in.ReceiveNotificationCase;
import ingprompt.patricia.notification.application.port.out.GlobalNotificationRepositoryOutPort;
import ingprompt.patricia.notification.application.port.out.GlobalReadMarkerRepositoryOutPort;
import ingprompt.patricia.notification.application.port.out.NotificationPushPort;
import ingprompt.patricia.notification.application.port.out.UnreadCounterOutPort;
import ingprompt.patricia.notification.application.port.out.UserNotificationRepositoryOutPort;
import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.domain.model.GlobalNotification;
import ingprompt.patricia.notification.domain.model.NotificationView;
import ingprompt.patricia.notification.domain.model.UserNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService implements ReceiveNotificationCase, NotificationQueryCase, MarkNotificationReadCase {
    private final UserNotificationRepositoryOutPort userRepository;
    private final GlobalNotificationRepositoryOutPort globalRepository;
    private final GlobalReadMarkerRepositoryOutPort readMarkerRepository;
    private final UnreadCounterOutPort unreadCounter;
    private final NotificationPushPort notificationPush;
    private final Duration retention;
    private final int defaultLimit;

    public NotificationService(UserNotificationRepositoryOutPort userRepository, GlobalNotificationRepositoryOutPort globalRepository, GlobalReadMarkerRepositoryOutPort readMarkerRepository, UnreadCounterOutPort unreadCounter, NotificationPushPort notificationPush, @Value("${notification.retention-days}") long retentionDays, @Value("${notification.feed.default-limit}") int defaultLimit) {
        this.userRepository = userRepository;
        this.globalRepository = globalRepository;
        this.readMarkerRepository = readMarkerRepository;
        this.unreadCounter = unreadCounter;
        this.notificationPush = notificationPush;
        this.retention = Duration.ofDays(retentionDays);
        this.defaultLimit = defaultLimit;
    }

    // ---- ReceiveNotificationCase ----
    @Override
    public void notifyUser(UUID recipientId, NotificationType type, String message, Map<String, String> payload, String sourceEventId) {
        if (userRepository.existsBySourceEventAndRecipient(sourceEventId, recipientId)) {
            log.debug("Skipping duplicate notification for recipient {} (source {})", recipientId, sourceEventId);
            return;
        }
        UserNotification notification = UserNotification.create(recipientId, type, message, payload, sourceEventId, retention);
        userRepository.save(notification);
        unreadCounter.incrementIfPresent(recipientId);
        // Real-time fan-out to the user's bell (fire-and-forget; never rolls back the write).
        notificationPush.pushToUser(recipientId, NotificationView.ofTargeted(notification));
    }

    @Override
    public void notifyUsers(Set<UUID> recipientIds, NotificationType type, String message, Map<String, String> payload, String sourceEventId) {
        if (recipientIds == null) {
            return;
        }
        for (UUID recipientId : recipientIds) {
            notifyUser(recipientId, type, message, payload, sourceEventId);
        }
    }

    @Override
    public void notifyEveryone(NotificationType type, String message, Map<String, String> payload, String sourceEventId) {
        if (globalRepository.existsBySourceEvent(sourceEventId)) {
            log.debug("Skipping duplicate broadcast (source {})", sourceEventId);
            return;
        }
        GlobalNotification notification = GlobalNotification.create(type, message, payload, sourceEventId, retention);
        globalRepository.save(notification);
        // Broadcast to every connected client; a fresh global is unread for everyone.
        notificationPush.pushToAll(NotificationView.ofGlobal(notification, NotificationState.UNREAD));
    }

    // ---- NotificationQueryCase ----
    @Override
    public List<NotificationView> getFeed(UUID userId, int limit) {
        int effectiveLimit = limit > 0 ? limit : defaultLimit;
        Instant lastReadAt = readMarkerRepository.findLastReadAt(userId).orElse(Instant.EPOCH);

        List<NotificationView> feed = new ArrayList<>();
        userRepository.findRecentByRecipient(userId, effectiveLimit).forEach(n -> feed.add(NotificationView.ofTargeted(n)));
        globalRepository.findRecent(effectiveLimit).forEach(g -> {
            NotificationState state = g.getCreatedAt().isAfter(lastReadAt) ? NotificationState.UNREAD : NotificationState.READ;
            feed.add(NotificationView.ofGlobal(g, state));
        });

        feed.sort(Comparator.comparing(NotificationView::createdAt).reversed());
        return feed.size() > effectiveLimit ? feed.subList(0, effectiveLimit) : feed;
    }

    @Override
    public long getUnreadCount(UUID userId) {
        long targeted = unreadCounter.get(userId).orElseGet(() -> {
            long fromDb = userRepository.countUnread(userId);
            unreadCounter.set(userId, fromDb);
            return fromDb;
        });
        Instant lastReadAt = readMarkerRepository.findLastReadAt(userId).orElse(Instant.EPOCH);
        long globals = globalRepository.countCreatedAfter(lastReadAt);
        return targeted + globals;
    }

    // ---- MarkNotificationReadCase ----
    @Override
    public void markRead(UUID userId, UUID notificationId) {
        Optional<UserNotification> maybe = userRepository.findByIdAndRecipient(notificationId, userId);
        if (maybe.isEmpty()) {
            return;
        }
        UserNotification notification = maybe.get();
        if (notification.markRead()) {
            userRepository.save(notification);
            unreadCounter.decrementIfPresent(userId);
        }
    }

    @Override
    public void markAllRead(UUID userId) {
        userRepository.markAllRead(userId);
        unreadCounter.reset(userId);
        readMarkerRepository.setLastReadAt(userId, Instant.now());
    }
}
