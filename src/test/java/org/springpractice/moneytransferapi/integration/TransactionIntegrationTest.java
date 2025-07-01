package org.springpractice.moneytransferapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @BeforeEach
    void setup() {
        // Ensure two users exist before transfer
        User sender = new User();
        sender.setFirstName("Sender");
        sender.setEmail("sender@example.com");
        sender.setBalance(new BigDecimal("1000"));
        userRepo.save(sender);

        User receiver = new User();
        receiver.setFirstName("Receiver");
        receiver.setEmail("receiver@example.com");
        receiver.setBalance(new BigDecimal("100"));
        userRepo.save(receiver);
    }

    @Test
    void testTransferTransaction() throws Exception {
        User sender = userRepo.findAll().get(0);
        User receiver = userRepo.findAll().get(1);

        TransactionRequest request = new TransactionRequest();
        request.setSenderID(sender.getId());
        request.setReceiverID(receiver.getId());
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
