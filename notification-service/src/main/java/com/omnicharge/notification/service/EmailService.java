package com.omnicharge.notification.service;

import com.omnicharge.notification.dto.PaymentSuccessEvent;

public interface EmailService {

    void sendRechargeSuccessEmail(PaymentSuccessEvent event);
}
