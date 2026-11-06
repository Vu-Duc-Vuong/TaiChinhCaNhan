package com.example.taichinhcanhan.repository;

import com.example.taichinhcanhan.entity.Transaction;
import com.example.taichinhcanhan.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

          List<Transaction> findByUserAccountIdOrderByTransactionDateDesc(Long userId);

          List<Transaction> findByUserAccountIdAndTypeOrderByTransactionDateDesc(
                              Long userId,
                              TransactionType type);

          List<Transaction> findByUserAccountIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                              Long userId,
                              LocalDate startDate,
                              LocalDate endDate);

          @Query("""
                              select transaction from Transaction transaction
                              where transaction.userAccount.id = :userId
                                and (:keyword is null or :keyword = '' or lower(transaction.description) like lower(concat('%', :keyword, '%')))
                                and (:type is null or transaction.type = :type)
                                and (:moneySourceId is null or transaction.moneySource.id = :moneySourceId)
                                and (:categoryId is null or transaction.category.id = :categoryId)
                                and (:startDate is null or transaction.transactionDate >= :startDate)
                                and (:endDate is null or transaction.transactionDate <= :endDate)
                              order by transaction.transactionDate desc, transaction.id desc
                              """)
          List<Transaction> searchByUserId(
                              @Param("userId") Long userId,
                              @Param("keyword") String keyword,
                              @Param("type") TransactionType type,
                              @Param("moneySourceId") Long moneySourceId,
                              @Param("categoryId") Long categoryId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

          boolean existsByMoneySourceId(Long moneySourceId);

          boolean existsByCategoryId(Long categoryId);

          Optional<Transaction> findByIdAndUserAccountId(Long id, Long userId);
}