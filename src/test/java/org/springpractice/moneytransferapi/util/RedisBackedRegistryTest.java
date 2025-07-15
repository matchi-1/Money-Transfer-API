package org.springpractice.moneytransferapi.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springpractice.moneytransferapi.dto.TransactionResponseEvent;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
public class RedisBackedRegistryTest {

    @Container
    static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @Autowired
    private TransactionResponseRegistry registry;

    @Test
    void testEndToEndRegistry() throws Exception {
        String requestId = UUID.randomUUID().toString();
        TransactionResponseEvent expected = new TransactionResponseEvent(requestId, TransactionStatus.SUCCESS, "OK");

        registry.complete(requestId, expected);

        TransactionResponseEvent result = registry.register(requestId).get(2, TimeUnit.SECONDS);

        assertEquals(expected.getStatus(), result.getStatus());
        assertEquals(expected.getMessage(), result.getMessage());
    }
}