package org.springpractice.moneytransferapi.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.entity.User;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springpractice.moneytransferapi.repository.TransactionRepo;
import org.springpractice.moneytransferapi.repository.UserRepo;
import org.springpractice.moneytransferapi.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepo transactionRepo;
    private final UserRepo userRepo;
    //private final KafkaTemplate<String, Transaction> kafkaTemplate;

    @Autowired
    public TransactionServiceImpl(TransactionRepo transactionRepo,
                                  UserRepo userRepo) { // ,KafkaTemplate<String, Transaction> kafkaTemplate
        this.transactionRepo = transactionRepo;
        this.userRepo = userRepo;
        //this.kafkaTemplate = kafkaTemplate;
    }

    @Override public Transaction transfer(Long senderID, Long receiverID, BigDecimal amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setStatus(TransactionStatus.PENDING);

        try {
            // check if sender and receiver ID exists
            User sender =  userRepo.findById(senderID)
                    .orElseThrow(() -> new IllegalArgumentException("Sender not found with id: " + senderID));
            transaction.setSender(sender);
            User receiver = userRepo.findById(receiverID)
                    .orElseThrow(() -> new IllegalArgumentException("Receiver not found with id: " + receiverID));
            transaction.setReceiver(receiver);

            // check sender balance if it's enough to make the transaction
            if (sender.getBalance().compareTo(amount) < 0) { // returns -1 if balance is less than amount
                throw new IllegalArgumentException("Insufficient balance. Available: " + sender.getBalance() + ", Required: " + amount);
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Transfer amount must be greater than 0.");
            }

            // update balance (subtract from sender, add to receiver)
            sender.setBalance(sender.getBalance().subtract(amount));
            receiver.setBalance(receiver.getBalance().add(amount));

            // save updates
            userRepo.save(sender);
            userRepo.save(receiver);

            // update status to success
            transaction.setStatus(TransactionStatus.SUCCESS);

        } catch(IllegalArgumentException e){
            transaction.setStatus(TransactionStatus.FAILED);
            throw e;

        } finally {
            transactionRepo.save(transaction);
            // kafkaTemplate.send("transaction-events", transaction); // async Kafka event
        }

        return transaction;
    }

    @Override public List<Transaction> getAllTransactions() {
        return transactionRepo.findAll();
    }

    @Override public Transaction getTransactionById(Long id) {
        return transactionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id));
    }

    @Override public List<Transaction> getTransactionsBySender(Long senderID) {
        return transactionRepo.findBySenderId(senderID);
    }

    @Override public List<Transaction> getTransactionsByReceiver(Long receiverID) {
        return transactionRepo.findByReceiverId(receiverID);
    }

}

