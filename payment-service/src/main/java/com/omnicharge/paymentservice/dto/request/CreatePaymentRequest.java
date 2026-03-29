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

    @NotNull
    private String userEmail;

    @NotNull
    private String operatorName;

    @NotNull
    private String planName;

    @NotNull
    private String mobileNumber;

    @NotNull
    private String correlationId;
}
