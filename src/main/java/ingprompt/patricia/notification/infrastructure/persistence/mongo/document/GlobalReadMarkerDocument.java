package ingprompt.patricia.notification.infrastructure.persistence.mongo.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "global_read_markers")
public class GlobalReadMarkerDocument {
    @Id
    private UUID userId;

    private Instant lastReadAt;
}
