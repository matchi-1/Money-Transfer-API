package org.springpractice.moneytransferapi.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springpractice.moneytransferapi.dto.TransactionRequestEvent;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springpractice.moneytransferapi.service.TransactionService;

import java.time.Duration;
import java.time.Instant;

@Service
public class TransactionRequestListener {

    private final TransactionService transactionService;
    private final KafkaTemplate<String, TransactionResponseEvent> responseKafkaTemplate;

    // inject Redis template for the idempotency check
    private final StringRedisTemplate redisTemplate;

    // redis key prefix
    private static final String REQUEST_ID_PREFIX = "idempotency:txn-request:";

    @Autowired
    public TransactionRequestListener(TransactionService transactionService,
                                      KafkaTemplate<String, TransactionResponseEvent> responseKafkaTemplate,
                                      StringRedisTemplate redisTemplate) {
        this.transactionService = transactionService;
        this.responseKafkaTemplate = responseKafkaTemplate;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "transaction-requests", groupId = "money-transfer")
    public void listen(TransactionRequestEvent event) {
        String requestId = event.getRequestId();
        String redisKey = REQUEST_ID_PREFIX + requestId;

        // --- IDEMPOTENCY CHECK ---
        // atomically set the key if it's absent. if it returns false, it was already there.
        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(redisKey, Instant.now().toString(), Duration.ofMinutes(10));

        if (Boolean.FALSE.equals(wasSet)) {
            System.out.println("Duplicate request skipped: " + requestId);
            return; // exit without processing
        }

        // --- process the transaction ---
        TransactionResponseEvent response = new TransactionResponseEvent();
        response.setRequestId(requestId);

        try {
            // the service now handles saving the final state (success or fail)
            transactionService.transfer(
                    event.getSenderID(),
                    event.getReceiverID(),
                    event.getAmount(),
                    event.getDescription()
            );
            response.setStatus(TransactionStatus.SUCCESS);
            response.setMessage("Transaction successful");
        } catch (Exception e) {
            response.setStatus(TransactionStatus.FAILED);
            response.setMessage(e.getMessage());
        }

        // send the final result to the response topic
        responseKafkaTemplate.send("transaction-responses", response);
    }
}