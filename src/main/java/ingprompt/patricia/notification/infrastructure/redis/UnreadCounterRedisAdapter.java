package ingprompt.patricia.notification.infrastructure.redis;

import ingprompt.patricia.notification.application.port.out.UnreadCounterOutPort;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;


@Component
@AllArgsConstructor
public class UnreadCounterRedisAdapter implements UnreadCounterOutPort {
    private static final String KEY_PREFIX = "notif:unread:";

    private final StringRedisTemplate redis;

    @Override
    public Optional<Long> get(UUID userId) {
        String value = redis.opsForValue().get(key(userId));
        return value == null ? Optional.empty() : Optional.of(Long.parseLong(value));
    }

    @Override
    public void set(UUID userId, long value) {
        redis.opsForValue().set(key(userId), Long.toString(Math.max(0, value)));
    }

    @Override
    public void incrementIfPresent(UUID userId) {
        if (Boolean.TRUE.equals(redis.hasKey(key(userId)))) {
            redis.opsForValue().increment(key(userId));
        }
    }

    @Override
    public void decrementIfPresent(UUID userId) {
        String key = key(userId);
        if (Boolean.TRUE.equals(redis.hasKey(key))) {
            Long current = redis.opsForValue().decrement(key);
            if (current != null && current < 0) {
                redis.opsForValue().set(key, "0");
            }
        }
    }

    @Override
    public void reset(UUID userId) {
        redis.opsForValue().set(key(userId), "0");
    }

    private String key(UUID userId) {
        return KEY_PREFIX + userId;
    }
}
