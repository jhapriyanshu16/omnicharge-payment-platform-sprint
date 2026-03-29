package com.omnicharge.paymentservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue rechargePaymentSuccessQueue() {
        return new Queue("payment.success.recharge.queue", true);
    }

    @Bean
    public Queue notificationPaymentSuccessQueue() {
        return new Queue("payment.success.notification.queue", true);
    }

    @Bean
    public Binding rechargeBinding(
            @Qualifier("rechargePaymentSuccessQueue") Queue rechargePaymentSuccessQueue,
            TopicExchange paymentExchange
    ) {
        return BindingBuilder.bind(rechargePaymentSuccessQueue)
                .to(paymentExchange)
                .with(PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding(
            @Qualifier("notificationPaymentSuccessQueue") Queue notificationPaymentSuccessQueue,
            TopicExchange paymentExchange
    ) {
        return BindingBuilder.bind(notificationPaymentSuccessQueue)
                .to(paymentExchange)
                .with(PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonMessageConverter);
        return rabbitTemplate;
    }
}
