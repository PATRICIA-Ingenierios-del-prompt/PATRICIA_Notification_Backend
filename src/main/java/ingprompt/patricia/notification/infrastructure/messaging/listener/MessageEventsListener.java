package ingprompt.patricia.notification.infrastructure.messaging.listener;

import ingprompt.patricia.notification.infrastructure.messaging.config.RabbitMQConfig;
import ingprompt.patricia.notification.infrastructure.messaging.event.MessageCreatedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class MessageEventsListener {

    @RabbitListener(queues = RabbitMQConfig.MESSAGE_CREATED_QUEUE)
    public void onMessageCreated(MessageCreatedEvent event) {
        log.debug("Received message event on chat {} (parche {}) — offline-only push handling deferred to the push layer",
                event.getChatId(), event.getParcheId());
        // TODO(push-layer): for each recipient in event.getRecipientIds() that is OFFLINE,
        // emit/collapse a "new messages in <parcheName>" mobile push.
    }
}
