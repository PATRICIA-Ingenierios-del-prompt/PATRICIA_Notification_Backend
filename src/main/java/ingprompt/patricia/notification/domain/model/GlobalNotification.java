package ingprompt.patricia.notification.domain.model;

import ingprompt.patricia.notification.domain.enums.NotificationType;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;


@Getter
public class GlobalNotification {
    private final UUID id;
    private final NotificationType type;
    private final String message;
    private final Map<String, String> payload;
    private final Instant createdAt;
    private final String sourceEventId;
    private final Instant expiresAt;

    private GlobalNotification(UUID id, NotificationType type, String message, Map<String, String> payload, Instant createdAt, String sourceEventId, Instant expiresAt) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.payload = payload;
        this.createdAt = createdAt;
        this.sourceEventId = sourceEventId;
        this.expiresAt = expiresAt;
    }

    public static GlobalNotification create(NotificationType type, String message, Map<String, String> payload, String sourceEventId, Duration retention) {
        Instant now = Instant.now();
        return new GlobalNotification(UUID.randomUUID(), type, message, payload == null ? Map.of() : payload, now, sourceEventId, now.plus(retention));
    }

    public static GlobalNotification rehydrate(UUID id, NotificationType type, String message, Map<String, String> payload, Instant createdAt, String sourceEventId, Instant expiresAt) {
        return new GlobalNotification(id, type, message, payload, createdAt, sourceEventId, expiresAt);
    }
}
