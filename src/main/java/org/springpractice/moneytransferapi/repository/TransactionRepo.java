package org.springpractice.moneytransferapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springpractice.moneytransferapi.entity.Transaction;


public interface TransactionRepo extends JpaRepository<Transaction,Long> {

}
