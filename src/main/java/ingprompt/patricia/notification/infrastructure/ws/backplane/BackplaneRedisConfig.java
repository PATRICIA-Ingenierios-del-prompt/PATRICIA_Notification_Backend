package ingprompt.patricia.notification.infrastructure.ws.backplane;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.util.StringUtils;

/**
 * Segunda conexion a Redis, apuntando al Cluster #2 (pub/sub backplane),
 * SEPARADA de la que Spring Boot autoconfigura para spring.data.redis.*
 * (esa sigue siendo el Cluster #1 / cache, usada solo por
 * {@code UnreadCounterRedisAdapter}).
 * <p>
 * Sin esto, un Deployment con 2+ replicas rompe el fan-out: el
 * {@code SimpleBroker} de Spring (ver StompConfig) vive en memoria de CADA
 * pod, asi que un mensaje publicado en la replica A nunca llega a un
 * cliente WebSocket conectado a la replica B. Este bean + el publisher +
 * el subscriber de abajo son el "sub de Redis del backplane" que faltaba:
 * cada pod publica el evento en el canal compartido y CADA pod (incluido
 * el que lo publico) lo re-emite a sus propios clientes locales via
 * {@code SimpMessagingTemplate}.
 */
@Configuration
public class BackplaneRedisConfig {

    @Bean
    public LettuceConnectionFactory backplaneRedisConnectionFactory(
            @Value("${backplane.redis.host}") String host,
            @Value("${backplane.redis.port}") int port,
            @Value("${backplane.redis.password:}") String password,
            @Value("${backplane.redis.ssl.enabled:false}") boolean sslEnabled
    ) {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(host, port);
        if (StringUtils.hasText(password)) {
            redisConfig.setPassword(password);
        }

        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder =
                LettuceClientConfiguration.builder();
        if (sslEnabled) {
            clientConfigBuilder.useSsl();
        }

        return new LettuceConnectionFactory(redisConfig, clientConfigBuilder.build());
    }

    @Bean
    public StringRedisTemplate backplaneRedisTemplate(
            LettuceConnectionFactory backplaneRedisConnectionFactory
    ) {
        return new StringRedisTemplate(backplaneRedisConnectionFactory);
    }

    @Bean
    public RedisMessageListenerContainer backplaneRedisMessageListenerContainer(
            LettuceConnectionFactory backplaneRedisConnectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(backplaneRedisConnectionFactory);
        return container;
    }
}
