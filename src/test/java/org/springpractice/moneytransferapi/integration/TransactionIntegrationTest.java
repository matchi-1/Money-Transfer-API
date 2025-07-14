package org.springpractice.moneytransferapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springpractice.moneytransferapi.dto.TransactionRequest;
import org.springpractice.moneytransferapi.entity.User;
import org.springpractice.moneytransferapi.repository.UserRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class TransactionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update"); // or "create-drop" if preferred
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepo userRepo;

    private Long senderId;
    private Long receiverId;

    @BeforeEach
    void setup() {
        userRepo.deleteAll(); // deletes only from container

        User sender = new User();
        sender.setFirstName("Sender");
        sender.setLastName("Smith");
        sender.setEmail("sender@example.com");
        sender.setBalance(new BigDecimal("1000"));
        sender = userRepo.save(sender); // assign persisted entity
        senderId = sender.getId(); // store ID

        User receiver = new User();
        receiver.setFirstName("Receiver");
        receiver.setLastName("Johnson");
        receiver.setEmail("receiver@example.com");
        receiver.setBalance(new BigDecimal("100"));
        receiver = userRepo.save(receiver);
        receiverId = receiver.getId();
    }


    @Test
    void testTransferTransaction() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setSenderID(senderId);
        request.setReceiverID(receiverId);
        request.setAmount(BigDecimal.valueOf(200));
        request.setDescription("Payment");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(200));
    }
}
