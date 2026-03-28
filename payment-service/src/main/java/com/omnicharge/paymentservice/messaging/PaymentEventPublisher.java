package com.omnicharge.paymentservice.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishSuccess(Long rechargeId) {
        rabbitTemplate.convertAndSend(
                "payment.exchange",
                "payment.success",
                rechargeId
        );
    }
}