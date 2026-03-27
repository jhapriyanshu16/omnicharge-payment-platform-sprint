package com.omnicharge.rechargeservice.dto.response;

import com.omnicharge.rechargeservice.entity.RechargeStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeResponse {

    private Long rechargeId;
    private String mobileNumber;
    private Long operatorId;
    private Long planId;
    private Double amount;
    private RechargeStatus status;
    private LocalDateTime createdAt;
}