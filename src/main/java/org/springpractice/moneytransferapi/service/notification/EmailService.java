package org.springpractice.moneytransferapi.service.notification;

public interface EmailService {
    void sendTransactionNotification(String to, String subject, String htmlBody);
}
