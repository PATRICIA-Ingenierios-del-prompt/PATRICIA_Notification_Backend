package ingprompt.patricia.notification.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcheCreatedEvent {
    private String sourceEventId;
    private UUID parcheId;
    private String name;
    private String visibility;   // "PUBLIC" | "PRIVATE"
    private UUID ownerId;
}
