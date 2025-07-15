package org.springpractice.moneytransferapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springframework.stereotype.Component;
import org.springpractice.moneytransferapi.enums.TransactionStatus;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TransactionResponseRegistry {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    // shared executor for polling tasks
    private final ExecutorService pollingExecutor = Executors.newCachedThreadPool();

    public TransactionResponseRegistry(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<TransactionResponseEvent> register(String requestId) {
        // initial status as placeholder
        redisTemplate.opsForValue().set("txn:response:" + requestId, "PENDING", Duration.ofSeconds(20));
        return pollUntilComplete(requestId);
    }

    private CompletableFuture<TransactionResponseEvent> pollUntilComplete(String requestId) {
        // CompletableFuture manages the asynchronous task
        return CompletableFuture.supplyAsync(() -> {
            // poll for a result up to 50 times (50 * 100ms = 5 seconds)
            for (int i = 0; i < 50; i++) {
                String json = redisTemplate.opsForValue().get("txn:response:" + requestId);
                if (json != null && !"PENDING".equals(json)) {
                    try {
                        return objectMapper.readValue(json, TransactionResponseEvent.class);
                    } catch (JsonProcessingException e) {
                        // exception that CompletableFuture will catch
                        throw new RuntimeException("Failed to deserialize response", e);
                    }
                }
                try {
                    // wait before the next poll
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // preserve the interrupted status
                    throw new RuntimeException("Polling was interrupted", e);
                }
            }
            // if the loop finishes without a result, the request has timed out
            throw new RuntimeException("Timed out");
        }, pollingExecutor); // run on shared thread pool
    }

    public void complete(String requestId, TransactionResponseEvent response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set("txn:response:" + requestId, json, Duration.ofSeconds(60));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize TransactionResponseEvent", e);
        }
    }

    // cleanup method for the executor when the application shuts down
    @PreDestroy
    public void shutdown() {
        pollingExecutor.shutdown();
    }
}