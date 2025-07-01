package org.springpractice.moneytransferapi.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.repository.TransactionRepo;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransactionEventConsumer {

    private final TransactionRepo transactionRepo;

    // simulate idempotent tracking in memory
    private final ConcurrentHashMap<Long, Boolean> processedTransactions = new ConcurrentHashMap<>();

    public TransactionEventConsumer(TransactionRepo transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    @KafkaListener(topics = "transaction-events", groupId = "money-transfer")
    public void consume(Transaction transaction) {
        Long txnId = transaction.getId();

        if (txnId == null || processedTransactions.containsKey(txnId)) {
            System.out.println("Duplicate or null transaction skipped: " + txnId);
            return;
        }

        // simulate processing logic
        System.out.println("Processing transaction: " + txnId);
        processedTransactions.put(txnId, true);
    }
}
