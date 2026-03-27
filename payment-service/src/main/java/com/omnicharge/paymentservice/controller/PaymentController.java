package com.omnicharge.paymentservice.controller;

import com.omnicharge.paymentservice.dto.request.CreatePaymentRequest;
import com.omnicharge.paymentservice.dto.response.ApiResponse;
import com.omnicharge.paymentservice.dto.response.PaymentResponse;
import com.omnicharge.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping
    public ApiResponse<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request){

        PaymentResponse response = service.createPayment(request);

        return ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Payment initiated")
                .data(response)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> get(@PathVariable("id") Long id){

        return ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Payment details")
                .data(service.getById(id))
                .build();
    }
}