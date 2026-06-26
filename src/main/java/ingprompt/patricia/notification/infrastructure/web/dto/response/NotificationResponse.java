package ingprompt.patricia.notification.infrastructure.web.dto.response;

import ingprompt.patricia.notification.domain.enums.NotificationScope;
import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.domain.model.NotificationView;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationScope scope,
        NotificationType type,
        String message,
        Map<String, String> payload,
        NotificationState state,
        Instant createdAt
) {
    public static NotificationResponse from(NotificationView v) {
        return new NotificationResponse(v.id(), v.scope(), v.type(), v.message(), v.payload(), v.state(), v.createdAt());
    }
}
