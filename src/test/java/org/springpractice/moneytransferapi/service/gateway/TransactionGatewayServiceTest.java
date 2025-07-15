package org.springpractice.moneytransferapi.service.gateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springpractice.moneytransferapi.dto.TransactionRequestEvent;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springpractice.moneytransferapi.util.TransactionResponseRegistry;
import org.junit.jupiter.api.Timeout;

@ExtendWith(MockitoExtension.class)
class TransactionGatewayServiceTest {

    @Mock
    private KafkaTemplate<String, TransactionRequestEvent> kafkaTemplate;

    @Mock
    private TransactionResponseRegistry registry;

    @InjectMocks
    private TransactionGatewayService gatewayService;

    @Test
    void testInitiateTransaction_returnsResponseOnSuccess() throws Exception {
        String requestId = UUID.randomUUID().toString();
        Long senderId = 1L;
        Long receiverId = 2L;
        BigDecimal amount = BigDecimal.valueOf(150);
        String desc = "Test async";

        TransactionResponseEvent expectedResponse =
                new TransactionResponseEvent(requestId, TransactionStatus.SUCCESS, "Processed");

        CompletableFuture<TransactionResponseEvent> mockFuture = CompletableFuture.completedFuture(expectedResponse);

        when(registry.register(any())).thenReturn(mockFuture);

        TransactionResponseEvent result = gatewayService.initiateTransaction(senderId, receiverId, amount, desc);

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertEquals("Processed", result.getMessage());
        verify(kafkaTemplate).send(eq("transaction-requests"), any(TransactionRequestEvent.class));
    }

    @Test
    @Timeout(7) // prevents test from hanging forever
    void testInitiateTransaction_timesOut() {
        when(registry.register(any())).thenReturn(new CompletableFuture<>()); // never completes

        TransactionResponseEvent result = gatewayService.initiateTransaction(1L, 2L, BigDecimal.TEN, "desc");

        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertTrue(result.getMessage().toLowerCase().contains("timed out") || result.getMessage().toLowerCase().contains("internal"));
        verify(kafkaTemplate).send(eq("transaction-requests"), any(TransactionRequestEvent.class));
    }
}
