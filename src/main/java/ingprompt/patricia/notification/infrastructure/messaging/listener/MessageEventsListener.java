package ingprompt.patricia.notification.infrastructure.messaging.listener;

import ingprompt.patricia.notification.application.port.in.ReceiveNotificationCase;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.infrastructure.messaging.config.RabbitMQConfig;
import ingprompt.patricia.notification.infrastructure.messaging.event.MessageCreatedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class MessageEventsListener {

    private final ReceiveNotificationCase receiveNotification;

    @RabbitListener(queues = RabbitMQConfig.MESSAGE_CREATED_QUEUE)
    public void onMessageCreated(MessageCreatedEvent event) {
        Set<UUID> recipientIds = event.getRecipientIds() == null ? Set.of() :
                event.getRecipientIds().stream()
                        .map(UUID::fromString)
                        .filter(uuid -> !uuid.equals(event.getSenderId()))
                        .collect(Collectors.toSet());

        if (recipientIds.isEmpty()) {
            log.debug("No recipients for message {} — skipping notification", event.getMessageId());
            return;
        }

        String preview = "Mensaje nuevo";
        if ("FILE".equalsIgnoreCase(event.getMessageType()) || "IMAGE".equalsIgnoreCase(event.getMessageType())) {
            preview = "Archivo adjunto";
        } else if (event.getContent() != null && !event.getContent().isBlank()) {
            preview = event.getContent();
        }

        String context = event.getParcheName() != null && !event.getParcheName().isBlank()
                ? event.getParcheName()
                : "chat privado";

        String message = (event.getSenderUsername() != null ? event.getSenderUsername() : "Alguien")
                + " en " + context + ": " + preview;

        Map<String, String> payload = new HashMap<>();
        if (event.getChatId() != null) {
            payload.put("chatId", event.getChatId().toString());
        }
        if (event.getParcheId() != null) {
            payload.put("parcheId", event.getParcheId().toString());
        }

        log.info("Notifying {} recipient(s) for message {}", recipientIds.size(), event.getMessageId());
        receiveNotification.notifyUsers(
                recipientIds,
                NotificationType.NEW_MESSAGE_ON_PARCHE,
                message,
                payload,
                event.getMessageId()
        );
    }
}
