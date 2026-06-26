package ingprompt.patricia.notification.application.port.in;

import java.util.UUID;

public interface MarkNotificationReadCase {
    void markRead(UUID userId, UUID notificationId);
    void markAllRead(UUID userId);
}
