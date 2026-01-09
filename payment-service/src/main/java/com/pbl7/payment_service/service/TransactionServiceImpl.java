package com.pbl7.payment_service.service;


import com.pbl7.payment_service.dto.OrderDTO;
import com.pbl7.payment_service.entity.Transaction;
import com.pbl7.payment_service.repository.TransactionRepository;
import com.pbl7.payment_service.service.impl.TransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionServiceImpl implements TransactionService {

    TransactionRepository transactionRepository;

    @Override
    public Transaction createTransaction(OrderDTO order) {

        Transaction transaction = new Transaction();
        transaction.setCustomerId(order.getUserId());
        transaction.setOrderId(order.getId());
        transaction.setAmount(order.getTotalPrice());

        return transactionRepository.save(transaction);
    }


    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}
