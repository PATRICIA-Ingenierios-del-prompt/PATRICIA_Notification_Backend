package ingprompt.patricia.notification.infrastructure.ws.backplane;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ingprompt.patricia.notification.infrastructure.web.dto.response.NotificationResponse;

/**
 * Envelope publicado en el canal Redis del backplane (uno solo, ver
 * {@code backplane.redis.channel}). {@code targetUserId == null} significa
 * "broadcast a /topic/notifications"; si viene con valor, es un push
 * privado a "/user/{targetUserId}/queue/notifications".
 * <p>
 * Se serializa/deserializa con Jackson (mismo ObjectMapper de la app, via
 * StringRedisTemplate + un serializer JSON explicito en el publisher/
 * subscriber, para no depender de si el ObjectMapper por defecto trae
 * modulos de Java Time configurados de una forma u otra).
 */
public record NotificationBroadcastEnvelope(
        String targetUserId,
        NotificationResponse payload
) {
    @JsonIgnore
    public boolean isGlobalBroadcast() {
        return targetUserId == null;
    }
}
