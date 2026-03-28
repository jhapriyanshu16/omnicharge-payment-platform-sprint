package com.omnicharge.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_RETRY_QUEUE = "notification.retry.queue";
    public static final String NOTIFICATION_DLQ = "notification.dlq";
    public static final String NOTIFICATION_RETRY_ROUTING_KEY = "notification.retry";
    public static final String NOTIFICATION_DLQ_ROUTING_KEY = "notification.dlq";

    @Value("${omnicharge.notification.retry.delay-ms:10000}")
    private long retryDelayMs;

    @Bean
    public Queue notificationMainQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", PAYMENT_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", NOTIFICATION_RETRY_ROUTING_KEY);
        return new Queue(NOTIFICATION_QUEUE, true, false, false, arguments);
    }

    @Bean
    public Queue notificationRetryDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", retryDelayMs);
        arguments.put("x-dead-letter-exchange", PAYMENT_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", PAYMENT_SUCCESS_ROUTING_KEY);
        return new Queue(NOTIFICATION_RETRY_QUEUE, true, false, false, arguments);
    }

    @Bean
    public Queue notificationDeadLetterQueue() {
        return new Queue(NOTIFICATION_DLQ, true);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Binding notificationBinding(
            @Qualifier("notificationMainQueue") Queue notificationMainQueue,
            TopicExchange paymentExchange
    ) {
        return BindingBuilder.bind(notificationMainQueue)
                .to(paymentExchange)
                .with(PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding notificationRetryBinding(
            @Qualifier("notificationRetryDelayQueue") Queue notificationRetryDelayQueue,
            TopicExchange paymentExchange
    ) {
        return BindingBuilder.bind(notificationRetryDelayQueue)
                .to(paymentExchange)
                .with(NOTIFICATION_RETRY_ROUTING_KEY);
    }

    @Bean
    public Binding notificationDlqBinding(
            @Qualifier("notificationDeadLetterQueue") Queue notificationDeadLetterQueue,
            TopicExchange paymentExchange
    ) {
        return BindingBuilder.bind(notificationDeadLetterQueue)
                .to(paymentExchange)
                .with(NOTIFICATION_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jacksonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
