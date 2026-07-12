package ingprompt.patricia.notification.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequestedEvent {
    private String sourceEventId;
    private UUID requesterId;
    private String requesterName;
    private UUID targetUserId;
}
