package com.omnicharge.rechargeservice.messaging;

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

    @RabbitListener(queues = "payment.success.queue")
    public void handlePaymentSuccess(Long rechargeId) {

        log.info("Received payment success event for rechargeId {}", rechargeId);

        repository.findById(rechargeId).ifPresent(recharge -> {
            recharge.setStatus(RechargeStatus.SUCCESS);
            repository.save(recharge);

            log.info("Recharge updated to SUCCESS for id={}", rechargeId);
        });
    }
}