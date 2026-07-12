package ingprompt.patricia.notification.infrastructure.ws.backplane;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ingprompt.patricia.notification.domain.enums.NotificationScope;
import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.infrastructure.web.dto.response.NotificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisNotificationRelayPublisherTest {

    @Mock
    private StringRedisTemplate backplaneRedisTemplate;
    @Mock
    private ObjectMapper objectMapper;

    private RedisNotificationRelayPublisher publisher;

    private NotificationResponse payload() {
        return new NotificationResponse(UUID.randomUUID(), NotificationScope.TARGETED, NotificationType.NEW_MATCH_REQUEST,
                "hi", Map.of(), NotificationState.UNREAD, Instant.now());
    }

    @BeforeEach
    void setUp() {
        publisher = new RedisNotificationRelayPublisher(backplaneRedisTemplate, objectMapper);
        ReflectionTestUtils.setField(publisher, "channel", "patricia:notifications");
    }

    @Test
    void publishToUser_serializesEnvelopeAndSendsToChannel() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any(NotificationBroadcastEnvelope.class))).thenReturn("{\"json\":true}");

        publisher.publishToUser("user-1", payload());

        verify(backplaneRedisTemplate).convertAndSend("patricia:notifications", "{\"json\":true}");
    }

    @Test
    void publishToAll_wrapsPayloadWithNullTargetUser() throws JsonProcessingException {
        ArgumentCapture capture = new ArgumentCapture();
        when(objectMapper.writeValueAsString(any(NotificationBroadcastEnvelope.class)))
                .thenAnswer(invocation -> {
                    capture.envelope = invocation.getArgument(0);
                    return "{}";
                });

        publisher.publishToAll(payload());

        verify(backplaneRedisTemplate).convertAndSend(eq("patricia:notifications"), anyString());
        org.assertj.core.api.Assertions.assertThat(capture.envelope.isGlobalBroadcast()).isTrue();
    }

    @Test
    void publish_serializationFails_isSwallowed() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenThrow(mock(JsonProcessingException.class));

        publisher.publishToUser("user-1", payload()); // must not throw
    }

    @Test
    void publish_redisThrowsRuntimeException_isSwallowed() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        doThrow(new RuntimeException("connection reset")).when(backplaneRedisTemplate).convertAndSend(anyString(), anyString());

        publisher.publishToUser("user-1", payload()); // must not throw
    }

    private static class ArgumentCapture {
        NotificationBroadcastEnvelope envelope;
    }
}
