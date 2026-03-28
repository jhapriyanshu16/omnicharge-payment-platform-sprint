package com.omnicharge.notification.consumer;

import com.omnicharge.notification.config.RabbitMQConfig;
import com.omnicharge.notification.dto.PaymentSuccessEvent;
import com.omnicharge.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessEventConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_SUCCESS_NOTIFICATION_QUEUE)
    public void consume(PaymentSuccessEvent event) {
        log.info("[correlationId={}] Received payment success event for rechargeId={} and email={}",
                event.getCorrelationId(), event.getRechargeId(), event.getUserEmail());
        emailService.sendRechargeSuccessEmail(event);
    }
}
