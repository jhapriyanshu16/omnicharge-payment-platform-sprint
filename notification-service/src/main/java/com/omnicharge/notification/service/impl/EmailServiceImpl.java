package com.omnicharge.notification.service.impl;

import com.omnicharge.notification.dto.PaymentSuccessEvent;
import com.omnicharge.notification.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.text.DecimalFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("0.00");

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendRechargeSuccessEmail(PaymentSuccessEvent event) {
        log.info("[correlationId={}] Preparing recharge success email for rechargeId={} to {}",
                event.getCorrelationId(), event.getRechargeId(), event.getUserEmail());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(event.getUserEmail());
            helper.setSubject("Recharge Successful");
            helper.setText(buildHtmlContent(event), true);

            log.info("[correlationId={}] Sending HTML recharge email for rechargeId={}",
                    event.getCorrelationId(), event.getRechargeId());
            mailSender.send(mimeMessage);
            log.info("[correlationId={}] Recharge success email sent to {} for rechargeId={}",
                    event.getCorrelationId(), event.getUserEmail(), event.getRechargeId());
        } catch (MessagingException | MailException ex) {
            log.error("[correlationId={}] Failed to send recharge success email for rechargeId={} to {}",
                    event.getCorrelationId(), event.getRechargeId(), event.getUserEmail(), ex);
            throw new IllegalStateException("Unable to send recharge success email", ex);
        }
    }

    private String buildHtmlContent(PaymentSuccessEvent event) {
        String amount = event.getAmount() == null ? "0.00" : AMOUNT_FORMAT.format(event.getAmount());
        String rechargeId = event.getRechargeId() == null ? "N/A" : String.valueOf(event.getRechargeId());

        return """
                <html>
                <body style="margin:0;padding:0;background-color:#f4f7fb;font-family:Arial,sans-serif;color:#1f2937;">
                    <div style="max-width:640px;margin:0 auto;padding:32px 16px;">
                        <div style="background:#0f172a;padding:24px 32px;border-radius:16px 16px 0 0;">
                            <h1 style="margin:0;color:#ffffff;font-size:28px;font-weight:700;">Recharge Successful</h1>
                            <p style="margin:8px 0 0;color:#cbd5e1;font-size:14px;">Your OmniCharge payment has been processed successfully.</p>
                        </div>
                        <div style="background:#ffffff;border:1px solid #dbe4f0;border-top:none;border-radius:0 0 16px 16px;padding:32px;">
                            <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:14px;padding:24px;">
                                <div style="margin-bottom:18px;padding-bottom:18px;border-bottom:1px solid #e5e7eb;">
                                    <span style="display:block;color:#64748b;font-size:12px;letter-spacing:0.08em;text-transform:uppercase;">Recharge ID</span>
                                    <span style="display:block;margin-top:6px;font-size:18px;font-weight:600;color:#0f172a;">%s</span>
                                </div>
                                <div style="margin-bottom:18px;padding-bottom:18px;border-bottom:1px solid #e5e7eb;">
                                    <span style="display:block;color:#64748b;font-size:12px;letter-spacing:0.08em;text-transform:uppercase;">Mobile Number</span>
                                    <span style="display:block;margin-top:6px;font-size:16px;font-weight:500;">%s</span>
                                </div>
                                <div style="margin-bottom:18px;padding-bottom:18px;border-bottom:1px solid #e5e7eb;">
                                    <span style="display:block;color:#64748b;font-size:12px;letter-spacing:0.08em;text-transform:uppercase;">Operator</span>
                                    <span style="display:block;margin-top:6px;font-size:16px;font-weight:500;">%s</span>
                                </div>
                                <div style="margin-bottom:18px;padding-bottom:18px;border-bottom:1px solid #e5e7eb;">
                                    <span style="display:block;color:#64748b;font-size:12px;letter-spacing:0.08em;text-transform:uppercase;">Plan</span>
                                    <span style="display:block;margin-top:6px;font-size:16px;font-weight:500;">%s</span>
                                </div>
                                <div>
                                    <span style="display:block;color:#64748b;font-size:12px;letter-spacing:0.08em;text-transform:uppercase;">Amount</span>
                                    <span style="display:block;margin-top:6px;font-size:24px;font-weight:700;color:#16a34a;">INR %s</span>
                                </div>
                            </div>
                            <p style="margin:24px 0 0;color:#64748b;font-size:13px;line-height:1.6;">
                                Status: <strong style="color:#0f172a;">%s</strong><br/>
                                Correlation ID: <span style="color:#334155;">%s</span>
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                rechargeId,
                safe(event.getMobileNumber()),
                safe(event.getOperatorName()),
                safe(event.getPlanName()),
                amount,
                safe(event.getStatus()),
                safe(event.getCorrelationId())
        );
    }

    private String safe(String value) {
        return HtmlUtils.htmlEscape(value == null ? "N/A" : value);
    }
}
