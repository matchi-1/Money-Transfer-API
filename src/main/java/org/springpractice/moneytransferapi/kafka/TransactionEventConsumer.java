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
        if (txnId == null) {
            System.out.println("Transaction with null ID skipped.");
            return;
        }

        String key = PROCESSED_PREFIX + txnId;

        // atomically set the key ONLY if it does not already exist.
        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(key, "true", Duration.ofMinutes(10));

        // if 'wasSet' is false, it means the key already existed.
        if (Boolean.FALSE.equals(wasSet)) {
            System.out.println("Duplicate transaction skipped: " + txnId);
            return;
        }

        // if we reach here, we are the first to process this event.
        System.out.println("Processing transaction event: " + eventDTO);
    }

}
