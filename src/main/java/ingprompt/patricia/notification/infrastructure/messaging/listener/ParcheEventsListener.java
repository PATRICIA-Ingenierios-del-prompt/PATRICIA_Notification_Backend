package ingprompt.patricia.notification.infrastructure.messaging.listener;

import ingprompt.patricia.notification.application.port.in.ReceiveNotificationCase;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.infrastructure.messaging.config.RabbitMQConfig;
import ingprompt.patricia.notification.infrastructure.messaging.event.ParcheCreatedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class ParcheEventsListener {
    private static final String PUBLIC = "PUBLIC";

    private final ReceiveNotificationCase receiveNotification;

    @RabbitListener(queues = RabbitMQConfig.PARCHE_CREATED_QUEUE)
    public void onParcheCreated(ParcheCreatedEvent event) {
        if (!PUBLIC.equalsIgnoreCase(event.getVisibility())) {
            log.debug("Ignoring non-public parche {}", event.getParcheId());
            return;
        }
        log.info("Broadcasting new public parche {}", event.getParcheId());
        receiveNotification.notifyEveryone(
                NotificationType.NEW_PUBLIC_PARCHE,
                NotificationType.NEW_PUBLIC_PARCHE.render(event.getName()),
                Map.of("parcheId", String.valueOf(event.getParcheId())),
                event.getSourceEventId());
    }
}
