package org.springpractice.moneytransferapi.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springpractice.moneytransferapi.dto.TransactionRequest;
import org.springpractice.moneytransferapi.entity.Transaction;
import org.springpractice.moneytransferapi.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Validated
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // add transactions
    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.transfer(
                request.getSenderID(),
                request.getReceiverID(),
                request.getAmount(),
                request.getDescription()));
    }

    // GET all transactions
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    // GET user by ID
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping("/sender/{id}")
    public ResponseEntity<List<Transaction>> getBySender(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(transactionService.getTransactionsBySender(id));
    }

    @GetMapping("/receiver/{id}")
    public ResponseEntity<List<Transaction>> getByReceiver(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(transactionService.getTransactionsByReceiver(id));
    }

}
