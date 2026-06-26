package ingprompt.patricia.notification.application.port.in;

import ingprompt.patricia.notification.domain.enums.NotificationType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;


public interface ReceiveNotificationCase {
    void notifyUser(UUID recipientId, NotificationType type, String message, Map<String, String> payload, String sourceEventId);
    void notifyUsers(Set<UUID> recipientIds, NotificationType type, String message, Map<String, String> payload, String sourceEventId);
    void notifyEveryone(NotificationType type, String message, Map<String, String> payload, String sourceEventId);
}
