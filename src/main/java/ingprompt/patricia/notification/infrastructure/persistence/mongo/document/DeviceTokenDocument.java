package ingprompt.patricia.notification.infrastructure.persistence.mongo.document;

import ingprompt.patricia.notification.domain.enums.DevicePlatform;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "device_tokens")
public class DeviceTokenDocument {
    @Id
    private String token;

    @Indexed
    private UUID userId;

    private DevicePlatform platform;
    private Instant registeredAt;
}
