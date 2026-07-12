package ingprompt.patricia.notification.infrastructure.ws.backplane;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * El "sub" de Redis que faltaba: cada replica del pod se suscribe al mismo
 * canal del backplane y, al recibir un mensaje (publicado por CUALQUIER
 * replica, incluida ella misma), lo entrega a sus propios clientes
 * WebSocket conectados localmente via {@link SimpMessagingTemplate}. Esto
 * es lo que hace que el {@code SimpleBroker} en memoria (ver StompConfig)
 * funcione correctamente con 2+ replicas: sin este relay, un cliente
 * conectado a la replica B nunca se entera de un evento que RabbitMQ
 * entrego a la replica A.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationRelaySubscriber implements MessageListener {

    private static final String USER_QUEUE = "/queue/notifications";
    private static final String GLOBAL_TOPIC = "/topic/notifications";

    private final RedisMessageListenerContainer backplaneRedisMessageListenerContainer;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Value("${backplane.redis.channel}")
    private String channel;

    @PostConstruct
    void subscribe() {
        backplaneRedisMessageListenerContainer.addMessageListener(this, new ChannelTopic(channel));
        log.info("Subscribed to backplane Redis channel '{}' for cross-replica STOMP relay", channel);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            NotificationBroadcastEnvelope envelope = objectMapper.readValue(json, NotificationBroadcastEnvelope.class);

            if (envelope.isGlobalBroadcast()) {
                messagingTemplate.convertAndSend(GLOBAL_TOPIC, envelope.payload());
            } else {
                messagingTemplate.convertAndSendToUser(envelope.targetUserId(), USER_QUEUE, envelope.payload());
            }
        } catch (Exception ex) {
            // Un mensaje malformado o un fallo de entrega local no debe tumbar
            // el listener container - solo se pierde ese push puntual.
            log.warn("Failed to relay backplane notification message: {}", ex.getMessage());
        }
    }
}
