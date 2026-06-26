package ingprompt.patricia.notification.domain.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    NEW_PUBLIC_PARCHE("A new public parche '%s' was created"),
    NEW_EVENT_FOR_PUBLIC("A new event '%s' has been created for the community"),

    NEW_EVENT_IN_PARCHE("A new event has been created in %s"),
    NEW_MESSAGE_ON_PARCHE("You have a new message on %s"),
    NEW_MATCH_REQUEST("The user %s wants to connect with you");

    private final String template;

    NotificationType(String template) {
        this.template = template;
    }

    public String render(String... args) {
        return String.format(template, (Object[]) args);
    }
}
