package org.springpractice.moneytransferapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public TransactionResponseRegistry(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<TransactionResponseEvent> register(String requestId) {
        // initial status as placeholder
        redisTemplate.opsForValue().set("txn:response:" + requestId, "PENDING", Duration.ofSeconds(10));
        return pollUntilComplete(requestId);
    }

    private CompletableFuture<TransactionResponseEvent> pollUntilComplete(String requestId) {
        CompletableFuture<TransactionResponseEvent> future = new CompletableFuture<>();

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                int maxAttempts = 50;
                for (int i = 0; i < maxAttempts; i++) {
                    try {
                        String json = redisTemplate.opsForValue().get("txn:response:" + requestId);
                        if (json != null && !"PENDING".equals(json)) {
                            try {
                                TransactionResponseEvent response = objectMapper.readValue(json, TransactionResponseEvent.class);
                                future.complete(response);
                                return;
                            } catch (Exception e) {
                                future.completeExceptionally(e);
                                return;
                            }
                        }
                        Thread.sleep(100); // poll every 100ms
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // re-interrupt
                        future.completeExceptionally(e);
                        return;
                    }
                }
                future.complete(new TransactionResponseEvent(requestId, TransactionStatus.FAILED, "Timed out"));
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }


        return future;
    }

    public void complete(String requestId, TransactionResponseEvent response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set("txn:response:" + requestId, json, Duration.ofSeconds(10));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize TransactionResponseEvent", e);
        }
    }

    public void remove(String requestId) {
        redisTemplate.delete("txn:response:" + requestId);
    }
}
