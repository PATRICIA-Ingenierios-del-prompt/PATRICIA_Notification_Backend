package ingprompt.patricia.notification.infrastructure.ws.backplane;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import static org.assertj.core.api.Assertions.assertThat;

class BackplaneRedisConfigTest {

    private final BackplaneRedisConfig config = new BackplaneRedisConfig();

    @Test
    void connectionFactory_withoutPasswordOrSsl_configuresHostAndPort() {
        LettuceConnectionFactory factory = config.backplaneRedisConnectionFactory("localhost", 6379, "", false);

        assertThat(factory.getHostName()).isEqualTo("localhost");
        assertThat(factory.getPort()).isEqualTo(6379);
    }

    @Test
    void connectionFactory_withPasswordAndSsl_stillConfigures() {
        LettuceConnectionFactory factory = config.backplaneRedisConnectionFactory("redis-host", 6380, "s3cr3t", true);

        assertThat(factory.getHostName()).isEqualTo("redis-host");
        assertThat(factory.getPort()).isEqualTo(6380);
    }

    @Test
    void redisTemplate_isBackedByGivenConnectionFactory() {
        LettuceConnectionFactory factory = config.backplaneRedisConnectionFactory("localhost", 6379, "", false);

        StringRedisTemplate template = config.backplaneRedisTemplate(factory);

        assertThat(template.getConnectionFactory()).isEqualTo(factory);
    }

    @Test
    void messageListenerContainer_isBackedByGivenConnectionFactory() {
        LettuceConnectionFactory factory = config.backplaneRedisConnectionFactory("localhost", 6379, "", false);

        RedisMessageListenerContainer container = config.backplaneRedisMessageListenerContainer(factory);

        assertThat(container).isNotNull();
    }
}
