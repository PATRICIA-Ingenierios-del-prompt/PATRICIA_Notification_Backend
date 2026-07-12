package ingprompt.patricia.notification.infrastructure.ws;

import ingprompt.patricia.notification.application.port.out.NotificationPushPort;
import ingprompt.patricia.notification.domain.model.NotificationView;
import ingprompt.patricia.notification.infrastructure.web.dto.response.NotificationResponse;
import ingprompt.patricia.notification.infrastructure.ws.backplane.RedisNotificationRelayPublisher;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * IMPORTANTE: este adaptador YA NO escribe directo al SimpMessagingTemplate
 * local. Con 2+ replicas del pod (ver deploy/values.yaml, replicaCount),
 * el cliente WebSocket de un usuario puede estar conectado a CUALQUIERA de
 * las replicas -- no necesariamente a la misma que recibio el evento de
 * RabbitMQ y llamo a este metodo. Por eso ahora publica al backplane de
 * Redis (Cluster #2); el relay real hacia el cliente conectado lo hace
 * {@link ingprompt.patricia.notification.infrastructure.ws.backplane.RedisNotificationRelaySubscriber}
 * en la replica donde el cliente esta efectivamente conectado (incluida
 * esta misma replica, si aplica).
 */
@Slf4j
@Component
@AllArgsConstructor
public class StompNotificationBroadcaster implements NotificationPushPort {

    private final RedisNotificationRelayPublisher relayPublisher;

    @Override
    public void pushToUser(UUID userId, NotificationView view) {
        try {
            relayPublisher.publishToUser(userId.toString(), NotificationResponse.from(view));
        } catch (RuntimeException ex) {
            log.warn("Failed to push notification {} to user {}: {}", view.id(), userId, ex.getMessage());
        }
    }

    @Override
    public void pushToAll(NotificationView view) {
        try {
            relayPublisher.publishToAll(NotificationResponse.from(view));
        } catch (RuntimeException ex) {
            log.warn("Failed to broadcast notification {}: {}", view.id(), ex.getMessage());
        }
    }
}
