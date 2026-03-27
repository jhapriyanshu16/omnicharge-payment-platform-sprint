package com.omnicharge.paymentservice.service;

import com.omnicharge.paymentservice.dto.request.CreatePaymentRequest;
import com.omnicharge.paymentservice.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    PaymentResponse getById(Long id);
}