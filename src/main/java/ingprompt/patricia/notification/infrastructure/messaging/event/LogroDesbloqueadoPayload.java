package ingprompt.patricia.notification.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogroDesbloqueadoPayload {
    private String codigo;
    private String nombre;
    private int xp;
    private int xpTotalAcumulado;
}
