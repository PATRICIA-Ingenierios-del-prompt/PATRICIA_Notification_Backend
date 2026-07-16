package ingprompt.patricia.notification.domain.model;

import ingprompt.patricia.notification.domain.enums.DevicePlatform;
import ingprompt.patricia.notification.domain.enums.NotificationScope;
import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationDomainTest {

    @Test
    void userNotification_create_startsUnread() {
        UserNotification n = UserNotification.create(UUID.randomUUID(), NotificationType.NEW_MATCH_CONFIRMED,
                "hi", Map.of("k", "v"), "evt", Duration.ofDays(90));

        assertThat(n.isUnread()).isTrue();
        assertThat(n.getState()).isEqualTo(NotificationState.UNREAD);
        assertThat(n.getExpiresAt()).isAfter(n.getCreatedAt());
    }

    @Test
    void userNotification_markRead_transitionsOnce() {
        UserNotification n = UserNotification.create(UUID.randomUUID(), NotificationType.NEW_MATCH_CONFIRMED,
                "hi", null, "evt", Duration.ofDays(90));

        assertThat(n.markRead()).isTrue();              // first transition
        assertThat(n.getState()).isEqualTo(NotificationState.READ);
        assertThat(n.markRead()).isFalse();             // already read
    }

    @Test
    void userNotification_nullPayload_defaultsToEmptyMap() {
        UserNotification n = UserNotification.create(UUID.randomUUID(), NotificationType.NEW_MATCH_CONFIRMED,
                "hi", null, "evt", Duration.ofDays(90));
        assertThat(n.getPayload()).isEmpty();
    }

    @Test
    void globalNotification_create_setsFields() {
        GlobalNotification g = GlobalNotification.create(NotificationType.NEW_PUBLIC_PARCHE,
                "new parche", Map.of("parcheId", "p"), "evt", Duration.ofDays(90));

        assertThat(g.getType()).isEqualTo(NotificationType.NEW_PUBLIC_PARCHE);
        assertThat(g.getPayload()).containsEntry("parcheId", "p");
        assertThat(g.getExpiresAt()).isAfter(g.getCreatedAt());
    }

    @Test
    void notificationView_factories_mapScopeAndState() {
        UserNotification u = UserNotification.create(UUID.randomUUID(), NotificationType.NEW_MATCH_CONFIRMED,
                "hi", Map.of(), "evt", Duration.ofDays(90));
        GlobalNotification g = GlobalNotification.create(NotificationType.NEW_PUBLIC_PARCHE,
                "g", Map.of(), "evt2", Duration.ofDays(90));

        assertThat(NotificationView.ofTargeted(u).scope()).isEqualTo(NotificationScope.TARGETED);
        assertThat(NotificationView.ofGlobal(g, NotificationState.READ).scope()).isEqualTo(NotificationScope.GLOBAL);
        assertThat(NotificationView.ofGlobal(g, NotificationState.READ).state()).isEqualTo(NotificationState.READ);
    }

    @Test
    void notificationType_render_formatsTemplate() {
        assertThat(NotificationType.NEW_PUBLIC_PARCHE.render("Salsa"))
                .isEqualTo("A new public parche 'Salsa' was created");
    }

    @Test
    void deviceToken_register_setsFields() {
        UUID userId = UUID.randomUUID();
        DeviceToken t = DeviceToken.register(userId, "ExponentPushToken[x]", DevicePlatform.IOS);

        assertThat(t.getUserId()).isEqualTo(userId);
        assertThat(t.getToken()).isEqualTo("ExponentPushToken[x]");
        assertThat(t.getPlatform()).isEqualTo(DevicePlatform.IOS);
        assertThat(t.getRegisteredAt()).isNotNull();
    }

    @Test
    void deviceToken_rehydrate_restoresExactFields() {
        UUID userId = UUID.randomUUID();
        Instant registeredAt = Instant.now().minusSeconds(3600);

        DeviceToken t = DeviceToken.rehydrate(userId, "ExponentPushToken[y]", DevicePlatform.ANDROID, registeredAt);

        assertThat(t.getUserId()).isEqualTo(userId);
        assertThat(t.getToken()).isEqualTo("ExponentPushToken[y]");
        assertThat(t.getPlatform()).isEqualTo(DevicePlatform.ANDROID);
        assertThat(t.getRegisteredAt()).isEqualTo(registeredAt);
    }

    @Test
    void notificationType_render_formatsAllTemplates() {
        assertThat(NotificationType.NEW_EVENT_FOR_PUBLIC.render("Beach Cleanup"))
                .isEqualTo("A new event 'Beach Cleanup' has been created for the community");
        assertThat(NotificationType.NEW_EVENT_IN_PARCHE.render("Mountain Crew"))
                .isEqualTo("A new event has been created in Mountain Crew");
        assertThat(NotificationType.NEW_MESSAGE_ON_PARCHE.render("Mountain Crew"))
                .isEqualTo("You have a new message on Mountain Crew");
        assertThat(NotificationType.NEW_MATCH_CONFIRMED.render())
                .isEqualTo("You have a new match!");
    }
}
