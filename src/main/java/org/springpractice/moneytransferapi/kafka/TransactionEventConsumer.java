package org.springpractice.moneytransferapi.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springpractice.moneytransferapi.dto.TransactionEventDTO;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.repository.TransactionRepo;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransactionEventConsumer {

    private final TransactionRepo transactionRepo;

    // just to simulate idempotent tracking in memory
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PROCESSED_PREFIX = "processed:";

    public TransactionEventConsumer(TransactionRepo transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    @KafkaListener(topics = "transaction-events", groupId = "money-transfer")
    public void consume(TransactionEventDTO eventDTO) {
        Long txnId = eventDTO.getId();
        String key = PROCESSED_PREFIX + txnId;

        Boolean alreadyProcessed = redisTemplate.hasKey(key);
        if (txnId == null || alreadyProcessed) {
            System.out.println("Duplicate or null transaction skipped: " + txnId);
            return;
        }

        // process normally
        System.out.println("Processing transaction event: " + eventDTO);
        redisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(10));  //  TTL
    }

}
