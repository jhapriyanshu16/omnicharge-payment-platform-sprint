package com.omnicharge.rechargeservice.service.impl;

import com.omnicharge.rechargeservice.client.OperatorClient;
import com.omnicharge.rechargeservice.client.PaymentClient;
import com.omnicharge.rechargeservice.dto.request.CreatePaymentRequest;
import com.omnicharge.rechargeservice.dto.request.RechargeRequest;
import com.omnicharge.rechargeservice.dto.response.ApiResponse;
import com.omnicharge.rechargeservice.dto.response.PaymentResponse;
import com.omnicharge.rechargeservice.dto.response.PlanResponse;
import com.omnicharge.rechargeservice.dto.response.RechargeResponse;
import com.omnicharge.rechargeservice.entity.Recharge;
import com.omnicharge.rechargeservice.entity.RechargeStatus;
import com.omnicharge.rechargeservice.exception.PlanNotFoundException;
import com.omnicharge.rechargeservice.exception.RechargeNotFoundException;
import com.omnicharge.rechargeservice.repository.RechargeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RechargeServiceImplTest {

    @Mock
    private RechargeRepository repository;

    @Mock
    private OperatorClient operatorClient;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private RechargeServiceImpl rechargeService;

    @BeforeEach
    void setUpSaveBehavior() {
        lenient().when(repository.save(any(Recharge.class))).thenAnswer(invocation -> {
            Recharge recharge = invocation.getArgument(0);
            if (recharge.getId() == null) {
                recharge.setId(1L);
            }
            return recharge;
        });
    }

    @Test
    void shouldCreateRechargeSuccessfullyWhenPaymentSucceeds() {
        RechargeRequest request = rechargeRequest();
        PlanResponse plan = validPlan();

        ApiResponse<PlanResponse> planApiResponse = ApiResponse.<PlanResponse>builder()
                .success(true)
                .data(plan)
                .build();

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setTransactionId(10L);
        paymentResponse.setRechargeId(1L);
        paymentResponse.setAmount(299.0);
        paymentResponse.setStatus("SUCCESS");

        ApiResponse<PaymentResponse> paymentApiResponse = ApiResponse.<PaymentResponse>builder()
                .success(true)
                .data(paymentResponse)
                .build();

        when(operatorClient.getPlanById(request.getPlanId())).thenReturn(planApiResponse);
        when(paymentClient.createPayment(any(CreatePaymentRequest.class))).thenReturn(paymentApiResponse);

        RechargeResponse result = rechargeService.createRecharge("user@omnicharge.com", request);

        assertEquals(RechargeStatus.SUCCESS, result.getStatus());
        assertEquals(299.0, result.getAmount());

        ArgumentCaptor<CreatePaymentRequest> paymentCaptor = ArgumentCaptor.forClass(CreatePaymentRequest.class);
        verify(paymentClient).createPayment(paymentCaptor.capture());
        CreatePaymentRequest capturedRequest = paymentCaptor.getValue();
        assertEquals(1L, capturedRequest.getRechargeId());
        assertEquals("user@omnicharge.com", capturedRequest.getUserEmail());
        assertEquals("Jio", capturedRequest.getOperatorName());
        assertEquals("28 Days | 2.0 GB | 100 Talktime", capturedRequest.getPlanName());
    }

    @Test
    void shouldThrowExceptionWhenPlanResponseIsMissing() {
        RechargeRequest request = rechargeRequest();
        when(operatorClient.getPlanById(request.getPlanId())).thenReturn(ApiResponse.<PlanResponse>builder().build());

        assertThrows(PlanNotFoundException.class, () -> rechargeService.createRecharge("user@omnicharge.com", request));
        verify(repository, never()).save(any(Recharge.class));
    }

    @Test
    void shouldFailRechargeWhenPlanDoesNotBelongToOperator() {
        RechargeRequest request = rechargeRequest();
        PlanResponse plan = validPlan();
        plan.setOperatorId(99L);

        when(operatorClient.getPlanById(request.getPlanId())).thenReturn(
                ApiResponse.<PlanResponse>builder().data(plan).build()
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> rechargeService.createRecharge("user@omnicharge.com", request)
        );

        assertEquals("Plan does not belong to operator", exception.getMessage());
        verify(repository, never()).save(any(Recharge.class));
    }

    @Test
    void shouldKeepRechargePendingWhenPaymentIsNotSuccessful() {
        RechargeRequest request = rechargeRequest();
        PlanResponse plan = validPlan();

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setStatus("PENDING");

        when(operatorClient.getPlanById(request.getPlanId())).thenReturn(
                ApiResponse.<PlanResponse>builder().data(plan).build()
        );
        when(paymentClient.createPayment(any(CreatePaymentRequest.class))).thenReturn(
                ApiResponse.<PaymentResponse>builder().data(paymentResponse).build()
        );

        RechargeResponse result = rechargeService.createRecharge("user@omnicharge.com", request);

        assertEquals(RechargeStatus.PENDING, result.getStatus());
    }

    @Test
    void shouldFailRechargeWhenPaymentServiceThrowsException() {
        RechargeRequest request = rechargeRequest();
        PlanResponse plan = validPlan();

        when(operatorClient.getPlanById(request.getPlanId())).thenReturn(
                ApiResponse.<PlanResponse>builder().data(plan).build()
        );
        when(paymentClient.createPayment(any(CreatePaymentRequest.class))).thenThrow(new RuntimeException("payment down"));

        RechargeResponse result = rechargeService.createRecharge("user@omnicharge.com", request);

        assertEquals(RechargeStatus.FAILED, result.getStatus());
        verify(repository, times(2)).save(any(Recharge.class));
    }

    @Test
    void shouldFallbackWhenOperatorServiceIsDown() {
        RechargeRequest request = rechargeRequest();

        RechargeResponse result = rechargeService.fallbackPlan("user@omnicharge.com", request, new RuntimeException("operator down"));

        assertEquals(RechargeStatus.FAILED, result.getStatus());
        assertEquals(0.0, result.getAmount());
    }

    @Test
    void shouldGetRechargeByIdSuccessfully() {
        Recharge recharge = Recharge.builder()
                .id(1L)
                .userEmail("user@omnicharge.com")
                .mobileNumber("9876543210")
                .operatorId(10L)
                .planId(100L)
                .amount(299.0)
                .status(RechargeStatus.SUCCESS)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(recharge));

        RechargeResponse result = rechargeService.getById(1L);

        assertEquals(1L, result.getRechargeId());
        assertEquals(RechargeStatus.SUCCESS, result.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenRechargeIsMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RechargeNotFoundException.class, () -> rechargeService.getById(99L));
    }

    @Test
    void shouldUpdateRechargeStatusSuccessfully() {
        Recharge recharge = Recharge.builder()
                .id(1L)
                .status(RechargeStatus.PENDING)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(recharge));

        rechargeService.updateStatus(1L, "success");

        assertEquals(RechargeStatus.SUCCESS, recharge.getStatus());
        verify(repository).save(recharge);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingMissingRecharge() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RechargeNotFoundException.class, () -> rechargeService.updateStatus(99L, "SUCCESS"));
        verify(repository, never()).save(any(Recharge.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithInvalidStatus() {
        Recharge recharge = Recharge.builder()
                .id(1L)
                .status(RechargeStatus.PENDING)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(recharge));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> rechargeService.updateStatus(1L, "unknown-status")
        );

        assertEquals("Invalid status update requested: unknown-status", exception.getMessage());
        verify(repository, never()).save(recharge);
    }

    private RechargeRequest rechargeRequest() {
        RechargeRequest request = new RechargeRequest();
        request.setMobileNumber("9876543210");
        request.setOperatorId(10L);
        request.setPlanId(100L);
        return request;
    }

    private PlanResponse validPlan() {
        PlanResponse plan = new PlanResponse();
        plan.setId(100L);
        plan.setPrice(299.0);
        plan.setValidity(28);
        plan.setData(2.0);
        plan.setTalktime(100.0);
        plan.setOperatorId(10L);
        plan.setOperatorName("Jio");
        return plan;
    }
}
