package com.omnicharge.rechargeservice.service;

import com.omnicharge.rechargeservice.dto.request.RechargeRequest;
import com.omnicharge.rechargeservice.dto.response.RechargeResponse;

public interface RechargeService {

    RechargeResponse createRecharge(String userEmail, RechargeRequest request);

    RechargeResponse getById(Long id);
}