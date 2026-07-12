package ingprompt.patricia.notification.infrastructure.persistence.mongo.document;

import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_notifications")
@CompoundIndex(name = "recipient_created_idx", def = "{'recipientId': 1, 'createdAt': -1}")
@CompoundIndex(name = "recipient_state_idx", def = "{'recipientId': 1, 'state': 1}")
public class UserNotificationDocument {
    @Id
    private UUID id;

    private UUID recipientId;
    private NotificationType type;
    private String message;
    private Map<String, String> payload;
    private NotificationState state;
    private Instant createdAt;
    private String sourceEventId;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;
}
