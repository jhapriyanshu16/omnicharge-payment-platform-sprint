package com.omnicharge.operatorservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOperatorRequest {

    @NotBlank(message = "Operator name is required")
    private String name;

    @NotBlank(message = "Circle is required")
    private String circle;
}