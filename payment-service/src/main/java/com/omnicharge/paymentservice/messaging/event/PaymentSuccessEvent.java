package com.omnicharge.paymentservice.messaging.event;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentSuccessEvent {

    private Long rechargeId;
    private String status;
}
