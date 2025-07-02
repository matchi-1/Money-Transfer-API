package org.springpractice.moneytransferapi.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequestEvent {
    @NotBlank
    private String requestId;

    @NotNull
    @Min(1)
    private Long senderID;

    @NotNull
    @Min(1)
    private Long receiverID;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String description;
}
