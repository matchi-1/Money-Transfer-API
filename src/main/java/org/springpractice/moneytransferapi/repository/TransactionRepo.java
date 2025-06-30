package org.springpractice.moneytransferapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springpractice.moneytransferapi.entity.Transaction;

import java.util.List;


public interface TransactionRepo extends JpaRepository<Transaction,Long> {
    List<Transaction> findBySenderId(Long senderId);
    List<Transaction> findByReceiverId(Long receiverId);
}
