package org.springpractice.moneytransferapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springpractice.moneytransferapi.dto.TransactionRequest;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.service.TransactionService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.transfer(
                request.getSenderID(),
                request.getReceiverID(),
                request.getAmount(),
                request.getDescription()));
    }






}
