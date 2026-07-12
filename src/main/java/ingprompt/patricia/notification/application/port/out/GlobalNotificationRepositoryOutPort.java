package ingprompt.patricia.notification.application.port.out;

import ingprompt.patricia.notification.domain.model.GlobalNotification;

import java.time.Instant;
import java.util.List;

public interface GlobalNotificationRepositoryOutPort {
    void save(GlobalNotification notification);
    List<GlobalNotification> findRecent(int limit);
    long countCreatedAfter(Instant after);
    boolean existsBySourceEvent(String sourceEventId);
}
