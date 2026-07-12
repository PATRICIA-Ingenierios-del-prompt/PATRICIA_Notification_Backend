package ingprompt.patricia.notification.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreatedEvent {
    private String sourceEventId;
    private UUID chatId;
    private UUID parcheId;
    private String parcheName;
    private UUID senderId;
    private Set<UUID> recipientIds;   // chat members excluding the sender
}
