package com.omnicharge.rechargeservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlanResponse {

    private Long id;
    @JsonProperty("price")
    private Double price;
    private Integer validity;
    private Double data;
    private Double talktime;
    private Long operatorId;
    private String operatorName;
}