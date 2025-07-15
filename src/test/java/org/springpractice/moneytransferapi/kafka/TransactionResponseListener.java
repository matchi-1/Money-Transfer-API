package org.springpractice.moneytransferapi.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springpractice.moneytransferapi.util.TransactionResponseRegistry;

import static org.mockito.Mockito.*;

class TransactionResponseListenerTest {

    @Mock
    private TransactionResponseRegistry registry;

    @InjectMocks
    private TransactionResponseListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListen_shouldCompleteRegistryWithEvent() {
        // Given
        TransactionResponseEvent event = new TransactionResponseEvent();
        event.setRequestId("abc-123");
        event.setStatus(TransactionStatus.SUCCESS);
        event.setMessage("Processed");

        // When
        listener.listen(event);

        // Then
        verify(registry, times(1)).complete("abc-123", event);
    }
}
