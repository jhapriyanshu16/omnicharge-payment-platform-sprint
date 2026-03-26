package com.omnicharge.operatorservice.service;

import com.omnicharge.operatorservice.dto.request.CreatePlanRequest;
import com.omnicharge.operatorservice.dto.response.PlanResponse;

import java.util.List;

public interface PlanService {

    PlanResponse create(CreatePlanRequest request);

    List<PlanResponse> getPlansByOperator(Long operatorId);

    PlanResponse getById(Long id);

    void delete(Long id);
}