package ingprompt.patricia.notification.infrastructure.messaging.listener;

import ingprompt.patricia.notification.application.port.in.ReceiveNotificationCase;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.infrastructure.messaging.config.RabbitMQConfig;
import ingprompt.patricia.notification.infrastructure.messaging.event.MatchRequestedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class MatchingEventsListener {
    private final ReceiveNotificationCase receiveNotification;

    @RabbitListener(queues = RabbitMQConfig.MATCH_REQUESTED_QUEUE)
    public void onMatchRequested(MatchRequestedEvent event) {
        log.info("Notifying user {} of a match request from {}", event.getTargetUserId(), event.getRequesterId());
        receiveNotification.notifyUser(
                event.getTargetUserId(),
                NotificationType.NEW_MATCH_REQUEST,
                NotificationType.NEW_MATCH_REQUEST.render(event.getRequesterName()),
                Map.of("requesterId", String.valueOf(event.getRequesterId())),
                event.getSourceEventId());
    }
}
