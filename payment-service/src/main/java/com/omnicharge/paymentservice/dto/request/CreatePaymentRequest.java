package com.omnicharge.paymentservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreatePaymentRequest {

    @NotNull
    private Long rechargeId;

    @NotNull
    @Positive
    private Double amount;
}