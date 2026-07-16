package ingprompt.patricia.notification.infrastructure.persistence.mongo.mapper;

import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.domain.model.GlobalNotification;
import ingprompt.patricia.notification.domain.model.UserNotification;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.GlobalNotificationDocument;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.UserNotificationDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationDocumentMapperTest {

    @Test
    void userNotification_toDocument_thenToDomain_roundTrips() {
        UUID id = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(3600);
        UserNotification original = UserNotification.rehydrate(id, recipientId, NotificationType.NEW_MATCH_CONFIRMED,
                "hi", Map.of("k", "v"), NotificationState.UNREAD, now, "evt", expires);

        UserNotificationDocument document = NotificationDocumentMapper.toDocument(original);

        assertThat(document.getId()).isEqualTo(id);
        assertThat(document.getRecipientId()).isEqualTo(recipientId);
        assertThat(document.getType()).isEqualTo(NotificationType.NEW_MATCH_CONFIRMED);
        assertThat(document.getMessage()).isEqualTo("hi");
        assertThat(document.getPayload()).containsEntry("k", "v");
        assertThat(document.getState()).isEqualTo(NotificationState.UNREAD);
        assertThat(document.getCreatedAt()).isEqualTo(now);
        assertThat(document.getSourceEventId()).isEqualTo("evt");
        assertThat(document.getExpiresAt()).isEqualTo(expires);

        UserNotification roundTripped = NotificationDocumentMapper.toDomain(document);

        assertThat(roundTripped.getId()).isEqualTo(id);
        assertThat(roundTripped.getRecipientId()).isEqualTo(recipientId);
        assertThat(roundTripped.getType()).isEqualTo(NotificationType.NEW_MATCH_CONFIRMED);
        assertThat(roundTripped.getMessage()).isEqualTo("hi");
        assertThat(roundTripped.getPayload()).containsEntry("k", "v");
        assertThat(roundTripped.getState()).isEqualTo(NotificationState.UNREAD);
        assertThat(roundTripped.getCreatedAt()).isEqualTo(now);
        assertThat(roundTripped.getSourceEventId()).isEqualTo("evt");
        assertThat(roundTripped.getExpiresAt()).isEqualTo(expires);
    }

    @Test
    void globalNotification_toDocument_thenToDomain_roundTrips() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(7200);
        GlobalNotification original = GlobalNotification.rehydrate(id, NotificationType.NEW_PUBLIC_PARCHE,
                "g", Map.of("parcheId", "p"), now, "evt2", expires);

        GlobalNotificationDocument document = NotificationDocumentMapper.toDocument(original);

        assertThat(document.getId()).isEqualTo(id);
        assertThat(document.getType()).isEqualTo(NotificationType.NEW_PUBLIC_PARCHE);
        assertThat(document.getMessage()).isEqualTo("g");
        assertThat(document.getPayload()).containsEntry("parcheId", "p");
        assertThat(document.getCreatedAt()).isEqualTo(now);
        assertThat(document.getSourceEventId()).isEqualTo("evt2");
        assertThat(document.getExpiresAt()).isEqualTo(expires);

        GlobalNotification roundTripped = NotificationDocumentMapper.toDomain(document);

        assertThat(roundTripped.getId()).isEqualTo(id);
        assertThat(roundTripped.getType()).isEqualTo(NotificationType.NEW_PUBLIC_PARCHE);
        assertThat(roundTripped.getMessage()).isEqualTo("g");
        assertThat(roundTripped.getPayload()).containsEntry("parcheId", "p");
        assertThat(roundTripped.getCreatedAt()).isEqualTo(now);
        assertThat(roundTripped.getSourceEventId()).isEqualTo("evt2");
        assertThat(roundTripped.getExpiresAt()).isEqualTo(expires);
    }
}
