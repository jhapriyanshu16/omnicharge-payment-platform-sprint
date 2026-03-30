package com.omnicharge.paymentservice.service.impl;

import com.omnicharge.paymentservice.dto.request.CreatePaymentRequest;
import com.omnicharge.paymentservice.dto.response.PaymentResponse;
import com.omnicharge.paymentservice.entity.PaymentStatus;
import com.omnicharge.paymentservice.entity.Transaction;
import com.omnicharge.paymentservice.exception.PaymentNotFoundException;
import com.omnicharge.paymentservice.messaging.PaymentEventPublisher;
import com.omnicharge.paymentservice.repository.TransactionRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUpRepositorySaveBehavior() {
        lenient().when(repository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            if (transaction.getId() == null) {
                transaction.setId(1L);
            }
            return transaction;
        });
    }

    @Test
    void shouldCreatePaymentSuccessfully() throws Exception {
        CreatePaymentRequest request = paymentRequest();
        PaymentIntent intent = Mockito.mock(PaymentIntent.class);
        when(intent.getId()).thenReturn("pi_123");

        try (MockedStatic<PaymentIntent> paymentIntentMock = Mockito.mockStatic(PaymentIntent.class)) {
            paymentIntentMock.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(intent);

            PaymentResponse result = paymentService.createPayment(request);

            assertEquals(1L, result.getTransactionId());
            assertEquals(request.getRechargeId(), result.getRechargeId());
            assertEquals(PaymentStatus.PENDING, result.getStatus());
            assertEquals("pi_123", result.getStripePaymentIntentId());

            ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
            verify(repository).save(transactionCaptor.capture());
            Transaction saved = transactionCaptor.getValue();
            assertEquals(request.getRechargeId(), saved.getRechargeId());
            assertEquals(request.getCorrelationId(), saved.getCorrelationId());
            assertEquals(PaymentStatus.PENDING, saved.getStatus());
        }
    }

    @Test
    void shouldThrowRuntimeExceptionWhenStripeCreationFails() {
        CreatePaymentRequest request = paymentRequest();

        try (MockedStatic<PaymentIntent> paymentIntentMock = Mockito.mockStatic(PaymentIntent.class)) {
            paymentIntentMock.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenThrow(new RuntimeException("Stripe down"));

            RuntimeException exception = assertThrows(RuntimeException.class, () -> paymentService.createPayment(request));

            assertEquals("Payment initialization failed", exception.getMessage());
        }
    }

    @Test
    void shouldVerifyPaymentSuccessfullyAndPublishEvent() {
        Transaction transaction = Transaction.builder()
                .id(1L)
                .rechargeId(100L)
                .amount(299.0)
                .userEmail("user@omnicharge.com")
                .operatorName("Jio")
                .planName("28 Days")
                .mobileNumber("9876543210")
                .correlationId("corr-123")
                .status(PaymentStatus.PENDING)
                .stripePaymentIntentId("pi_123")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(transaction));

        PaymentResponse result = paymentService.verifyPayment(1L);

        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        verify(paymentEventPublisher).publishSuccess(any());
        verify(repository).save(transaction);
    }

    @Test
    void shouldStillReturnSuccessWhenPublishingEventFails() {
        Transaction transaction = Transaction.builder()
                .id(1L)
                .rechargeId(100L)
                .amount(299.0)
                .userEmail("user@omnicharge.com")
                .operatorName("Jio")
                .planName("28 Days")
                .mobileNumber("9876543210")
                .correlationId("corr-123")
                .status(PaymentStatus.PENDING)
                .stripePaymentIntentId("pi_123")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(transaction));
        doThrow(new RuntimeException("Rabbit down")).when(paymentEventPublisher).publishSuccess(any());

        PaymentResponse result = paymentService.verifyPayment(1L);

        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        verify(repository).save(transaction);
    }

    @Test
    void shouldThrowExceptionWhenPaymentToVerifyIsMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.verifyPayment(99L));
    }

    @Test
    void shouldGetPaymentByIdSuccessfully() {
        Transaction transaction = Transaction.builder()
                .id(1L)
                .rechargeId(100L)
                .amount(299.0)
                .status(PaymentStatus.PENDING)
                .stripePaymentIntentId("pi_123")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(transaction));

        PaymentResponse result = paymentService.getById(1L);

        assertEquals(1L, result.getTransactionId());
        assertEquals(100L, result.getRechargeId());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenPaymentIsMissingById() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getById(99L));
    }

    @Test
    void shouldCreatePaymentWithCorrectAmountInSmallestUnit() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setRechargeId(100L);
        request.setAmount(99.99);
        request.setUserEmail("user@omnicharge.com");
        request.setOperatorName("Airtel");
        request.setPlanName("7 Days");
        request.setMobileNumber("9876543210");
        request.setCorrelationId("corr-456");

        PaymentIntent intent = Mockito.mock(PaymentIntent.class);
        when(intent.getId()).thenReturn("pi_456");

        try (MockedStatic<PaymentIntent> paymentIntentMock = Mockito.mockStatic(PaymentIntent.class)) {
            paymentIntentMock.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(intent);

            PaymentResponse result = paymentService.createPayment(request);

            ArgumentCaptor<PaymentIntentCreateParams> paramsCaptor = ArgumentCaptor.forClass(PaymentIntentCreateParams.class);
            paymentIntentMock.verify(() -> PaymentIntent.create(paramsCaptor.capture()));

            assertEquals(99.99, result.getAmount());
        }
    }

    @Test
    void shouldVerifyPaymentWithCorrectTransactionData() {
        Transaction transaction = Transaction.builder()
                .id(2L)
                .rechargeId(200L)
                .amount(199.0)
                .userEmail("john@omnicharge.com")
                .operatorName("Vodafone")
                .planName("14 Days")
                .mobileNumber("8765432109")
                .correlationId("corr-200")
                .status(PaymentStatus.PENDING)
                .stripePaymentIntentId("pi_789")
                .build();

        when(repository.findById(2L)).thenReturn(Optional.of(transaction));

        PaymentResponse result = paymentService.verifyPayment(2L);

        assertEquals(2L, result.getTransactionId());
        assertEquals(200L, result.getRechargeId());
        assertEquals(199.0, result.getAmount());
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        assertEquals("pi_789", result.getStripePaymentIntentId());
    }

    @Test
    void shouldGetPaymentWithAllDetails() {
        Transaction transaction = Transaction.builder()
                .id(3L)
                .rechargeId(300L)
                .amount(499.0)
                .userEmail("test@omnicharge.com")
                .operatorName("Jio")
                .planName("60 Days")
                .mobileNumber("7654321098")
                .correlationId("corr-300")
                .status(PaymentStatus.SUCCESS)
                .stripePaymentIntentId("pi_999")
                .build();

        when(repository.findById(3L)).thenReturn(Optional.of(transaction));

        PaymentResponse result = paymentService.getById(3L);

        assertEquals(3L, result.getTransactionId());
        assertEquals(300L, result.getRechargeId());
        assertEquals(499.0, result.getAmount());
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        assertEquals("pi_999", result.getStripePaymentIntentId());
    }

    @Test
    void shouldThrowCorrectExceptionWhenPaymentNotFoundForVerification() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        PaymentNotFoundException exception = assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.verifyPayment(999L)
        );

        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
    void shouldThrowCorrectExceptionWhenPaymentNotFoundById() {
        when(repository.findById(888L)).thenReturn(Optional.empty());

        PaymentNotFoundException exception = assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.getById(888L)
        );

        assertEquals("Transaction not found", exception.getMessage());
    }

    private CreatePaymentRequest paymentRequest() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setRechargeId(100L);
        request.setAmount(299.0);
        request.setUserEmail("user@omnicharge.com");
        request.setOperatorName("Jio");
        request.setPlanName("28 Days");
        request.setMobileNumber("9876543210");
        request.setCorrelationId("corr-123");
        return request;
    }
}
