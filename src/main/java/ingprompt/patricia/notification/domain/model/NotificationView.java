package ingprompt.patricia.notification.domain.model;

import ingprompt.patricia.notification.domain.enums.NotificationScope;
import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.enums.NotificationType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;


public record NotificationView(
        UUID id,
        NotificationScope scope,
        NotificationType type,
        String message,
        Map<String, String> payload,
        NotificationState state,
        Instant createdAt
) {
    public static NotificationView ofTargeted(UserNotification n) {
        return new NotificationView(n.getId(), NotificationScope.TARGETED, n.getType(), n.getMessage(), n.getPayload(), n.getState(), n.getCreatedAt());
    }

    public static NotificationView ofGlobal(GlobalNotification n, NotificationState state) {
        return new NotificationView(n.getId(), NotificationScope.GLOBAL, n.getType(), n.getMessage(), n.getPayload(), state, n.getCreatedAt());
    }
}
