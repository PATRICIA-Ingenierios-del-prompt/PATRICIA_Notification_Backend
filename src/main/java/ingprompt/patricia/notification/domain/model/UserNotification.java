package ingprompt.patricia.notification.domain.model;

import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;


@Getter
public class UserNotification {
    private final UUID id;
    private final UUID recipientId;
    private final NotificationType type;
    private final String message;
    private final Map<String, String> payload;
    private NotificationState state;
    private final Instant createdAt;
    private final String sourceEventId;
    private final Instant expiresAt;

    private UserNotification(UUID id, UUID recipientId, NotificationType type, String message, Map<String, String> payload, NotificationState state, Instant createdAt, String sourceEventId, Instant expiresAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.type = type;
        this.message = message;
        this.payload = payload;
        this.state = state;
        this.createdAt = createdAt;
        this.sourceEventId = sourceEventId;
        this.expiresAt = expiresAt;
    }

    public static UserNotification create(UUID recipientId, NotificationType type, String message, Map<String, String> payload, String sourceEventId, Duration retention) {
        Instant now = Instant.now();
        return new UserNotification(UUID.randomUUID(), recipientId, type, message, payload == null ? Map.of() : payload, NotificationState.UNREAD, now, sourceEventId, now.plus(retention));
    }

    public static UserNotification rehydrate(UUID id, UUID recipientId, NotificationType type, String message, Map<String, String> payload, NotificationState state, Instant createdAt, String sourceEventId, Instant expiresAt) {
        return new UserNotification(id, recipientId, type, message, payload, state, createdAt, sourceEventId, expiresAt);
    }

    public boolean isUnread() {
        return state == NotificationState.UNREAD;
    }

    public boolean markRead() {
        if (state == NotificationState.READ) {
            return false;
        }
        state = NotificationState.READ;
        return true;
    }
}
