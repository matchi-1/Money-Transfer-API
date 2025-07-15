package org.springpractice.moneytransferapi.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.entity.User;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springpractice.moneytransferapi.repository.TransactionRepo;
import org.springpractice.moneytransferapi.repository.UserRepo;
import org.springpractice.moneytransferapi.service.fallback.FallbackTransactionLogger;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepo transactionRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private FallbackTransactionLogger fallbackLogger;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private final Long senderId = 1L;
    private final Long receiverId = 2L;
    private final BigDecimal amount = new BigDecimal("100.00");
    private final String description = "Test transfer";

    private User sender;
    private User receiver;

    @BeforeEach
    void setupUsers() {
        sender = new User();
        sender.setId(senderId);
        sender.setBalance(new BigDecimal("200.00"));

        receiver = new User();
        receiver.setId(receiverId);
        receiver.setBalance(new BigDecimal("50.00"));
    }

    @Test
    void transfer_successfulTransaction_updatesBalancesAndSaves() {
        when(userRepo.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepo.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction txn = transactionService.transfer(senderId, receiverId, amount, description);

        assertEquals(TransactionStatus.SUCCESS, txn.getStatus());
        assertEquals(amount, txn.getAmount());
        assertEquals(description, txn.getDescription());

        assertEquals(new BigDecimal("100.00"), sender.getBalance());
        assertEquals(new BigDecimal("150.00"), receiver.getBalance());

        verify(userRepo).save(sender);
        verify(userRepo).save(receiver);
        verify(transactionRepo).save(any(Transaction.class));
        verify(fallbackLogger, never()).logFailure(any());
    }

    @Test
    void transfer_insufficientBalance_logsFailureAndThrows() {
        sender.setBalance(new BigDecimal("50.00"));

        when(userRepo.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepo.findById(receiverId)).thenReturn(Optional.of(receiver));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(senderId, receiverId, amount, description));

        assertTrue(ex.getMessage().contains("Insufficient balance"));
        verify(fallbackLogger).logFailure(any(Transaction.class));
        verify(transactionRepo, never()).save(any());
    }

    @Test
    void transfer_invalidAmount_logsFailureAndThrows() {
        BigDecimal invalidAmount = new BigDecimal("-10.00");

        when(userRepo.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepo.findById(receiverId)).thenReturn(Optional.of(receiver));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(senderId, receiverId, invalidAmount, description));

        assertTrue(ex.getMessage().contains("greater than 0"));
        verify(fallbackLogger).logFailure(any(Transaction.class));
        verify(transactionRepo, never()).save(any());
    }

    @Test
    void transfer_senderNotFound_throwsException() {
        when(userRepo.findById(senderId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(senderId, receiverId, amount, description));

        assertTrue(ex.getMessage().contains("Sender not found"));
        verify(fallbackLogger).logFailure(any(Transaction.class));
        verify(transactionRepo, never()).save(any());
    }

    @Test
    void transfer_receiverNotFound_throwsException() {
        when(userRepo.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepo.findById(receiverId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(senderId, receiverId, amount, description));

        assertTrue(ex.getMessage().contains("Receiver not found"));
        verify(fallbackLogger).logFailure(any(Transaction.class));
        verify(transactionRepo, never()).save(any());
    }

    @Test
    void getAllTransactions_delegatesToRepo() {
        transactionService.getAllTransactions();
        verify(transactionRepo).findAll();
    }

    @Test
    void getTransactionById_found_returnsTransaction() {
        Transaction txn = new Transaction();
        txn.setId(1L);
        when(transactionRepo.findById(1L)).thenReturn(Optional.of(txn));

        Transaction result = transactionService.getTransactionById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getTransactionById_notFound_throws() {
        when(transactionRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> transactionService.getTransactionById(1L));
    }

    @Test
    void getTransactionsBySender_delegatesToRepo() {
        transactionService.getTransactionsBySender(10L);
        verify(transactionRepo).findBySenderId(10L);
    }

    @Test
    void getTransactionsByReceiver_delegatesToRepo() {
        transactionService.getTransactionsByReceiver(20L);
        verify(transactionRepo).findByReceiverId(20L);
    }
}
