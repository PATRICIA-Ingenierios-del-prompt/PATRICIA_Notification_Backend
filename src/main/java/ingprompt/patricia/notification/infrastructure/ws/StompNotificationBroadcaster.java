package ingprompt.patricia.notification.infrastructure.ws;

import ingprompt.patricia.notification.application.port.out.NotificationPushPort;
import ingprompt.patricia.notification.domain.model.NotificationView;
import ingprompt.patricia.notification.infrastructure.web.dto.response.NotificationResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
public class StompNotificationBroadcaster implements NotificationPushPort {
    private static final String USER_QUEUE = "/queue/notifications";   // resolved to /user/queue/notifications
    private static final String GLOBAL_TOPIC = "/topic/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void pushToUser(UUID userId, NotificationView view) {
        try {
            messagingTemplate.convertAndSendToUser(userId.toString(), USER_QUEUE, NotificationResponse.from(view));
        } catch (RuntimeException ex) {
            log.warn("Failed to push notification {} to user {}: {}", view.id(), userId, ex.getMessage());
        }
    }

    @Override
    public void pushToAll(NotificationView view) {
        try {
            messagingTemplate.convertAndSend(GLOBAL_TOPIC, NotificationResponse.from(view));
        } catch (RuntimeException ex) {
            log.warn("Failed to broadcast notification {}: {}", view.id(), ex.getMessage());
        }
    }
}
