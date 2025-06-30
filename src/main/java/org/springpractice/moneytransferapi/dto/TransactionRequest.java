package org.springpractice.moneytransferapi.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Data
public class TransactionRequest {
    @NotNull
    private Long senderID;

    @NotNull
    private Long receiverID;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private String description;
}

