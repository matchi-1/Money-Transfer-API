package org.springpractice.moneytransferapi.service.gateway;

import java.util.concurrent.TimeoutException;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springpractice.moneytransferapi.dto.TransactionRequestEvent;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springpractice.moneytransferapi.util.TransactionResponseRegistry;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionGatewayService {
    private final TransactionResponseRegistry registry;
    private final KafkaTemplate<String, TransactionRequestEvent> kafkaTemplate;

    public TransactionGatewayService(
            @Qualifier("transactionRequestKafkaTemplate") KafkaTemplate<String, TransactionRequestEvent> kafkaTemplate,
            TransactionResponseRegistry registry
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.registry = registry;
    }

    public TransactionResponseEvent initiateTransaction(Long senderId, Long receiverId, BigDecimal amount, String description) {
        String requestId = UUID.randomUUID().toString();
        TransactionRequestEvent request = new TransactionRequestEvent(requestId, senderId, receiverId, amount, description);

        CompletableFuture<TransactionResponseEvent> future = registry.register(requestId);
        kafkaTemplate.send("transaction-requests", request);

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            registry.remove(requestId);
            return new TransactionResponseEvent(requestId, TransactionStatus.FAILED, "Timed out waiting for response");
        } catch (Exception e) {
            registry.remove(requestId);
            return new TransactionResponseEvent(requestId, TransactionStatus.FAILED, "Internal error: " + e.getMessage());
        }
    }
}

