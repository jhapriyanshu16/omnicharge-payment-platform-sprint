package com.omnicharge.paymentservice.dto.response;

import com.omnicharge.paymentservice.entity.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long transactionId;
    private Long rechargeId;
    private Double amount;
    private PaymentStatus status;
    private String razorpayOrderId;
    private LocalDateTime createdAt;
}