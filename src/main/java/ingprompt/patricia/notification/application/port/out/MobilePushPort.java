package ingprompt.patricia.notification.application.port.out;

import ingprompt.patricia.notification.domain.model.NotificationView;

import java.util.UUID;

public interface MobilePushPort {
    void pushToUser(UUID userId, NotificationView view);
}
