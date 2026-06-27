package ingprompt.patricia.notification.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventLinkedToParcheEvent {
    private String sourceEventId;
    private UUID eventId;
    private String eventName;
    private UUID parcheId;
    private String parcheName;
    private Set<UUID> memberIds;   // recipients (parche members at creation time)
}
