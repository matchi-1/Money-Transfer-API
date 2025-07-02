package org.springpractice.moneytransferapi.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springpractice.moneytransferapi.util.TransactionResponseRegistry;

@Service
public class TransactionResponseListener {

    @Autowired
    private TransactionResponseRegistry registry;

    @KafkaListener(topics = "transaction-responses", groupId = "money-transfer")
    public void listen(TransactionResponseEvent event) {
        registry.complete(event.getRequestId(), event);
    }
}
