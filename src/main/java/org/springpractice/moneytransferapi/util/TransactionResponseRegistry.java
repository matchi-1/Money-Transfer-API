package org.springpractice.moneytransferapi.util;

import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TransactionResponseRegistry {

    private final Map<String, CompletableFuture<TransactionResponseEvent>> futures = new ConcurrentHashMap<>();

    public CompletableFuture<TransactionResponseEvent> register(String requestId) {
        CompletableFuture<TransactionResponseEvent> future = new CompletableFuture<>();
        futures.put(requestId, future);
        return future;
    }

    public void complete(String requestId, TransactionResponseEvent response) {
        CompletableFuture<TransactionResponseEvent> future = futures.remove(requestId);
        if (future != null) {
            future.complete(response);
        }
    }

    public void remove(String requestId) {
        futures.remove(requestId);
    }
}
