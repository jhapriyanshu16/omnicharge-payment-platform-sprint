package com.omnicharge.rechargeservice.dto.response;

import lombok.Data;

@Data
public class PaymentResponse {

    private Long transactionId;
    private Long rechargeId;
    private Double amount;
    private String status;
}