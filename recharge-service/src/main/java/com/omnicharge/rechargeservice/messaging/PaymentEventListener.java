package com.omnicharge.rechargeservice.messaging;

import com.omnicharge.rechargeservice.config.RabbitMQConfig;
import com.omnicharge.rechargeservice.messaging.event.PaymentSuccessEvent;
import com.omnicharge.rechargeservice.entity.RechargeStatus;
import com.omnicharge.rechargeservice.repository.RechargeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final RechargeRepository repository;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_SUCCESS_RECHARGE_QUEUE)
    public void handlePaymentSuccess(PaymentSuccessEvent event) {

        log.info("[correlationId={}] Received payment success event for rechargeId={}",
                event.getCorrelationId(), event.getRechargeId());

        repository.findById(event.getRechargeId()).ifPresent(recharge -> {
            recharge.setStatus(RechargeStatus.SUCCESS);
            repository.save(recharge);

            log.info("[correlationId={}] Recharge updated to SUCCESS for id={}",
                    event.getCorrelationId(), event.getRechargeId());
        });
    }
}
