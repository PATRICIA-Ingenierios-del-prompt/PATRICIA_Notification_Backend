package ingprompt.patricia.notification.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface GlobalReadMarkerRepositoryOutPort {
    Optional<Instant> findLastReadAt(UUID userId);
    void setLastReadAt(UUID userId, Instant lastReadAt);
}
