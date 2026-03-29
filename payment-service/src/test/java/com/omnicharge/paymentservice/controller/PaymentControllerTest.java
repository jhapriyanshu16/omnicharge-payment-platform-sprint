package com.omnicharge.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnicharge.paymentservice.dto.request.CreatePaymentRequest;
import com.omnicharge.paymentservice.dto.response.PaymentResponse;
import com.omnicharge.paymentservice.entity.PaymentStatus;
import com.omnicharge.paymentservice.exception.GlobalExceptionHandler;
import com.omnicharge.paymentservice.exception.PaymentNotFoundException;
import com.omnicharge.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    void shouldCreatePaymentSuccessfully() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setRechargeId(1L);
        request.setAmount(299.0);
        request.setUserEmail("user@example.com");
        request.setOperatorName("Jio");
        request.setPlanName("28 Days | 2 GB");
        request.setMobileNumber("9876543210");
        request.setCorrelationId("corr-123");

        PaymentResponse response = PaymentResponse.builder()
                .transactionId(1L)
                .rechargeId(1L)
                .amount(299.0)
                .status(PaymentStatus.PENDING)
                .stripePaymentIntentId("pi_123")
                .build();

        when(paymentService.createPayment(request)).thenReturn(response);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment initiated"))
                .andExpect(jsonPath("$.data.stripePaymentIntentId").value("pi_123"));
    }

    @Test
    void shouldReturnBadRequestWhenPaymentPayloadIsInvalid() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setRechargeId(null);
        request.setAmount(-1.0);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnPaymentDetails() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .transactionId(1L)
                .rechargeId(1L)
                .amount(299.0)
                .status(PaymentStatus.SUCCESS)
                .stripePaymentIntentId("pi_123")
                .build();

        when(paymentService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionId").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenPaymentDoesNotExist() throws Exception {
        when(paymentService.getById(99L)).thenThrow(new PaymentNotFoundException("Transaction not found"));

        mockMvc.perform(get("/payments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Transaction not found"));
    }

    @Test
    void shouldVerifyPaymentSuccessfully() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .transactionId(1L)
                .rechargeId(1L)
                .amount(299.0)
                .status(PaymentStatus.SUCCESS)
                .stripePaymentIntentId("pi_123")
                .build();

        when(paymentService.verifyPayment(1L)).thenReturn(response);

        mockMvc.perform(post("/payments/verify/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment successful"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }
}
