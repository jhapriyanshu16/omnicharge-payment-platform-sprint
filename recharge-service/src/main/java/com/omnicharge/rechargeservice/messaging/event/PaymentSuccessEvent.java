package com.omnicharge.rechargeservice.messaging.event;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentSuccessEvent {

    private Long rechargeId;
    private String status;
}
