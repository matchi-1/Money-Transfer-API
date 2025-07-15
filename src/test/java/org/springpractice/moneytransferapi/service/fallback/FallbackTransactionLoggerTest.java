package org.springpractice.moneytransferapi.service.fallback;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springpractice.moneytransferapi.repository.TransactionRepo;

import java.math.BigDecimal;


@ExtendWith(MockitoExtension.class)
class FallbackTransactionLoggerTest {

    @Mock
    private TransactionRepo transactionRepo;

    @InjectMocks
    private FallbackTransactionLogger fallbackLogger;

    @Test
    void testLogFailure_savesTransaction() {
        Transaction failedTxn = new Transaction();
        failedTxn.setStatus(TransactionStatus.FAILED);
        failedTxn.setAmount(BigDecimal.valueOf(100));
        failedTxn.setDescription("Fallback failure");

        fallbackLogger.logFailure(failedTxn);

        verify(transactionRepo, times(1)).save(failedTxn);
    }
}
