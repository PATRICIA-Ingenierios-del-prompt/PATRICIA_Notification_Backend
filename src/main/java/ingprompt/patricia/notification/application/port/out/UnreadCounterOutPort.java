package ingprompt.patricia.notification.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface UnreadCounterOutPort {
    Optional<Long> get(UUID userId);
    void set(UUID userId, long value);
    void incrementIfPresent(UUID userId);
    void decrementIfPresent(UUID userId);
    void reset(UUID userId);
}
