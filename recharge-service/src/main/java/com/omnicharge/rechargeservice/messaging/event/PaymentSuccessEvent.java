package com.omnicharge.rechargeservice.messaging.event;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
