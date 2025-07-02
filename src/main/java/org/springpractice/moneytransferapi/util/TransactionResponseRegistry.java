package org.springpractice.moneytransferapi.util;

import org.springframework.stereotype.Component;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TransactionResponseRegistry {

    private final Map<String, CompletableFuture<TransactionResponseEvent>> futureMap = new ConcurrentHashMap<>();

    public CompletableFuture<TransactionResponseEvent> register(String requestId) {
        CompletableFuture<TransactionResponseEvent> future = new CompletableFuture<>();
        futureMap.put(requestId, future);
        return future;
    }

    public void complete(String requestId, TransactionResponseEvent response) {
        CompletableFuture<TransactionResponseEvent> future = futureMap.remove(requestId);
        if (future != null) {
            future.complete(response);
        }
    }
}
