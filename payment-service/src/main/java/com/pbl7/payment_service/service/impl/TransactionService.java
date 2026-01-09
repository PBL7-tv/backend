package com.pbl7.payment_service.service.impl;



import com.pbl7.payment_service.dto.OrderDTO;
import com.pbl7.payment_service.entity.Transaction;

import java.util.List;

public interface TransactionService {
    Transaction createTransaction(OrderDTO order);
    List<Transaction> getAllTransactions();
}
