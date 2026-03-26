package com.omnicharge.operatorservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {

    private Long id;
    private Double price;
    private Integer validity;
    private Double data;
    private Double talktime;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createdAt;
}