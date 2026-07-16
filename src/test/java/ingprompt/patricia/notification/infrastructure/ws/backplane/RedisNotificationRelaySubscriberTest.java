package ingprompt.patricia.notification.infrastructure.ws.backplane;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ingprompt.patricia.notification.domain.enums.NotificationScope;
import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.infrastructure.web.dto.response.NotificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisNotificationRelaySubscriberTest {

    @Mock
    private RedisMessageListenerContainer container;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private Message redisMessage;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private RedisNotificationRelaySubscriber subscriber;

    @BeforeEach
    void setUp() {
        subscriber = new RedisNotificationRelaySubscriber(container, messagingTemplate, objectMapper);
        ReflectionTestUtils.setField(subscriber, "channel", "patricia:notifications");
    }

    @Test
    void subscribe_registersListenerOnConfiguredChannel() {
        subscriber.subscribe();

        ArgumentCaptor<ChannelTopic> topicCaptor = ArgumentCaptor.forClass(ChannelTopic.class);
        verify(container).addMessageListener(eq(subscriber), topicCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(topicCaptor.getValue().getTopic()).isEqualTo("patricia:notifications");
    }

    private NotificationResponse payload() {
        return new NotificationResponse(UUID.randomUUID(), NotificationScope.TARGETED, NotificationType.NEW_MATCH_CONFIRMED,
                "hi", Map.of(), NotificationState.UNREAD, Instant.now());
    }

    @Test
    void onMessage_globalBroadcast_sendsToTopic() throws Exception {
        NotificationBroadcastEnvelope envelope = new NotificationBroadcastEnvelope(null, payload());
        String json = objectMapper.writeValueAsString(envelope);
        when(redisMessage.getBody()).thenReturn(json.getBytes(StandardCharsets.UTF_8));

        subscriber.onMessage(redisMessage, null);

        verify(messagingTemplate).convertAndSend(eq("/topic/notifications"), any(NotificationResponse.class));
    }

    @Test
    void onMessage_targetedBroadcast_sendsToUserQueue() {
        NotificationBroadcastEnvelope envelope = new NotificationBroadcastEnvelope("user-1", payload());
        String json;
        try {
            json = objectMapper.writeValueAsString(envelope);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(redisMessage.getBody()).thenReturn(json.getBytes(StandardCharsets.UTF_8));

        subscriber.onMessage(redisMessage, null);

        verify(messagingTemplate).convertAndSendToUser(eq("user-1"), eq("/queue/notifications"), any(NotificationResponse.class));
    }

    @Test
    void onMessage_malformedJson_isSwallowed() {
        when(redisMessage.getBody()).thenReturn("not-json".getBytes(StandardCharsets.UTF_8));

        subscriber.onMessage(redisMessage, null); // must not throw

        verifyNoInteractions(messagingTemplate);
    }
}
