package org.springpractice.moneytransferapi.service.notification.impl;


import com.resend.Resend;
import com.resend.services.emails.model.SendEmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springpractice.moneytransferapi.service.notification.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private final Resend resend;

    public EmailServiceImpl(@Value("${resend.api.key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    @Override
    public void sendTransactionNotification(String to, String subject, String htmlBody) {
        SendEmailRequest request = SendEmailRequest.builder()
                .from("onboarding@resend.dev")
                .to(to)
                .subject(subject)
                .html(htmlBody)
                .build();

        try {
            resend.emails().send(request);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}
