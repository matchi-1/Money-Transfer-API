package org.springpractice.moneytransferapi.service;

import org.springpractice.moneytransferapi.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    Transaction transfer(Long senderID, Long receiverID, BigDecimal amount, String description);

    List<Transaction> getAllTransactions();

    Transaction getTransactionById(Long id);

    List<Transaction> getTransactionsBySender(Long senderID);

    List<Transaction> getTransactionsByReceiver(Long receiverID);
}
