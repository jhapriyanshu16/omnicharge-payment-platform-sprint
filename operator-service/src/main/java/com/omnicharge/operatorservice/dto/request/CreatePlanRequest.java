package com.omnicharge.operatorservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreatePlanRequest {

    @NotNull(message = "Operator ID is required")
    private Long operatorId;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private Double price;

    @NotNull(message = "Validity period is required")
    @Positive(message = "Validity must be at least 1 day")
    private Integer validity;

    @PositiveOrZero(message = "Data cannot be negative")
    private Double data;

    @PositiveOrZero(message = "Talktime cannot be negative")
    private Double talktime;
}