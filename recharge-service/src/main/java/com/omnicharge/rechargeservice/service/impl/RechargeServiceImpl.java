package com.omnicharge.rechargeservice.service.impl;

import com.omnicharge.rechargeservice.client.OperatorClient;
import com.omnicharge.rechargeservice.dto.request.RechargeRequest;
import com.omnicharge.rechargeservice.dto.response.ApiResponse;
import com.omnicharge.rechargeservice.dto.response.PlanResponse;
import com.omnicharge.rechargeservice.dto.response.RechargeResponse;
import com.omnicharge.rechargeservice.entity.Recharge;
import com.omnicharge.rechargeservice.entity.RechargeStatus;
import com.omnicharge.rechargeservice.exception.RechargeNotFoundException;
import com.omnicharge.rechargeservice.repository.RechargeRepository;
import com.omnicharge.rechargeservice.service.RechargeService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RechargeServiceImpl implements RechargeService {

    private final RechargeRepository repository;
    private final OperatorClient operatorClient;

    @Override
    @CircuitBreaker(name = "operatorService", fallbackMethod = "fallbackPlan")
    public RechargeResponse createRecharge(String userEmail, RechargeRequest request) {

        log.info("Recharge request received from user {}", userEmail);

        log.info("Fetching plan details from operator-service for planId {}", request.getPlanId());

        ApiResponse<PlanResponse> response =
                operatorClient.getPlanById(request.getPlanId());

        PlanResponse plan = response.getData();

        if(plan == null){
            throw new RuntimeException("Plan not found");
        }

        Recharge recharge = Recharge.builder()
                .userEmail(userEmail)
                .mobileNumber(request.getMobileNumber())
                .operatorId(request.getOperatorId())
                .planId(request.getPlanId())
                .amount(plan.getPrice())
                .status(RechargeStatus.PENDING)
                .build();

        Recharge saved = repository.save(recharge);

        log.info("Recharge created with id {}", saved.getId());

        return map(saved);
    }

    public RechargeResponse fallbackPlan(String userEmail, RechargeRequest request, Throwable ex){

        log.error("Operator service is down! Fallback triggered");

        Recharge recharge = Recharge.builder()
                .userEmail(userEmail)
                .mobileNumber(request.getMobileNumber())
                .operatorId(request.getOperatorId())
                .planId(request.getPlanId())
                .amount(0.0)
                .status(RechargeStatus.FAILED)
                .build();

        Recharge saved = repository.save(recharge);

        return map(saved);
    }

    @Override
    public RechargeResponse getById(Long id) {

        log.info("Fetching recharge {}", id);

        Recharge recharge = repository.findById(id)
                .orElseThrow(() -> new RechargeNotFoundException("Recharge not found"));

        return map(recharge);
    }

    private RechargeResponse map(Recharge r){

        return RechargeResponse.builder()
                .rechargeId(r.getId())
                .mobileNumber(r.getMobileNumber())
                .operatorId(r.getOperatorId())
                .planId(r.getPlanId())
                .amount(r.getAmount())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}