package com.omnicharge.rechargeservice.client;

import com.omnicharge.rechargeservice.dto.response.ApiResponse;
import com.omnicharge.rechargeservice.dto.response.PaymentResponse;
import com.omnicharge.rechargeservice.dto.request.CreatePaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentClient {

    @PostMapping("/payments")
    ApiResponse<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request);
}