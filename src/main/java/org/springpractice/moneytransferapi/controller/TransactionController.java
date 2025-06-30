package org.springpractice.moneytransferapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springpractice.moneytransferapi.repository.TransactionRepo;
import org.springpractice.moneytransferapi.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public  TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }




}
