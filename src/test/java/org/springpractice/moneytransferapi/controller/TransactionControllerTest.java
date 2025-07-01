package org.springpractice.moneytransferapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springpractice.moneytransferapi.dto.TransactionRequest;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.enums.TransactionStatus;
import org.springpractice.moneytransferapi.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(TransactionControllerTest.MockedServiceConfig.class)
public class TransactionControllerTest {

    @TestConfiguration
    static class MockedServiceConfig {
        @Bean
        public TransactionService transactionService() {
            return Mockito.mock(TransactionService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Transaction sampleTransaction() {
        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setAmount(BigDecimal.valueOf(500));
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setCreatedAt(LocalDateTime.now());
        return tx;
    }

    @Test
    void testAddTransaction() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setSenderID(1L);
        request.setReceiverID(2L);
        request.setAmount(BigDecimal.valueOf(500));
        request.setDescription("Test transfer");

        Transaction tx = sampleTransaction();
        when(transactionService.transfer(any(), any(), any(), any())).thenReturn(tx);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(500));
    }

    @Test
    void testGetTransactionById() throws Exception {
        Transaction tx = sampleTransaction();
        when(transactionService.getTransactionById(1L)).thenReturn(tx);

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetAllTransactions() throws Exception {
        Transaction tx = sampleTransaction();
        when(transactionService.getAllTransactions()).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    void testGetBySenderId() throws Exception {
        Transaction tx = sampleTransaction();
        when(transactionService.getTransactionsBySender(1L)).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/transactions/sender/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(500));
    }

    @Test
    void testGetByReceiverId() throws Exception {
        Transaction tx = sampleTransaction();
        when(transactionService.getTransactionsByReceiver(2L)).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/transactions/receiver/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }
}
