package ingprompt.patricia.notification.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class GlobalReadMarker {
    private final UUID userId;
    private final Instant lastReadAt;

    public GlobalReadMarker(UUID userId, Instant lastReadAt) {
        this.userId = userId;
        this.lastReadAt = lastReadAt;
    }
}
