package org.springpractice.moneytransferapi.service.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springpractice.moneytransferapi.dto.TransactionRequestEvent;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springpractice.moneytransferapi.util.TransactionResponseRegistry;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionGatewayServiceTest {

    @Mock
    private KafkaTemplate<String, TransactionRequestEvent> kafkaTemplate;

    @Mock
    private TransactionResponseRegistry registry;

    @InjectMocks
    private TransactionGatewayService gatewayService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInitiateTransaction_returnsResponseOnSuccess() throws Exception {
        // arrange
        String requestId = UUID.randomUUID().toString();
        Long senderId = 1L;
        Long receiverId = 2L;
        BigDecimal amount = BigDecimal.valueOf(150);
        String desc = "Test async";

        // mock response future
        TransactionResponseEvent expectedResponse =
                new TransactionResponseEvent(requestId, TransactionStatus.SUCCESS, "Processed");

        CompletableFuture<TransactionResponseEvent> mockFuture = new CompletableFuture<>();
        mockFuture.complete(expectedResponse);

        // intercept request to capture UUID
        when(registry.register(any())).thenReturn(mockFuture);

        // act
        TransactionResponseEvent result = gatewayService.initiateTransaction(senderId, receiverId, amount, desc);

        // assert
        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertEquals("Processed", result.getMessage());

        verify(kafkaTemplate).send(eq("transaction-requests"), any(TransactionRequestEvent.class));
    }

    @Test
    void testInitiateTransaction_timesOut() {
        // arrange
        String requestId = UUID.randomUUID().toString();

        CompletableFuture<TransactionResponseEvent> hangingFuture = new CompletableFuture<>();
        when(registry.register(any())).thenReturn(hangingFuture);

        // act
        TransactionResponseEvent result = gatewayService.initiateTransaction(1L, 2L, BigDecimal.TEN, "desc");

        // assert: fallback when timeout or error occurs (will likely hit timeout block)
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertTrue(result.getMessage().toLowerCase().contains("timed out") || result.getMessage().toLowerCase().contains("internal"));
    }
}
