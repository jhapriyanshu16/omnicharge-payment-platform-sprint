package com.omnicharge.rechargeservice.dto.response;

import lombok.Data;

@Data
public class PlanResponse {

    private Long id;
    private Double price;
    private Integer validity;
    private Double data;
    private Double talktime;
    private Long operatorId;
    private String operatorName;
}