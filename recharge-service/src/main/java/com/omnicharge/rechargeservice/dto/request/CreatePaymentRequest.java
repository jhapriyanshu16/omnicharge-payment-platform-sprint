package com.omnicharge.rechargeservice.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    private Long rechargeId;
    private Double amount;
}