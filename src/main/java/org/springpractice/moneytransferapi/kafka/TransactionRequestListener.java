package org.springpractice.moneytransferapi.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springpractice.moneytransferapi.dto.TransactionRequestEvent;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springpractice.moneytransferapi.service.TransactionService;

@Service
public class TransactionRequestListener {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private KafkaTemplate<String, TransactionResponseEvent> kafkaTemplate;

    @KafkaListener(topics = "transaction-requests", groupId = "money-transfer")
    public void listen(TransactionRequestEvent event) {
        TransactionResponseEvent response = new TransactionResponseEvent();
        response.setRequestId(event.getRequestId());

        try {
            transactionService.transfer(
                    event.getSenderID(),
                    event.getReceiverID(),
                    event.getAmount(),
                    event.getDescription()
            );
            response.setStatus(TransactionStatus.SUCCESS);
        } catch (Exception e) {
            response.setStatus(TransactionStatus.FAILED);
            response.setMessage(e.getMessage());
        }

        kafkaTemplate.send("transaction-responses", response);
    }
}