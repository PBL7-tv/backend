package com.pbl7.payment_service.repository;

import com.pbl7.payment_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
