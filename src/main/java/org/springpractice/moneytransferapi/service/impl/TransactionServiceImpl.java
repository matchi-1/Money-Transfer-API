package org.springpractice.moneytransferapi.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.entity.User;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springpractice.moneytransferapi.repository.TransactionRepo;
import org.springpractice.moneytransferapi.repository.UserRepo;
import org.springpractice.moneytransferapi.service.notification.EmailService;
import org.springpractice.moneytransferapi.service.TransactionService;
import org.springpractice.moneytransferapi.service.fallback.FallbackTransactionLogger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepo transactionRepo;
    private final UserRepo userRepo;
    private final FallbackTransactionLogger fallbackLogger;


    private final EmailService emailService;

    public TransactionServiceImpl(TransactionRepo transactionRepo,
                                  UserRepo userRepo,
                                  FallbackTransactionLogger fallbackLogger,
                                  EmailService emailService) {
        this.transactionRepo = transactionRepo;
        this.userRepo = userRepo;
        this.fallbackLogger = fallbackLogger;
        this.emailService = emailService;
    }

    @Override
    public Transaction transfer(Long senderID, Long receiverID, BigDecimal amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setStatus(TransactionStatus.PENDING);

        try {
            User sender = userRepo.findById(senderID)
                    .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
            transaction.setSender(sender);

            User receiver = userRepo.findById(receiverID)
                    .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));
            transaction.setReceiver(receiver);

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                transaction.setStatus(TransactionStatus.FAILED);
                fallbackLogger.logFailure(transaction);
                throw new IllegalArgumentException("Amount must be greater than 0");
            }

            if (sender.getBalance().compareTo(amount) < 0) {
                transaction.setStatus(TransactionStatus.FAILED);
                fallbackLogger.logFailure(transaction);
                throw new IllegalArgumentException("Insufficient balance. Current balance: " + sender.getBalance() + ". Required balance: " + amount);
            }

            sender.setBalance(sender.getBalance().subtract(amount));
            receiver.setBalance(receiver.getBalance().add(amount));
            userRepo.save(sender);
            userRepo.save(receiver);

            transaction.setStatus(TransactionStatus.SUCCESS);

            try {
                emailService.sendTransactionNotification(
                        sender.getEmail(),
                        "Money Sent - MoneyTransferAPI",
                        "<p>Hi " + sender.getFirstName() + "!<br>" +
                                "You sent ₱" + amount + " to " + receiver.getFirstName() + ".</p>"
                );
                System.out.println("EMAIL RE:SENDING MONEY sent to: " +  sender.getEmail());
            } catch (Exception e) {
                System.err.println("Failed to send email to sender: " + e.getMessage());
            }

            try {
                emailService.sendTransactionNotification(
                        receiver.getEmail(),
                        "Money Received - MoneyTransferAPI",
                        "<p>Hi " + receiver.getFirstName() + "!<br>" +
                                "You received ₱" + amount + " from " + sender.getFirstName() + ".</p>"
                );
                System.out.println("EMAIL RE:RECEIVING MONEY sent to: " +  receiver.getEmail());
            } catch (Exception e) {
                System.err.println("Failed to send email to receiver: " + e.getMessage());
            }

            return transactionRepo.save(transaction);

        } catch (Exception e) {
            if (transaction.getId() == null && transaction.getStatus() == TransactionStatus.PENDING) {
                transaction.setStatus(TransactionStatus.FAILED);
                fallbackLogger.logFailure(transaction);
            }
            throw e;
        }
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

