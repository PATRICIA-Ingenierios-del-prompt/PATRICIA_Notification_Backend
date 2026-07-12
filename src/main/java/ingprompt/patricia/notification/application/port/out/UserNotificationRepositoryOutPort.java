package ingprompt.patricia.notification.application.port.out;

import ingprompt.patricia.notification.domain.model.UserNotification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserNotificationRepositoryOutPort {
    void save(UserNotification notification);
    Optional<UserNotification> findByIdAndRecipient(UUID id, UUID recipientId);
    List<UserNotification> findRecentByRecipient(UUID recipientId, int limit);
    long countUnread(UUID recipientId);
    int markAllRead(UUID recipientId);
    boolean existsBySourceEventAndRecipient(String sourceEventId, UUID recipientId);
}
