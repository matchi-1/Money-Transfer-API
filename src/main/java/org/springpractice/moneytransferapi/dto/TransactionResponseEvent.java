package org.springpractice.moneytransferapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springpractice.moneytransferapi.enums.TransactionStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseEvent {
    @NotBlank
    private String requestId;

    @NotNull
    private TransactionStatus status;

    private String message;
}