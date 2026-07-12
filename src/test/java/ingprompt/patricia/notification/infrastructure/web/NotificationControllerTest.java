package ingprompt.patricia.notification.infrastructure.web;

import ingprompt.patricia.notification.application.port.in.MarkNotificationReadCase;
import ingprompt.patricia.notification.application.port.in.NotificationQueryCase;
import ingprompt.patricia.notification.domain.enums.NotificationScope;
import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.domain.model.NotificationView;
import ingprompt.patricia.notification.infrastructure.web.dto.response.NotificationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationQueryCase queryCase;
    @Mock
    private MarkNotificationReadCase markReadCase;

    private NotificationController controller;

    @Test
    void feed_mapsViewsToResponses() {
        controller = new NotificationController(queryCase, markReadCase);
        UUID userId = UUID.randomUUID();
        NotificationView view = new NotificationView(UUID.randomUUID(), NotificationScope.TARGETED,
                NotificationType.NEW_MATCH_REQUEST, "hi", Map.of(), NotificationState.UNREAD, Instant.now());
        when(queryCase.getFeed(userId, 5)).thenReturn(List.of(view));

        ResponseEntity<List<NotificationResponse>> response = controller.feed(userId, 5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).message()).isEqualTo("hi");
    }

    @Test
    void unreadCount_returnsCountFromQueryCase() {
        controller = new NotificationController(queryCase, markReadCase);
        UUID userId = UUID.randomUUID();
        when(queryCase.getUnreadCount(userId)).thenReturn(9L);

        ResponseEntity<?> response = controller.unreadCount(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void markRead_delegatesAndReturnsNoContent() {
        controller = new NotificationController(queryCase, markReadCase);
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.markRead(userId, notificationId);

        verify(markReadCase).markRead(userId, notificationId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void markAllRead_delegatesAndReturnsNoContent() {
        controller = new NotificationController(queryCase, markReadCase);
        UUID userId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.markAllRead(userId);

        verify(markReadCase).markAllRead(userId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
