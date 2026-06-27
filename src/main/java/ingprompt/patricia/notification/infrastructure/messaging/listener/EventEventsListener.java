package ingprompt.patricia.notification.infrastructure.messaging.listener;

import ingprompt.patricia.notification.application.port.in.ReceiveNotificationCase;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.infrastructure.messaging.config.RabbitMQConfig;
import ingprompt.patricia.notification.infrastructure.messaging.event.EventCreatedEvent;
import ingprompt.patricia.notification.infrastructure.messaging.event.EventLinkedToParcheEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class EventEventsListener {
    private final ReceiveNotificationCase receiveNotification;

    @RabbitListener(queues = RabbitMQConfig.EVENT_CREATED_QUEUE)
    public void onEventCreated(EventCreatedEvent event) {
        if (event.isLinkedToParche()) {
            log.debug("Ignoring parche-linked event {} (handled as a targeted notification)", event.getEventId());
            return;
        }
        log.info("Broadcasting new community event {}", event.getEventId());
        receiveNotification.notifyEveryone(
                NotificationType.NEW_EVENT_FOR_PUBLIC,
                NotificationType.NEW_EVENT_FOR_PUBLIC.render(event.getName()),
                Map.of("eventId", String.valueOf(event.getEventId())),
                event.getSourceEventId());
    }

    @RabbitListener(queues = RabbitMQConfig.EVENT_LINKED_QUEUE)
    public void onEventLinkedToParche(EventLinkedToParcheEvent event) {
        log.info("Notifying {} members of parche {} about event {}",
                event.getMemberIds() == null ? 0 : event.getMemberIds().size(), event.getParcheId(), event.getEventId());
        receiveNotification.notifyUsers(
                event.getMemberIds(),
                NotificationType.NEW_EVENT_IN_PARCHE,
                NotificationType.NEW_EVENT_IN_PARCHE.render(event.getParcheName()),
                Map.of(
                        "eventId", String.valueOf(event.getEventId()),
                        "parcheId", String.valueOf(event.getParcheId())),
                event.getSourceEventId());
    }
}
