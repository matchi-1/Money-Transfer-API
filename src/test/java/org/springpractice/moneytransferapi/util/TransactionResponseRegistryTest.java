package org.springpractice.moneytransferapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class TransactionResponseRegistryTest {

    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        REDIS_CONTAINER.start();
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
    }

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private TransactionResponseRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TransactionResponseRegistry(redisTemplate, objectMapper);
    }

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testRegisterAndCompleteShouldReturnResponse() throws Exception {
        String requestId = "test-request-123";

        // start polling
        CompletableFuture<TransactionResponseEvent> future = registry.register(requestId);

        // simulate response after 500ms
        Thread.sleep(500);
        TransactionResponseEvent response = new TransactionResponseEvent(
                requestId,
                TransactionStatus.SUCCESS,
                "Test success"
        );
        registry.complete(requestId, response);

        TransactionResponseEvent result = future.get(5, TimeUnit.SECONDS);
        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertEquals("Test success", result.getMessage());
    }

    @Test
    void testPollShouldTimeoutIfNoResponse() {
        String requestId = "no-response-456";
        CompletableFuture<TransactionResponseEvent> future = registry.register(requestId);

        ExecutionException ex = assertThrows(ExecutionException.class, () -> {
            future.get(6, TimeUnit.SECONDS);
        });

        // check cause is RuntimeException with "Timed out"
        Throwable cause = ex.getCause();
        assertNotNull(cause);
        assertInstanceOf(RuntimeException.class, cause);
        assertTrue(cause.getMessage().contains("Timed out"));
    }


    @Test
    void testCompleteStoresJsonCorrectly() {
        String requestId = "store-test-789";
        TransactionResponseEvent response = new TransactionResponseEvent(
                requestId,
                TransactionStatus.FAILED,
                "Failure message"
        );

        registry.complete(requestId, response);

        String json = redisTemplate.opsForValue().get("response:txn-request:" + requestId);
        assertNotNull(json);
        assertTrue(json.contains("Failure message"));
    }
}
