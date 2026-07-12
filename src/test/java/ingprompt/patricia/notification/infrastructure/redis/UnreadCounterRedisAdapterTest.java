package ingprompt.patricia.notification.infrastructure.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnreadCounterRedisAdapterTest {

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private UnreadCounterRedisAdapter adapter;
    private UUID userId;
    private String key;

    @BeforeEach
    void setUp() {
        lenient().when(redis.opsForValue()).thenReturn(valueOperations);
        adapter = new UnreadCounterRedisAdapter(redis);
        userId = UUID.randomUUID();
        key = "notif:unread:" + userId;
    }

    @Test
    void get_present_parsesLong() {
        when(valueOperations.get(key)).thenReturn("42");

        assertThat(adapter.get(userId)).contains(42L);
    }

    @Test
    void get_absent_returnsEmpty() {
        when(valueOperations.get(key)).thenReturn(null);

        assertThat(adapter.get(userId)).isEmpty();
    }

    @Test
    void set_storesNonNegativeValue() {
        adapter.set(userId, 7L);
        verify(valueOperations).set(key, "7");
    }

    @Test
    void set_negativeValue_clampsToZero() {
        adapter.set(userId, -5L);
        verify(valueOperations).set(key, "0");
    }

    @Test
    void incrementIfPresent_keyExists_increments() {
        when(redis.hasKey(key)).thenReturn(true);

        adapter.incrementIfPresent(userId);

        verify(valueOperations).increment(key);
    }

    @Test
    void incrementIfPresent_keyMissing_doesNothing() {
        when(redis.hasKey(key)).thenReturn(false);

        adapter.incrementIfPresent(userId);

        verify(valueOperations, org.mockito.Mockito.never()).increment(key);
    }

    @Test
    void decrementIfPresent_keyExists_decrementsWithoutGoingNegative() {
        when(redis.hasKey(key)).thenReturn(true);
        when(valueOperations.decrement(key)).thenReturn(2L);

        adapter.decrementIfPresent(userId);

        verify(valueOperations).decrement(key);
        verify(valueOperations, org.mockito.Mockito.never()).set(key, "0");
    }

    @Test
    void decrementIfPresent_goesNegative_resetsToZero() {
        when(redis.hasKey(key)).thenReturn(true);
        when(valueOperations.decrement(key)).thenReturn(-1L);

        adapter.decrementIfPresent(userId);

        verify(valueOperations).set(key, "0");
    }

    @Test
    void decrementIfPresent_keyMissing_doesNothing() {
        when(redis.hasKey(key)).thenReturn(false);

        adapter.decrementIfPresent(userId);

        verify(valueOperations, org.mockito.Mockito.never()).decrement(key);
    }

    @Test
    void reset_setsCounterToZero() {
        adapter.reset(userId);
        verify(valueOperations).set(key, "0");
    }
}
