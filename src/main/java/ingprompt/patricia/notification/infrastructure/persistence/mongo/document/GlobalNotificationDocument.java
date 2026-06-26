package ingprompt.patricia.notification.infrastructure.persistence.mongo.document;

import ingprompt.patricia.notification.domain.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "global_notifications")
public class GlobalNotificationDocument {
    @Id
    private UUID id;

    private NotificationType type;
    private String message;
    private Map<String, String> payload;

    @Indexed
    private Instant createdAt;

    private String sourceEventId;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;
}
