package com.omnicharge.paymentservice.service.impl;

import com.omnicharge.paymentservice.dto.request.CreatePaymentRequest;
import com.omnicharge.paymentservice.dto.response.PaymentResponse;
import com.omnicharge.paymentservice.entity.PaymentStatus;
import com.omnicharge.paymentservice.entity.Transaction;
import com.omnicharge.paymentservice.exception.PaymentNotFoundException;
import com.omnicharge.paymentservice.messaging.PaymentEventPublisher;
import com.omnicharge.paymentservice.messaging.event.PaymentSuccessEvent;
import com.omnicharge.paymentservice.repository.TransactionRepository;
import com.omnicharge.paymentservice.service.PaymentService;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository repository;
    private final PaymentEventPublisher paymentEventPublisher;

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {

        log.info("[correlationId={}] Creating Stripe payment intent for rechargeId={}",
                request.getCorrelationId(), request.getRechargeId());

        try {

            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount((long) (request.getAmount() * 100))
                            .setCurrency("inr")
                            .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Transaction txn = Transaction.builder()
                    .rechargeId(request.getRechargeId())
                    .amount(request.getAmount())
                    .userEmail(request.getUserEmail())
                    .operatorName(request.getOperatorName())
                    .planName(request.getPlanName())
                    .mobileNumber(request.getMobileNumber())
                    .correlationId(request.getCorrelationId())
                    .status(PaymentStatus.PENDING)
                    .stripePaymentIntentId(intent.getId())
                    .build();

            Transaction saved = repository.save(txn);

            log.info("[correlationId={}] Stripe PaymentIntent created | txnId={} | intentId={}",
                    saved.getCorrelationId(), saved.getId(), intent.getId());

            return map(saved);

        } catch (Exception ex) {

            log.error("[correlationId={}] Stripe payment creation failed for rechargeId {}",
                    request.getCorrelationId(), request.getRechargeId(), ex);

            throw new RuntimeException("Payment initialization failed");
        }
    }

    @Override
    public PaymentResponse verifyPayment(Long id) {

        Transaction txn = repository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Transaction not found"));

        txn.setStatus(PaymentStatus.SUCCESS);
        repository.save(txn);

        log.info("[correlationId={}] Payment SUCCESS | txnId={} | rechargeId={}",
                txn.getCorrelationId(), txn.getId(), txn.getRechargeId());

        try {
            PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                    .rechargeId(txn.getRechargeId())
                    .userEmail(txn.getUserEmail())
                    .operatorName(txn.getOperatorName())
                    .planName(txn.getPlanName())
                    .amount(txn.getAmount())
                    .mobileNumber(txn.getMobileNumber())
                    .status(txn.getStatus().name())
                    .correlationId(txn.getCorrelationId())
                    .build();

            paymentEventPublisher.publishSuccess(event);
        } catch (Exception ex) {
            log.error("[correlationId={}] Failed to publish payment success event for rechargeId={}",
                    txn.getCorrelationId(), txn.getRechargeId(), ex);
        }

        return map(txn);
    }

    @Override
    public PaymentResponse getById(Long id) {

        Transaction txn = repository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Transaction not found"));

        return map(txn);
    }

    private PaymentResponse map(Transaction t){

        return PaymentResponse.builder()
                .transactionId(t.getId())
                .rechargeId(t.getRechargeId())
                .amount(t.getAmount())
                .status(t.getStatus())
                .stripePaymentIntentId(t.getStripePaymentIntentId())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
