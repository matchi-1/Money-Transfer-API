package org.springpractice.moneytransferapi.dto;

import lombok.Data;
import org.springpractice.moneytransferapi.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String description;
    private TransactionStatus status;
    private String senderEmail;
    private String receiverEmail;
    private LocalDateTime createdAt;
}

