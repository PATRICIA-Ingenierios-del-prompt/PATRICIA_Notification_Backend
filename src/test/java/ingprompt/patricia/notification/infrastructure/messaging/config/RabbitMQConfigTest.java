package ingprompt.patricia.notification.infrastructure.messaging.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void exchanges_areDurableTopicsWithExpectedNames() {
        assertExchange(config.parcheExchange(), RabbitMQConfig.PARCHE_EXCHANGE);
        assertExchange(config.eventExchange(), RabbitMQConfig.EVENT_EXCHANGE);
        assertExchange(config.communicationExchange(), RabbitMQConfig.COMMUNICATION_EXCHANGE);
        assertExchange(config.matchingExchange(), RabbitMQConfig.MATCHING_EXCHANGE);
        assertExchange(config.logrosExchange(), RabbitMQConfig.LOGROS_EXCHANGE);
        assertExchange(config.notificationsDeadLetterExchange(), RabbitMQConfig.DLX_EXCHANGE);
    }

    private void assertExchange(TopicExchange exchange, String expectedName) {
        assertThat(exchange.getName()).isEqualTo(expectedName);
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
    }

    @Test
    void inboundQueues_areDurableAndDeadLetterToDlx() {
        Queue queue = config.parcheCreatedQueue();

        assertThat(queue.getName()).isEqualTo(RabbitMQConfig.PARCHE_CREATED_QUEUE);
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments())
                .containsEntry("x-dead-letter-exchange", RabbitMQConfig.DLX_EXCHANGE)
                .containsEntry("x-dead-letter-routing-key", RabbitMQConfig.PARCHE_CREATED_QUEUE + ".dlq");
    }

    @Test
    void allInboundQueues_haveDeadLetterArguments() {
        assertThat(config.eventCreatedQueue().getArguments()).containsKey("x-dead-letter-exchange");
        assertThat(config.eventLinkedQueue().getArguments()).containsKey("x-dead-letter-exchange");
        assertThat(config.messageCreatedQueue().getArguments()).containsKey("x-dead-letter-exchange");
        assertThat(config.matchConfirmedQueue().getArguments()).containsKey("x-dead-letter-exchange");
        assertThat(config.logroDesbloqueadoQueue().getArguments()).containsKey("x-dead-letter-exchange");
    }

    @Test
    void dlqQueues_areDurableWithSuffixedNames() {
        assertThat(config.parcheCreatedDlq().getName()).isEqualTo(RabbitMQConfig.PARCHE_CREATED_QUEUE + ".dlq");
        assertThat(config.eventCreatedDlq().getName()).isEqualTo(RabbitMQConfig.EVENT_CREATED_QUEUE + ".dlq");
        assertThat(config.eventLinkedDlq().getName()).isEqualTo(RabbitMQConfig.EVENT_LINKED_QUEUE + ".dlq");
        assertThat(config.messageCreatedDlq().getName()).isEqualTo(RabbitMQConfig.MESSAGE_CREATED_QUEUE + ".dlq");
        assertThat(config.matchConfirmedDlq().getName()).isEqualTo(RabbitMQConfig.MATCH_CONFIRMED_QUEUE + ".dlq");
        assertThat(config.logroDesbloqueadoDlq().getName()).isEqualTo(RabbitMQConfig.LOGRO_DESBLOQUEADO_QUEUE + ".dlq");
    }

    @Test
    void inboundBindings_bindQueueToOwningExchangeWithRoutingKey() {
        Binding binding = config.parcheCreatedBinding();

        assertThat(binding.getExchange()).isEqualTo(RabbitMQConfig.PARCHE_EXCHANGE);
        assertThat(binding.getDestination()).isEqualTo(RabbitMQConfig.PARCHE_CREATED_QUEUE);
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.PARCHE_CREATED_ROUTING_KEY);
    }

    @Test
    void allInboundBindings_useExpectedRoutingKeys() {
        assertThat(config.eventCreatedBinding().getRoutingKey()).isEqualTo(RabbitMQConfig.EVENT_CREATED_ROUTING_KEY);
        assertThat(config.eventLinkedBinding().getRoutingKey()).isEqualTo(RabbitMQConfig.EVENT_LINKED_ROUTING_KEY);
        assertThat(config.messageCreatedBinding().getRoutingKey()).isEqualTo(RabbitMQConfig.MESSAGE_CREATED_ROUTING_KEY);
        assertThat(config.matchConfirmedBinding().getRoutingKey()).isEqualTo(RabbitMQConfig.MATCH_CONFIRMED_ROUTING_KEY);
        assertThat(config.logroDesbloqueadoBinding().getRoutingKey()).isEqualTo(RabbitMQConfig.LOGRO_DESBLOQUEADO_ROUTING_KEY);
    }

    @Test
    void matchConfirmedBinding_bindsQueueToMatchingExchange() {
        Binding binding = config.matchConfirmedBinding();

        assertThat(binding.getExchange()).isEqualTo(RabbitMQConfig.MATCHING_EXCHANGE);
        assertThat(binding.getDestination()).isEqualTo(RabbitMQConfig.MATCH_CONFIRMED_QUEUE);
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.MATCH_CONFIRMED_ROUTING_KEY);
    }

    @Test
    void logroDesbloqueadoBinding_bindsQueueToLogrosExchange() {
        Binding binding = config.logroDesbloqueadoBinding();

        assertThat(binding.getExchange()).isEqualTo(RabbitMQConfig.LOGROS_EXCHANGE);
        assertThat(binding.getDestination()).isEqualTo(RabbitMQConfig.LOGRO_DESBLOQUEADO_QUEUE);
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.LOGRO_DESBLOQUEADO_ROUTING_KEY);
    }

    @Test
    void dlqBindings_bindDlqToDeadLetterExchange() {
        Binding binding = config.parcheCreatedDlqBinding();

        assertThat(binding.getExchange()).isEqualTo(RabbitMQConfig.DLX_EXCHANGE);
        assertThat(binding.getDestination()).isEqualTo(RabbitMQConfig.PARCHE_CREATED_QUEUE + ".dlq");
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.PARCHE_CREATED_QUEUE + ".dlq");
    }

    @Test
    void allDlqBindings_areWiredToDeadLetterExchange() {
        assertThat(config.eventCreatedDlqBinding().getExchange()).isEqualTo(RabbitMQConfig.DLX_EXCHANGE);
        assertThat(config.eventLinkedDlqBinding().getExchange()).isEqualTo(RabbitMQConfig.DLX_EXCHANGE);
        assertThat(config.messageCreatedDlqBinding().getExchange()).isEqualTo(RabbitMQConfig.DLX_EXCHANGE);
        assertThat(config.matchConfirmedDlqBinding().getExchange()).isEqualTo(RabbitMQConfig.DLX_EXCHANGE);
        assertThat(config.logroDesbloqueadoDlqBinding().getExchange()).isEqualTo(RabbitMQConfig.DLX_EXCHANGE);
    }

    @Test
    void inboundContracts_matchProducersExactly() {
        // Pins the literal wire values against what each owning service actually publishes,
        // so a producer-side rename doesn't silently desync from this consumer.
        assertThat(RabbitMQConfig.PARCHE_EXCHANGE).isEqualTo("parche.events");
        assertThat(RabbitMQConfig.EVENT_EXCHANGE).isEqualTo("event.events");
        assertThat(RabbitMQConfig.MATCHING_EXCHANGE).isEqualTo("patricia.matching");
        assertThat(RabbitMQConfig.MATCH_CONFIRMED_ROUTING_KEY).isEqualTo("match.confirmado");
        assertThat(RabbitMQConfig.LOGROS_EXCHANGE).isEqualTo("patricia.logros");
        assertThat(RabbitMQConfig.LOGRO_DESBLOQUEADO_ROUTING_KEY).isEqualTo("logro.desbloqueado");
    }

    @Test
    void jsonMessageConverter_usesInferredTypePrecedence() {
        MessageConverter converter = config.jsonMessageConverter();

        assertThat(converter).isInstanceOf(Jackson2JsonMessageConverter.class);
    }

    @Test
    void retryInterceptor_isConfigured() {
        RetryOperationsInterceptor interceptor = config.retryInterceptor();

        assertThat(interceptor).isNotNull();
    }

    @Test
    void listenerContainerFactory_wiresConnectionFactoryAndConverterAndRetry() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        MessageConverter converter = config.jsonMessageConverter();
        RetryOperationsInterceptor retryInterceptor = config.retryInterceptor();

        SimpleRabbitListenerContainerFactory factory =
                config.rabbitListenerContainerFactory(connectionFactory, converter, retryInterceptor);

        assertThat(factory).isNotNull();
    }
}
