package ingprompt.patricia.notification.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCreatedEvent {
    private String sourceEventId;
    private UUID eventId;
    private String name;
    private UUID ownerId;
    private boolean linkedToParche;   // true => suppressed here (handled by EventLinkedToParcheEvent)
}
