package com.omnicharge.rechargeservice.controller;

import com.omnicharge.rechargeservice.dto.request.RechargeRequest;
import com.omnicharge.rechargeservice.dto.response.ApiResponse;
import com.omnicharge.rechargeservice.dto.response.RechargeResponse;
import com.omnicharge.rechargeservice.service.RechargeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recharges")
@RequiredArgsConstructor
@Slf4j
public class RechargeController {

    private final RechargeService service;

    @PostMapping
    public ApiResponse<RechargeResponse> create(
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody RechargeRequest request){
        log.info("User Email from header: {}", userEmail);
        return ApiResponse.<RechargeResponse>builder()
                .success(true)
                .message("Recharge initiated")
                .data(service.createRecharge(userEmail, request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<RechargeResponse> get(@PathVariable("id") Long id){

        return ApiResponse.<RechargeResponse>builder()
                .success(true)
                .message("Recharge details")
                .data(service.getById(id))
                .build();
    }
}