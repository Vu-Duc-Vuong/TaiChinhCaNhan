package com.example.taichinhcanhan.repository;

import com.example.taichinhcanhan.entity.Transaction;
import com.example.taichinhcanhan.entity.TransactionType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  List<Transaction> findByUserAccountIdOrderByTransactionDateDesc(Long userId);

  @Query("select transaction from Transaction transaction "
      + "join fetch transaction.category join fetch transaction.moneySource "
      + "where transaction.userAccount.id = :userId "
      + "order by transaction.transactionDate desc, transaction.id desc")
  List<Transaction> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

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

  boolean existsByCategoryIdAndTypeNot(Long categoryId, TransactionType type);

  Optional<Transaction> findByIdAndUserAccountId(Long id, Long userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select transaction from Transaction transaction where transaction.id = :transactionId and transaction.userAccount.id = :userId")
  Optional<Transaction> findOwnedByIdForUpdate(
      @Param("transactionId") Long transactionId,
      @Param("userId") Long userId);

  @Query("select transaction.moneySource.id, transaction.type, coalesce(sum(transaction.amount), 0) "
      + "from Transaction transaction where transaction.userAccount.id = :userId "
      + "group by transaction.moneySource.id, transaction.type")
  List<Object[]> summarizeBalancesByUserId(@Param("userId") Long userId);

  @Query("select coalesce(sum(transaction.amount), 0) from Transaction transaction "
      + "where transaction.userAccount.id = :userId "
      + "and transaction.moneySource.id = :sourceId "
      + "and transaction.type = :type "
      + "and (:excludedTransactionId is null or transaction.id <> :excludedTransactionId)")
  BigDecimal sumByUserAndSourceAndTypeExcludingTransaction(
      @Param("userId") Long userId,
      @Param("sourceId") Long sourceId,
      @Param("type") TransactionType type,
      @Param("excludedTransactionId") Long excludedTransactionId);

  @Query("""
      select coalesce(sum(transaction.amount), 0)
      from Transaction transaction
      where transaction.userAccount.id = :userId
        and transaction.type = com.example.taichinhcanhan.entity.TransactionType.EXPENSE
        and transaction.transactionDate between :startDate and :endDate
        and transaction.category.id = :categoryId
      """)
  BigDecimal sumExpenseForBudget(
      @Param("userId") Long userId,
      @Param("categoryId") Long categoryId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query("select transaction.type, coalesce(sum(transaction.amount), 0), count(transaction.id) "
      + "from Transaction transaction where transaction.userAccount.id = :userId "
      + "and transaction.transactionDate between :startDate and :endDate "
      + "group by transaction.type")
  List<Object[]> summarizeByType(
      @Param("userId") Long userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query("select function('YEAR', transaction.transactionDate), function('MONTH', transaction.transactionDate), "
      + "transaction.type, coalesce(sum(transaction.amount), 0) "
      + "from Transaction transaction where transaction.userAccount.id = :userId "
      + "and transaction.transactionDate between :startDate and :endDate "
      + "group by function('YEAR', transaction.transactionDate), function('MONTH', transaction.transactionDate), transaction.type "
      + "order by function('YEAR', transaction.transactionDate), function('MONTH', transaction.transactionDate)")
  List<Object[]> summarizeByMonth(
      @Param("userId") Long userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query("select function('YEAR', transaction.transactionDate), transaction.type, "
      + "coalesce(sum(transaction.amount), 0) "
      + "from Transaction transaction where transaction.userAccount.id = :userId "
      + "and transaction.transactionDate between :startDate and :endDate "
      + "group by function('YEAR', transaction.transactionDate), transaction.type "
      + "order by function('YEAR', transaction.transactionDate)")
  List<Object[]> summarizeByYear(
      @Param("userId") Long userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query("select transaction.category.name, coalesce(sum(transaction.amount), 0) "
      + "from Transaction transaction where transaction.userAccount.id = :userId "
      + "and transaction.type = com.example.taichinhcanhan.entity.TransactionType.EXPENSE "
      + "and transaction.transactionDate between :startDate and :endDate "
      + "group by transaction.category.id, transaction.category.name "
      + "order by sum(transaction.amount) desc")
  List<Object[]> summarizeExpenseByCategory(
      @Param("userId") Long userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query("select transaction.moneySource.name, transaction.type, coalesce(sum(transaction.amount), 0) "
      + "from Transaction transaction where transaction.userAccount.id = :userId "
      + "and transaction.transactionDate between :startDate and :endDate "
      + "group by transaction.moneySource.id, transaction.moneySource.name, transaction.type")
  List<Object[]> summarizeByMoneySource(
      @Param("userId") Long userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}