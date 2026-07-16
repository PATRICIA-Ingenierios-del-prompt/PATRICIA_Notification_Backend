package ingprompt.patricia.notification.infrastructure.messaging.listener;

import ingprompt.patricia.notification.application.port.in.ReceiveNotificationCase;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.infrastructure.messaging.config.RabbitMQConfig;
import ingprompt.patricia.notification.infrastructure.messaging.event.EventoEnvelope;
import ingprompt.patricia.notification.infrastructure.messaging.event.MatchConfirmadoPayload;
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

    @RabbitListener(queues = RabbitMQConfig.MATCH_CONFIRMED_QUEUE)
    public void onMatchConfirmado(EventoEnvelope<MatchConfirmadoPayload> event) {
        MatchConfirmadoPayload payload = event.getPayload();
        log.info("Notifying user {} of match {} with {}", event.getUsuarioId(), payload.getMatchId(), payload.getOtroUsuarioId());
        receiveNotification.notifyUser(
                event.getUsuarioId(),
                NotificationType.NEW_MATCH_CONFIRMED,
                NotificationType.NEW_MATCH_CONFIRMED.render(),
                Map.of(
                        "matchId", String.valueOf(payload.getMatchId()),
                        "otroUsuarioId", String.valueOf(payload.getOtroUsuarioId())),
                event.getEventoId());
    }
}
