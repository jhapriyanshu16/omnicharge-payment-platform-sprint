package com.omnicharge.rechargeservice.service.impl;

import com.omnicharge.rechargeservice.dto.request.RechargeRequest;
import com.omnicharge.rechargeservice.dto.response.RechargeResponse;
import com.omnicharge.rechargeservice.entity.Recharge;
import com.omnicharge.rechargeservice.entity.RechargeStatus;
import com.omnicharge.rechargeservice.exception.RechargeNotFoundException;
import com.omnicharge.rechargeservice.repository.RechargeRepository;
import com.omnicharge.rechargeservice.service.RechargeService;
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

    @Override
    public RechargeResponse createRecharge(String userEmail, RechargeRequest request) {

        log.info("Recharge request received from user {}", userEmail);

        Recharge recharge = Recharge.builder()
                .userEmail(userEmail)
                .mobileNumber(request.getMobileNumber())
                .operatorId(request.getOperatorId())
                .planId(request.getPlanId())
                .amount(0.0)
                .status(RechargeStatus.PENDING)
                .build();

        Recharge saved = repository.save(recharge);

        log.info("Recharge created with id {}", saved.getId());

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