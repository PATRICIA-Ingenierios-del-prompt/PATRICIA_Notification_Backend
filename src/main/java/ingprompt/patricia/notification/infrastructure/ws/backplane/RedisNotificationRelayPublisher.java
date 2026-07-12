package ingprompt.patricia.notification.infrastructure.ws.backplane;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ingprompt.patricia.notification.infrastructure.web.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica en el canal compartido del backplane en vez de escribir
 * directamente en el {@code SimpMessagingTemplate} local. Cada replica del
 * pod (incluida la que publica) recibe el mensaje via
 * {@link RedisNotificationRelaySubscriber} y ahi si lo entrega a sus
 * clientes WebSocket conectados localmente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationRelayPublisher {

    private final StringRedisTemplate backplaneRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${backplane.redis.channel}")
    private String channel;

    public void publishToUser(String userId, NotificationResponse payload) {
        publish(new NotificationBroadcastEnvelope(userId, payload));
    }

    public void publishToAll(NotificationResponse payload) {
        publish(new NotificationBroadcastEnvelope(null, payload));
    }

    private void publish(NotificationBroadcastEnvelope envelope) {
        try {
            String json = objectMapper.writeValueAsString(envelope);
            backplaneRedisTemplate.convertAndSend(channel, json);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize notification envelope for backplane publish: {}", ex.getMessage());
        } catch (RuntimeException ex) {
            // No tumbar la request si el backplane esta caido momentaneamente
            // (failover de ElastiCache, etc.) - el push en vivo se pierde,
            // pero la notificacion ya quedo persistida en Mongo y aparece
            // igual la proxima vez que el cliente pida el feed por REST.
            log.warn("Failed to publish notification envelope to backplane: {}", ex.getMessage());
        }
    }
}
