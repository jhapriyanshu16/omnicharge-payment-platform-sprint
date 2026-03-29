package com.omnicharge.operatorservice.controller;

import com.omnicharge.operatorservice.dto.request.CreatePlanRequest;
import com.omnicharge.operatorservice.dto.response.ApiResponse;
import com.omnicharge.operatorservice.dto.response.PlanResponse;
import com.omnicharge.operatorservice.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService service;

    @PostMapping
    public ApiResponse<PlanResponse> create(@Valid @RequestBody CreatePlanRequest request){

        return ApiResponse.<PlanResponse>builder()
                .success(true)
                .message("Plan created")
                .data(service.create(request))
                .build();
    }

    @GetMapping("/operator/{operatorId}")
    public ApiResponse<List<PlanResponse>> getByOperator(@PathVariable("operatorId") Long operatorId){

        return ApiResponse.<List<PlanResponse>>builder()
                .success(true)
                .message("Plans list")
                .data(service.getPlansByOperator(operatorId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PlanResponse> getById(@PathVariable("id") Long id){

        return ApiResponse.<PlanResponse>builder()
                .success(true)
                .message("Plan found")
                .data(service.getById(id))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable("id") Long id){

        service.delete(id);

        return ApiResponse.builder()
                .success(true)
                .message("Plan deleted")
                .build();
    }
}