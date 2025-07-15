package org.springpractice.moneytransferapi.kafka;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;


import org.springpractice.moneytransferapi.dto.TransactionRequestEvent;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springpractice.moneytransferapi.service.TransactionService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionRequestListenerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private KafkaTemplate<String, TransactionResponseEvent> kafkaTemplate;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TransactionRequestListener listener;

    private TransactionRequestEvent sampleEvent;

    @BeforeEach
    void setup() {
        sampleEvent = new TransactionRequestEvent(
                "req-123",
                1L,
                2L,
                new BigDecimal("100.00"),
                "Test transfer"
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testProcessNewTransactionRequest_success() {
        // given the request is new (idempotency key doesn't exist)
        when(valueOperations.setIfAbsent(
                eq("idempotency:txn-request:req-123"),
                anyString(),
                eq(Duration.ofMinutes(10))
        )).thenReturn(true);

        // simulate successful transfer
        when(transactionService.transfer(any(), any(), any(), any()))
                .thenReturn(new Transaction());

        // when
        listener.listen(sampleEvent);

        // then
        verify(transactionService, times(1)).transfer(1L, 2L, new BigDecimal("100.00"), "Test transfer");
        ArgumentCaptor<TransactionResponseEvent> responseCaptor = ArgumentCaptor.forClass(TransactionResponseEvent.class);
        verify(kafkaTemplate).send(eq("transaction-responses"), responseCaptor.capture());

        TransactionResponseEvent response = responseCaptor.getValue();
        assertEquals("req-123", response.getRequestId());
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
    }

    @Test
    void testProcessNewTransactionRequest_failure() {
        // given the request is new
        when(valueOperations.setIfAbsent(any(), any(), any())).thenReturn(true);

        // simulate failure
        doThrow(new RuntimeException("Deliberate error")).when(transactionService).transfer(any(), any(), any(), any());

        // when
        listener.listen(sampleEvent);

        // then
        verify(transactionService).transfer(any(), any(), any(), any());
        ArgumentCaptor<TransactionResponseEvent> responseCaptor = ArgumentCaptor.forClass(TransactionResponseEvent.class);
        verify(kafkaTemplate).send(eq("transaction-responses"), responseCaptor.capture());

        TransactionResponseEvent response = responseCaptor.getValue();
        assertEquals(TransactionStatus.FAILED, response.getStatus());
        assertTrue(response.getMessage().contains("Deliberate error"));
    }

    @Test
    void testSkipDuplicateTransactionRequest() {
        // given the request already exists in Redis
        when(valueOperations.setIfAbsent(any(), any(), any())).thenReturn(false);

        // when
        listener.listen(sampleEvent);

        // then
        verify(transactionService, never()).transfer(any(), any(), any(), any());
        verify(kafkaTemplate, never()).send(anyString(), any(TransactionResponseEvent.class));
    }
}
