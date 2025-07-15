package org.springpractice.moneytransferapi.service.fallback;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.repository.TransactionRepo;

@Service
public class FallbackTransactionLogger {

    private final TransactionRepo transactionRepo;

    public FallbackTransactionLogger(TransactionRepo transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(Transaction txn) {
        transactionRepo.save(txn);
    }
}
