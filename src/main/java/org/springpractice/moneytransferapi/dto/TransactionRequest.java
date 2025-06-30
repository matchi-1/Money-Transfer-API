package org.springpractice.moneytransferapi.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionRequest {
    Long senderID;
    Long receiverID;
    BigDecimal amount;
    String description;
}
