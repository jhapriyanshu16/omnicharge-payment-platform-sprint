package com.omnicharge.paymentservice.service.impl;

import com.omnicharge.paymentservice.dto.request.CreatePaymentRequest;
import com.omnicharge.paymentservice.dto.response.PaymentResponse;
import com.omnicharge.paymentservice.entity.PaymentStatus;
import com.omnicharge.paymentservice.entity.Transaction;
import com.omnicharge.paymentservice.exception.PaymentNotFoundException;
import com.omnicharge.paymentservice.repository.TransactionRepository;
import com.omnicharge.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository repository;

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {

        log.info("Creating payment for rechargeId {}", request.getRechargeId());

        Transaction txn = Transaction.builder()
                .rechargeId(request.getRechargeId())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .build();

        Transaction saved = repository.save(txn);

        log.info("Payment created with transactionId {}", saved.getId());

        return map(saved);
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
                .razorpayOrderId(t.getRazorpayOrderId())
                .createdAt(t.getCreatedAt())
                .build();
    }
}