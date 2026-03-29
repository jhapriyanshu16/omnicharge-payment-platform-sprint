package com.omnicharge.paymentservice.messaging;

import com.omnicharge.paymentservice.config.RabbitMQConfig;
import com.omnicharge.paymentservice.messaging.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishSuccess(PaymentSuccessEvent event) {
        log.info("[correlationId={}] Publishing payment success event for rechargeId={}",
                event.getCorrelationId(), event.getRechargeId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.PAYMENT_SUCCESS_ROUTING_KEY,
                event
        );
    }
}
