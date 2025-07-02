package org.springpractice.moneytransferapi.service.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springpractice.moneytransferapi.dto.TransactionRequestEvent;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springpractice.moneytransferapi.util.TransactionResponseRegistry;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionGatewayService {

    @Autowired
    private KafkaTemplate<String, TransactionRequestEvent> kafkaTemplate;

    @Autowired
    private TransactionResponseRegistry registry;

    public TransactionResponseEvent initiateTransaction(Long senderId, Long receiverId, BigDecimal amount, String description) throws Exception {
        String requestId = UUID.randomUUID().toString();
        TransactionRequestEvent request = new TransactionRequestEvent(requestId, senderId, receiverId, amount, description);

        CompletableFuture<TransactionResponseEvent> future = registry.register(requestId);
        kafkaTemplate.send("transaction-requests", request);

        // wait with timeout
        return future.get(5, TimeUnit.SECONDS); // catch TimeoutException in production
    }
}
