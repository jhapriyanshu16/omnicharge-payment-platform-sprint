package com.omnicharge.rechargeservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RechargeRequest {

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number format")
    private String mobileNumber;

    @NotNull(message = "Operator ID is required")
    @Positive(message = "Operator ID must be a valid positive number")
    private Long operatorId;

    @NotNull(message = "Plan ID is required")
    @Positive(message = "Plan ID must be a valid positive number")
    private Long planId;

//    @NotNull(message = "Amount is required")
//    @Positive(message = "Amount must be greater than zero")
//    private Double amount;
}