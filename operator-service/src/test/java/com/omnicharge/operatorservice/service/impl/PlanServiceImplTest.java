package com.omnicharge.operatorservice.service.impl;

import com.omnicharge.operatorservice.dto.request.CreatePlanRequest;
import com.omnicharge.operatorservice.dto.response.PlanResponse;
import com.omnicharge.operatorservice.entity.Operator;
import com.omnicharge.operatorservice.entity.Plan;
import com.omnicharge.operatorservice.exception.OperatorNotFoundException;
import com.omnicharge.operatorservice.exception.PlanAlreadyExistsException;
import com.omnicharge.operatorservice.exception.PlanNotFoundException;
import com.omnicharge.operatorservice.repository.OperatorRepository;
import com.omnicharge.operatorservice.repository.PlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceImplTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private OperatorRepository operatorRepository;

    @InjectMocks
    private PlanServiceImpl planService;

    @Test
    void shouldCreatePlanSuccessfully() {
        CreatePlanRequest request = new CreatePlanRequest();
        request.setOperatorId(10L);
        request.setPrice(299.0);
        request.setValidity(28);
        request.setData(2.0);
        request.setTalktime(100.0);

        Operator operator = Operator.builder().id(10L).name("Jio").circle("UP").build();
        Plan savedPlan = Plan.builder()
                .id(1L)
                .price(299.0)
                .validity(28)
                .data(2.0)
                .talktime(100.0)
                .operator(operator)
                .build();

        when(operatorRepository.findById(10L)).thenReturn(Optional.of(operator));
        when(planRepository.existsByOperatorIdAndPrice(10L, 299.0)).thenReturn(false);
        when(planRepository.save(any(Plan.class))).thenReturn(savedPlan);

        PlanResponse result = planService.create(request);

        assertEquals(savedPlan.getId(), result.getId());
        assertEquals(savedPlan.getPrice(), result.getPrice());
        assertEquals(operator.getId(), result.getOperatorId());
        assertEquals(operator.getName(), result.getOperatorName());
    }

    @Test
    void shouldThrowExceptionWhenCreatingPlanForMissingOperator() {
        CreatePlanRequest request = new CreatePlanRequest();
        request.setOperatorId(99L);

        when(operatorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OperatorNotFoundException.class, () -> planService.create(request));
        verify(planRepository, never()).save(any(Plan.class));
    }

    @Test
    void shouldThrowExceptionWhenPlanAlreadyExists() {
        CreatePlanRequest request = new CreatePlanRequest();
        request.setOperatorId(10L);
        request.setPrice(299.0);

        Operator operator = Operator.builder().id(10L).name("Jio").build();

        when(operatorRepository.findById(10L)).thenReturn(Optional.of(operator));
        when(planRepository.existsByOperatorIdAndPrice(10L, 299.0)).thenReturn(true);

        assertThrows(PlanAlreadyExistsException.class, () -> planService.create(request));
        verify(planRepository, never()).save(any(Plan.class));
    }

    @Test
    void shouldGetPlansByOperatorSuccessfully() {
        Operator operator = Operator.builder().id(10L).name("Jio").build();
        Plan first = Plan.builder().id(1L).price(199.0).validity(28).operator(operator).build();
        Plan second = Plan.builder().id(2L).price(299.0).validity(56).operator(operator).build();

        when(planRepository.findByOperatorId(10L)).thenReturn(List.of(first, second));

        List<PlanResponse> result = planService.getPlansByOperator(10L);

        assertEquals(2, result.size());
        assertEquals(List.of(199.0, 299.0), result.stream().map(PlanResponse::getPrice).toList());
    }

    @Test
    void shouldGetPlanByIdSuccessfully() {
        Operator operator = Operator.builder().id(10L).name("Jio").build();
        Plan plan = Plan.builder().id(1L).price(299.0).validity(28).operator(operator).build();
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));

        PlanResponse result = planService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals(299.0, result.getPrice());
    }

    @Test
    void shouldThrowExceptionWhenPlanNotFoundById() {
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PlanNotFoundException.class, () -> planService.getById(99L));
    }

    @Test
    void shouldDeletePlanSuccessfully() {
        Operator operator = Operator.builder().id(10L).name("Jio").build();
        Plan plan = Plan.builder().id(1L).price(299.0).validity(28).operator(operator).build();
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));

        planService.delete(1L);

        verify(planRepository).delete(plan);
    }

    @Test
    void shouldThrowExceptionWhenDeletingMissingPlan() {
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PlanNotFoundException.class, () -> planService.delete(99L));
        verify(planRepository, never()).delete(any(Plan.class));
    }
}
