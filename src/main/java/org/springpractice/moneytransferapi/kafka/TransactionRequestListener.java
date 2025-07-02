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

    private final TransactionService transactionService;
    private final KafkaTemplate<String, TransactionResponseEvent> responseKafkaTemplate;

    @Autowired
    public TransactionRequestListener(TransactionService transactionService,
                                      KafkaTemplate<String, TransactionResponseEvent> responseKafkaTemplate) {
        this.transactionService = transactionService;
        this.responseKafkaTemplate = responseKafkaTemplate;
    }

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

        responseKafkaTemplate.send("transaction-responses", response);
    }
}
