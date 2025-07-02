package org.springpractice.moneytransferapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ProcessedRequest {
    @Id
    private String requestId;
}
