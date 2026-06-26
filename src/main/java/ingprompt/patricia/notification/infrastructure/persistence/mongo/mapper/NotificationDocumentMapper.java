package ingprompt.patricia.notification.infrastructure.persistence.mongo.mapper;

import ingprompt.patricia.notification.domain.model.GlobalNotification;
import ingprompt.patricia.notification.domain.model.UserNotification;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.GlobalNotificationDocument;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.UserNotificationDocument;

public final class NotificationDocumentMapper {

    private NotificationDocumentMapper() {
    }

    public static UserNotificationDocument toDocument(UserNotification n) {
        return new UserNotificationDocument(n.getId(), n.getRecipientId(), n.getType(), n.getMessage(),
                n.getPayload(), n.getState(), n.getCreatedAt(), n.getSourceEventId(), n.getExpiresAt());
    }

    public static UserNotification toDomain(UserNotificationDocument d) {
        return UserNotification.rehydrate(d.getId(), d.getRecipientId(), d.getType(), d.getMessage(),
                d.getPayload(), d.getState(), d.getCreatedAt(), d.getSourceEventId(), d.getExpiresAt());
    }

    public static GlobalNotificationDocument toDocument(GlobalNotification n) {
        return new GlobalNotificationDocument(n.getId(), n.getType(), n.getMessage(),
                n.getPayload(), n.getCreatedAt(), n.getSourceEventId(), n.getExpiresAt());
    }

    public static GlobalNotification toDomain(GlobalNotificationDocument d) {
        return GlobalNotification.rehydrate(d.getId(), d.getType(), d.getMessage(),
                d.getPayload(), d.getCreatedAt(), d.getSourceEventId(), d.getExpiresAt());
    }
}
