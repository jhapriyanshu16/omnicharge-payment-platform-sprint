package com.omnicharge.paymentservice.repository;

import com.omnicharge.paymentservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}