package org.springpractice.moneytransferapi.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

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

    private String status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // ManyToOne = Each Transaction is associated with one User (the sender).
    // But a User can be associated with many transactions.

    // JoinColumn = Use the database column sender_id in the transactions table
    // to store the foreign key that links to users.id

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;


    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;
}
