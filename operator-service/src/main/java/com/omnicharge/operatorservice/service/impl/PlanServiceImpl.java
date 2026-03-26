package com.omnicharge.operatorservice.service.impl;

import com.omnicharge.operatorservice.dto.request.CreatePlanRequest;
import com.omnicharge.operatorservice.dto.response.PlanResponse;
import com.omnicharge.operatorservice.entity.Operator;
import com.omnicharge.operatorservice.entity.Plan;
import com.omnicharge.operatorservice.exception.OperatorNotFoundException;
import com.omnicharge.operatorservice.exception.PlanAlreadyExistsException;
import com.omnicharge.operatorservice.exception.OperatorNotFoundException;
import com.omnicharge.operatorservice.exception.PlanNotFoundException;
import com.omnicharge.operatorservice.repository.OperatorRepository;
import com.omnicharge.operatorservice.repository.PlanRepository;
import com.omnicharge.operatorservice.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final OperatorRepository operatorRepository;

    @Override
    public PlanResponse create(CreatePlanRequest request) {

        log.info("Creating plan for operator {}", request.getOperatorId());

        Operator operator = operatorRepository.findById(request.getOperatorId())
                .orElseThrow(() -> new OperatorNotFoundException("Operator not found"));

        if(planRepository.existsByOperatorIdAndPrice(request.getOperatorId(), request.getPrice())){
            throw new PlanAlreadyExistsException("Plan already exists with same price");
        }

        Plan plan = Plan.builder()
                .price(request.getPrice())
                .validity(request.getValidity())
                .data(request.getData())
                .talktime(request.getTalktime())
                .operator(operator)
                .build();

        Plan saved = planRepository.save(plan);

        return map(saved);
    }

    @Override
    public List<PlanResponse> getPlansByOperator(Long operatorId) {

        return planRepository.findByOperatorId(operatorId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public PlanResponse getById(Long id) {

        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new PlanNotFoundException("Plan not found with id: " + id));

        return map(plan);
    }

    @Override
    public void delete(Long id) {

        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new PlanNotFoundException("Plan not found with id: " + id));

        planRepository.delete(plan);
    }

    private PlanResponse map(Plan plan){

        return PlanResponse.builder()
                .id(plan.getId())
                .price(plan.getPrice())
                .validity(plan.getValidity())
                .data(plan.getData())
                .talktime(plan.getTalktime())
                .operatorId(plan.getOperator().getId())
                .operatorName(plan.getOperator().getName())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}