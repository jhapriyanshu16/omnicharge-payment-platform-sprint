package com.omnicharge.notification.consumer;

import com.omnicharge.notification.config.RabbitMQConfig;
import com.omnicharge.notification.dto.PaymentSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationDlqConsumer {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_DLQ)
    public void consume(PaymentSuccessEvent event) {
        log.error("[correlationId={}] Permanent notification failure received in DLQ for rechargeId={} and email={}",
                event.getCorrelationId(), event.getRechargeId(), event.getUserEmail());
    }
}
