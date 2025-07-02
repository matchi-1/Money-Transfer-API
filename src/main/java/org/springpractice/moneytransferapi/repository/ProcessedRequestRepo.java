package org.springpractice.moneytransferapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springpractice.moneytransferapi.entity.ProcessedRequest;

public interface ProcessedRequestRepo extends JpaRepository<ProcessedRequest, String> {
    boolean existsByRequestId(String requestId);
}
