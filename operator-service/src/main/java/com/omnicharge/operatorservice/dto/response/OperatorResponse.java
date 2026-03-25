package com.omnicharge.operatorservice.dto.response;


import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorResponse {

    private Long id;
    private String name;
    private String circle;
    private LocalDateTime createdAt;
}
