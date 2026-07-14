package ingprompt.patricia.notification.infrastructure.messaging.listener;

import ingprompt.patricia.notification.application.port.in.ReceiveNotificationCase;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.infrastructure.messaging.config.RabbitMQConfig;
import ingprompt.patricia.notification.infrastructure.messaging.event.EventoEnvelope;
import ingprompt.patricia.notification.infrastructure.messaging.event.LogroDesbloqueadoPayload;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class LogroEventsListener {
    private final ReceiveNotificationCase receiveNotification;

    @RabbitListener(queues = RabbitMQConfig.LOGRO_DESBLOQUEADO_QUEUE)
    public void onLogroDesbloqueado(EventoEnvelope<LogroDesbloqueadoPayload> event) {
        LogroDesbloqueadoPayload payload = event.getPayload();
        log.info("Notifying user {} they unlocked mona {}", event.getUsuarioId(), payload.getCodigo());
        receiveNotification.notifyUser(
                event.getUsuarioId(),
                NotificationType.ALBUM_MONA_UNLOCKED,
                NotificationType.ALBUM_MONA_UNLOCKED.render(payload.getNombre()),
                Map.of("monaCodigo", payload.getCodigo()),
                event.getEventoId());
    }
}
