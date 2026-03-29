package com.omnicharge.notification.consumer;

import com.omnicharge.notification.config.RabbitMQConfig;
import com.omnicharge.notification.dto.PaymentSuccessEvent;
import com.omnicharge.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessEventConsumer {

    private final EmailService emailService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${omnicharge.notification.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consume(PaymentSuccessEvent event, Message message) {
        long retryCount = MessageRetryHelper.getDeathCount(message, RabbitMQConfig.NOTIFICATION_QUEUE);

        log.info("[correlationId={}] Received payment success event for rechargeId={} and email={} | retryCount={}",
                event.getCorrelationId(), event.getRechargeId(), event.getUserEmail(), retryCount);

        try {
            emailService.sendRechargeSuccessEmail(event);
            log.info("[correlationId={}] Email processing completed for rechargeId={} | retryCount={}",
                    event.getCorrelationId(), event.getRechargeId(), retryCount);
        } catch (Exception ex) {
            if (retryCount >= maxRetryAttempts) {
                log.error("[correlationId={}] Max retry limit reached for rechargeId={} | retryCount={} | sending to DLQ",
                        event.getCorrelationId(), event.getRechargeId(), retryCount, ex);
                publishToDlq(event);
                return;
            }

            long nextRetryAttempt = retryCount + 1;
            log.warn("[correlationId={}] Email processing failed for rechargeId={} | retryAttempt={} of {} | sending to retry queue",
                    event.getCorrelationId(), event.getRechargeId(), nextRetryAttempt, maxRetryAttempts, ex);
            throw new AmqpRejectAndDontRequeueException("Email delivery failed. Routing message to retry queue.", ex);
        }
    }

    private void publishToDlq(PaymentSuccessEvent event) {
        log.error("[correlationId={}] Publishing rechargeId={} to DLQ={}",
                event.getCorrelationId(), event.getRechargeId(), RabbitMQConfig.NOTIFICATION_DLQ);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_DLQ_ROUTING_KEY,
                event
        );
    }
}
