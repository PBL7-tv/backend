package com.pbl7.payment_service.controller;


import com.pbl7.payment_service.entity.Transaction;
import com.pbl7.payment_service.service.impl.TransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionController {

    TransactionService transactionService;


    @GetMapping()
    public ResponseEntity<List<Transaction>> getAllTransaction()  {

        List<Transaction> transactions = transactionService.getAllTransactions();

        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }
}
