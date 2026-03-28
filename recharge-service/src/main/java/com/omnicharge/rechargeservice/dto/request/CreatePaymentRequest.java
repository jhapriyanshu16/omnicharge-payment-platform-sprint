package com.omnicharge.rechargeservice.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    private Long rechargeId;
    private Double amount;
    private String userEmail;
    private String operatorName;
    private String planName;
    private String mobileNumber;
    private String correlationId;
}
