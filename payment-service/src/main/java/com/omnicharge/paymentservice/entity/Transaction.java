package com.omnicharge.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long rechargeId;

    private Double amount;

    private String userEmail;

    private String operatorName;

    private String planName;

    private String mobileNumber;

    private String correlationId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String stripePaymentIntentId;
    private String stripePaymentId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
