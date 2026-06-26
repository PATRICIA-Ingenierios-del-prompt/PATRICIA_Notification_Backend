package ingprompt.patricia.notification.application.port.in;

import ingprompt.patricia.notification.domain.model.NotificationView;

import java.util.List;
import java.util.UUID;

public interface NotificationQueryCase {
    List<NotificationView> getFeed(UUID userId, int limit);
    long getUnreadCount(UUID userId);
}
