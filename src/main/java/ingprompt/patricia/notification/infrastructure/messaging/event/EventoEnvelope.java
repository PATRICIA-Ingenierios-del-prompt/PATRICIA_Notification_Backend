package ingprompt.patricia.notification.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Generic wrapper for User Backend's event envelope (usuario.creado, perfil.actualizado,
 * logro.desbloqueado, etc.): {eventoId, timestamp, usuarioId, tipo, payload}.
 * Consumers declare {@code EventoEnvelope<TheirPayloadType>} as the listener parameter;
 * Jackson2JsonMessageConverter (TypePrecedence.INFERRED) resolves the generic from it.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoEnvelope<T> {
    private String eventoId;
    private Instant timestamp;
    private UUID usuarioId;
    private String tipo;
    private T payload;
}
