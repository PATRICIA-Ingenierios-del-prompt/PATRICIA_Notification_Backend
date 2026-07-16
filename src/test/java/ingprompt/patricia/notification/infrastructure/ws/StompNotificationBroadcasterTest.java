package ingprompt.patricia.notification.infrastructure.ws;

import ingprompt.patricia.notification.domain.enums.NotificationScope;
import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.domain.model.NotificationView;
import ingprompt.patricia.notification.infrastructure.web.dto.response.NotificationResponse;
import ingprompt.patricia.notification.infrastructure.ws.backplane.RedisNotificationRelayPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StompNotificationBroadcasterTest {

    @Mock
    private RedisNotificationRelayPublisher relayPublisher;

    private StompNotificationBroadcaster broadcaster;

    private NotificationView view() {
        return new NotificationView(UUID.randomUUID(), NotificationScope.TARGETED, NotificationType.NEW_MATCH_CONFIRMED,
                "hi", Map.of(), NotificationState.UNREAD, Instant.now());
    }

    @Test
    void pushToUser_publishesToRelay() {
        broadcaster = new StompNotificationBroadcaster(relayPublisher);
        UUID userId = UUID.randomUUID();

        broadcaster.pushToUser(userId, view());

        verify(relayPublisher).publishToUser(eq(userId.toString()), any(NotificationResponse.class));
    }

    @Test
    void pushToUser_relayThrows_isSwallowed() {
        broadcaster = new StompNotificationBroadcaster(relayPublisher);
        UUID userId = UUID.randomUUID();
        doThrow(new RuntimeException("redis down")).when(relayPublisher).publishToUser(any(), any());

        broadcaster.pushToUser(userId, view()); // must not throw
    }

    @Test
    void pushToAll_publishesToRelay() {
        broadcaster = new StompNotificationBroadcaster(relayPublisher);

        broadcaster.pushToAll(view());

        verify(relayPublisher).publishToAll(any(NotificationResponse.class));
    }

    @Test
    void pushToAll_relayThrows_isSwallowed() {
        broadcaster = new StompNotificationBroadcaster(relayPublisher);
        doThrow(new RuntimeException("redis down")).when(relayPublisher).publishToAll(any());

        broadcaster.pushToAll(view()); // must not throw
    }
}
