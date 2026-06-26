package ingprompt.patricia.notification.domain.enums;

/**
 * Whether a notification is addressed to a specific user (TARGETED, one document
 * per recipient) or broadcast to everyone (GLOBAL, stored once and read-merged).
 */
public enum NotificationScope {
    TARGETED,
    GLOBAL
}
