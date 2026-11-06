package com.example.taichinhcanhan.repository;

import com.example.taichinhcanhan.entity.Transaction;
import com.example.taichinhcanhan.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

          List<Transaction> findByUserAccountIdOrderByTransactionDateDesc(Long userId);

          List<Transaction> findByUserAccountIdAndTypeOrderByTransactionDateDesc(
                              Long userId,
                              TransactionType type);

          List<Transaction> findByUserAccountIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                              Long userId,
                              LocalDate startDate,
                              LocalDate endDate);

          boolean existsByMoneySourceId(Long moneySourceId);
}