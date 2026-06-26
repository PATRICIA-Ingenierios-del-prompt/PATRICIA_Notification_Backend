package ingprompt.patricia.notification.infrastructure.web;

import ingprompt.patricia.notification.application.port.in.MarkNotificationReadCase;
import ingprompt.patricia.notification.application.port.in.NotificationQueryCase;
import ingprompt.patricia.notification.infrastructure.web.dto.response.NotificationResponse;
import ingprompt.patricia.notification.infrastructure.web.dto.response.UnreadCountResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/notifications")
@AllArgsConstructor
public class NotificationController {
    private final NotificationQueryCase queryCase;
    private final MarkNotificationReadCase markReadCase;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> feed(@RequestHeader("X-User-Id") UUID userId, @RequestParam(defaultValue = "0") int limit) {
        List<NotificationResponse> feed = queryCase.getFeed(userId, limit).stream().map(NotificationResponse::from).toList();
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(new UnreadCountResponse(queryCase.getUnreadCount(userId)));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markRead(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID notificationId) {
        markReadCase.markRead(userId, notificationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@RequestHeader("X-User-Id") UUID userId) {
        markReadCase.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }
}
