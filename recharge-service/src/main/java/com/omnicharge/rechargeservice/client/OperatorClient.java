package com.omnicharge.rechargeservice.client;

import com.omnicharge.rechargeservice.dto.response.PlanResponse;
import com.omnicharge.rechargeservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "OPERATOR-SERVICE")
public interface OperatorClient {

    @GetMapping("/plans/{id}")
    ApiResponse<PlanResponse> getPlanById(@PathVariable("id") Long id);
}