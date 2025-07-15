package org.springpractice.moneytransferapi.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springpractice.moneytransferapi.dto.TransactionEventDTO;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.entity.User;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springpractice.moneytransferapi.repository.TransactionRepo;
import org.springpractice.moneytransferapi.repository.UserRepo;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    @Mock private TransactionRepo transactionRepo;
    @Mock private UserRepo userRepo;
    @Mock private KafkaTemplate<String, TransactionEventDTO> kafkaTemplate;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sender = new User();
        sender.setId(1L);
        sender.setEmail("sender@example.com");
        sender.setBalance(BigDecimal.valueOf(1000));

        receiver = new User();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");
        receiver.setBalance(BigDecimal.valueOf(500));

        // always assign ID to saved transactions to avoid NPE on getId()
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(123L);  // simulate DB ID assignment
            return tx;
        });
    }

//    @Test
//    void testSuccessfulTransfer() {
//        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
//        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
//
//        BigDecimal amount = BigDecimal.valueOf(200);
//        String desc = "Payment";
//
//        Transaction result = transactionService.transfer(1L, 2L, amount, desc);
//
//        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
//        assertEquals(sender, result.getSender());
//        assertEquals(receiver, result.getReceiver());
//        assertEquals(amount, result.getAmount());
//        assertEquals(desc, result.getDescription());
//
//        assertEquals(BigDecimal.valueOf(800), sender.getBalance());
//        assertEquals(BigDecimal.valueOf(700), receiver.getBalance());
//
//        verify(userRepo).save(sender);
//        verify(userRepo).save(receiver);
//        verify(transactionRepo).save(result);
//        verify(kafkaTemplate).send(eq("transaction-events"), eq("123"), any(TransactionEventDTO.class));
//    }

    @Test
    void testTransferFailsIfSenderNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transfer(1L, 2L, BigDecimal.TEN, "Test"));

        assertEquals("Sender not found with id: 1", ex.getMessage());
        verify(transactionRepo).save(any(Transaction.class));
        verify(kafkaTemplate).send(eq("transaction-events"), eq("123"), any(TransactionEventDTO.class));
    }

    @Test
    void testTransferFailsIfReceiverNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepo.findById(2L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transfer(1L, 2L, BigDecimal.TEN, "Test"));

        assertEquals("Receiver not found with id: 2", ex.getMessage());
        verify(transactionRepo).save(any(Transaction.class));
        verify(kafkaTemplate).send(eq("transaction-events"), eq("123"), any(TransactionEventDTO.class));
    }

    @Test
    void testTransferFailsIfInsufficientBalance() {
        sender.setBalance(BigDecimal.valueOf(50));

        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transfer(1L, 2L, BigDecimal.valueOf(100), "Test"));

        assertTrue(ex.getMessage().contains("Insufficient balance"));
        verify(transactionRepo).save(any(Transaction.class));
        verify(kafkaTemplate).send(eq("transaction-events"), eq("123"), any(TransactionEventDTO.class));
    }

    @Test
    void testTransferFailsIfNegativeAmount() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transfer(1L, 2L, BigDecimal.valueOf(-5), "Test"));

        assertEquals("Transfer amount must be greater than 0.", ex.getMessage());
        verify(transactionRepo).save(any(Transaction.class));
        verify(kafkaTemplate).send(eq("transaction-events"), eq("123"), any(TransactionEventDTO.class));
    }
}
