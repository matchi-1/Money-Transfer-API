package org.springpractice.moneytransferapi.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springpractice.moneytransferapi.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING) // save enum as string
    private TransactionStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // ManyToOne = each Transaction is associated with one User (the sender).
    // but a User can be associated with many transactions.

    // JoinColumn = use the database column sender_id in the transactions table
    // to store the foreign key that links to users.id

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;


    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;
}
