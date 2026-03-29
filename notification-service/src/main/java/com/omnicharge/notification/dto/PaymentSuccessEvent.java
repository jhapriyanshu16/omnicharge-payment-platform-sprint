package com.omnicharge.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent {

    private Long rechargeId;
    private String userEmail;
    private String operatorName;
    private String planName;
    private Double amount;
    private String mobileNumber;
    private String status;
    private String correlationId;
}
