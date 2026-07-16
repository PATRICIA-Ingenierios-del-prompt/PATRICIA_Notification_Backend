package ingprompt.patricia.notification.infrastructure.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMQConfig {

    // ---------- Inbound exchanges (owned by other services) ----------
    public static final String PARCHE_EXCHANGE = "parche.events";          // Parches MS
    public static final String EVENT_EXCHANGE = "event.events";            // Events MS
    public static final String COMMUNICATION_EXCHANGE = "communication.events"; // Communication MS
    public static final String MATCHING_EXCHANGE = "patricia.matching";    // Matching MS (see messaging.exchange in its application.yml)
    public static final String LOGROS_EXCHANGE = "patricia.logros";        // User Backend (album achievements)

    // ---------- Routing keys we listen to ----------
    public static final String PARCHE_CREATED_ROUTING_KEY = "parche.created";              // #1
    public static final String EVENT_CREATED_ROUTING_KEY = "event.created";                // #2
    public static final String EVENT_LINKED_ROUTING_KEY = "event.linked.to.parche";        // #3
    public static final String MESSAGE_CREATED_ROUTING_KEY = "parche.message.created";      // #4 (no publisher yet, see README note)
    public static final String MATCH_CONFIRMED_ROUTING_KEY = "match.confirmado";           // #5
    public static final String LOGRO_DESBLOQUEADO_ROUTING_KEY = "logro.desbloqueado";      // #6

    // ---------- Our queues ----------
    public static final String PARCHE_CREATED_QUEUE = "notifications.parche.created.queue";
    public static final String EVENT_CREATED_QUEUE = "notifications.event.created.queue";
    public static final String EVENT_LINKED_QUEUE = "notifications.event.linked.queue";
    public static final String MESSAGE_CREATED_QUEUE = "notifications.message.created.queue";
    public static final String MATCH_CONFIRMED_QUEUE = "notifications.match.confirmed.queue";
    public static final String LOGRO_DESBLOQUEADO_QUEUE = "notifications.logro.desbloqueado.queue";

    // ---------- Dead-letter infrastructure ----------
    public static final String DLX_EXCHANGE = "notifications.dlx";
    private static final String DLQ_SUFFIX = ".dlq";

    // ---- Exchanges (declared idempotently; owners declare the same durable topic exchanges) ----

    @Bean
    public TopicExchange parcheExchange() {
        return new TopicExchange(PARCHE_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(EVENT_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange communicationExchange() {
        return new TopicExchange(COMMUNICATION_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange matchingExchange() {
        return new TopicExchange(MATCHING_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange logrosExchange() {
        return new TopicExchange(LOGROS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange notificationsDeadLetterExchange() {
        return new TopicExchange(DLX_EXCHANGE, true, false);
    }

    // ---- Inbound queues: each dead-letters to the DLX on basicNack(requeue=false) ----

    @Bean
    public Queue parcheCreatedQueue() {
        return dlqEnabled(PARCHE_CREATED_QUEUE);
    }

    @Bean
    public Queue eventCreatedQueue() {
        return dlqEnabled(EVENT_CREATED_QUEUE);
    }

    @Bean
    public Queue eventLinkedQueue() {
        return dlqEnabled(EVENT_LINKED_QUEUE);
    }

    @Bean
    public Queue messageCreatedQueue() {
        return dlqEnabled(MESSAGE_CREATED_QUEUE);
    }

    @Bean
    public Queue matchConfirmedQueue() {
        return dlqEnabled(MATCH_CONFIRMED_QUEUE);
    }

    @Bean
    public Queue logroDesbloqueadoQueue() {
        return dlqEnabled(LOGRO_DESBLOQUEADO_QUEUE);
    }

    // ---- Per-queue DLQs (parked poison messages live here for inspection) ----

    @Bean
    public Queue parcheCreatedDlq() {
        return QueueBuilder.durable(PARCHE_CREATED_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public Queue eventCreatedDlq() {
        return QueueBuilder.durable(EVENT_CREATED_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public Queue eventLinkedDlq() {
        return QueueBuilder.durable(EVENT_LINKED_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public Queue messageCreatedDlq() {
        return QueueBuilder.durable(MESSAGE_CREATED_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public Queue matchConfirmedDlq() {
        return QueueBuilder.durable(MATCH_CONFIRMED_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public Queue logroDesbloqueadoDlq() {
        return QueueBuilder.durable(LOGRO_DESBLOQUEADO_QUEUE + DLQ_SUFFIX).build();
    }

    // ---- Bindings: live queues to their owning exchange ----

    @Bean
    public Binding parcheCreatedBinding() {
        return BindingBuilder.bind(parcheCreatedQueue()).to(parcheExchange()).with(PARCHE_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding eventCreatedBinding() {
        return BindingBuilder.bind(eventCreatedQueue()).to(eventExchange()).with(EVENT_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding eventLinkedBinding() {
        return BindingBuilder.bind(eventLinkedQueue()).to(eventExchange()).with(EVENT_LINKED_ROUTING_KEY);
    }

    @Bean
    public Binding messageCreatedBinding() {
        return BindingBuilder.bind(messageCreatedQueue()).to(communicationExchange()).with(MESSAGE_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding matchConfirmedBinding() {
        return BindingBuilder.bind(matchConfirmedQueue()).to(matchingExchange()).with(MATCH_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    public Binding logroDesbloqueadoBinding() {
        return BindingBuilder.bind(logroDesbloqueadoQueue()).to(logrosExchange()).with(LOGRO_DESBLOQUEADO_ROUTING_KEY);
    }

    // ---- Bindings: DLQs to the DLX ----

    @Bean
    public Binding parcheCreatedDlqBinding() {
        return dlqBinding(PARCHE_CREATED_QUEUE, parcheCreatedDlq());
    }

    @Bean
    public Binding eventCreatedDlqBinding() {
        return dlqBinding(EVENT_CREATED_QUEUE, eventCreatedDlq());
    }

    @Bean
    public Binding eventLinkedDlqBinding() {
        return dlqBinding(EVENT_LINKED_QUEUE, eventLinkedDlq());
    }

    @Bean
    public Binding messageCreatedDlqBinding() {
        return dlqBinding(MESSAGE_CREATED_QUEUE, messageCreatedDlq());
    }

    @Bean
    public Binding matchConfirmedDlqBinding() {
        return dlqBinding(MATCH_CONFIRMED_QUEUE, matchConfirmedDlq());
    }

    @Bean
    public Binding logroDesbloqueadoDlqBinding() {
        return dlqBinding(LOGRO_DESBLOQUEADO_QUEUE, logroDesbloqueadoDlq());
    }

    // ---- Message conversion + listener container ----

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        // INFERRED: deserialize into the consumer's local DTO, ignore the producer's __TypeId__.
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(500L, 2.0, 10_000L)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            RetryOperationsInterceptor retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        // On exhaustion the message goes to its DLQ instead of looping forever.
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }

    // ---- helpers ----

    private Queue dlqEnabled(String queueName) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", queueName + DLQ_SUFFIX)
                .build();
    }

    private Binding dlqBinding(String queueName, Queue dlq) {
        return BindingBuilder.bind(dlq).to(notificationsDeadLetterExchange()).with(queueName + DLQ_SUFFIX);
    }
}
