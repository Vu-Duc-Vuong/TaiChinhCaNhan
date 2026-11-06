package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.TransactionRequest;
import com.example.taichinhcanhan.entity.Category;
import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.Transaction;
import com.example.taichinhcanhan.entity.TransactionType;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.CategoryRepository;
import com.example.taichinhcanhan.repository.MoneySourceRepository;
import com.example.taichinhcanhan.repository.TransactionRepository;
import com.example.taichinhcanhan.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
public class TransactionService {

          private final TransactionRepository transactionRepository;
          private final TransferRepository transferRepository;
          private final MoneySourceRepository moneySourceRepository;
          private final CategoryRepository categoryRepository;

          public TransactionService(TransactionRepository transactionRepository,
                              TransferRepository transferRepository,
                              MoneySourceRepository moneySourceRepository,
                              CategoryRepository categoryRepository) {
                    this.transactionRepository = transactionRepository;
                    this.transferRepository = transferRepository;
                    this.moneySourceRepository = moneySourceRepository;
                    this.categoryRepository = categoryRepository;
          }

          @Transactional(readOnly = true)
          public List<Transaction> search(UserAccount userAccount,
                              String keyword,
                              TransactionType type,
                              Long moneySourceId,
                              Long categoryId,
                              java.time.LocalDate startDate,
                              java.time.LocalDate endDate) {
                    String normalizedKeyword = keyword == null ? null : keyword.trim();
                    return transactionRepository.searchByUserId(
                                        userAccount.getId(), normalizedKeyword, type, moneySourceId, categoryId,
                                        startDate, endDate);
          }

          @Transactional
          public Transaction create(UserAccount userAccount, TransactionRequest request) {
                    MoneySource moneySource = findOwnedMoneySourceForUpdate(request.getMoneySourceId(), userAccount);
                    Category category = findOwnedCategory(request.getCategoryId(), userAccount);
                    validateCategoryType(request.getType(), category);
                    validateAvailableBalance(moneySource, userAccount, request.getType(), request.getAmount(), null);

                    Transaction transaction = new Transaction();
                    applyRequest(transaction, request, category, moneySource);
                    transaction.setUserAccount(userAccount);
                    return transactionRepository.save(transaction);
          }

          @Transactional(readOnly = true)
          public Transaction findOwnedById(Long id, UserAccount userAccount) {
                    return transactionRepository.findByIdAndUserAccountId(id, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch."));
          }

          @Transactional
          public Transaction update(Long id, UserAccount userAccount, TransactionRequest request) {
                    Transaction transaction = transactionRepository.findOwnedByIdForUpdate(id, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch."));
                    Map<Long, MoneySource> lockedSources = lockOwnedSources(
                                        List.of(transaction.getMoneySource().getId(), request.getMoneySourceId()),
                                        userAccount);
                    MoneySource moneySource = lockedSources.get(request.getMoneySourceId());
                    Category category = findOwnedCategory(request.getCategoryId(), userAccount);
                    validateCategoryType(request.getType(), category);
                    validateAvailableBalance(moneySource, userAccount, request.getType(), request.getAmount(), id);
                    applyRequest(transaction, request, category, moneySource);
                    return transactionRepository.save(transaction);
          }

          @Transactional
          public void delete(Long id, UserAccount userAccount) {
                    Transaction transaction = transactionRepository.findOwnedByIdForUpdate(id, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch."));
                    lockOwnedSources(List.of(transaction.getMoneySource().getId()), userAccount);
                    transactionRepository.delete(transaction);
          }

          private Category findOwnedCategory(Long categoryId, UserAccount userAccount) {
                    return categoryRepository.findByIdAndUserAccountId(categoryId, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                            "Danh mục không thuộc người dùng hiện tại."));
          }

          private MoneySource findOwnedMoneySourceForUpdate(Long moneySourceId, UserAccount userAccount) {
                    return moneySourceRepository.findOwnedByIdForUpdate(moneySourceId, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                            "Nguồn tiền không thuộc người dùng hiện tại."));
          }

          private Map<Long, MoneySource> lockOwnedSources(List<Long> sourceIds, UserAccount userAccount) {
                    Map<Long, MoneySource> sources = new HashMap<>();
                    for (Long sourceId : new TreeSet<>(sourceIds)) {
                              sources.put(sourceId, findOwnedMoneySourceForUpdate(sourceId, userAccount));
                    }
                    return sources;
          }

          private void validateAvailableBalance(MoneySource moneySource,
                              UserAccount userAccount,
                              TransactionType type,
                              BigDecimal amount,
                              Long excludedTransactionId) {
                    if (type != TransactionType.EXPENSE) {
                              return;
                    }

                    BigDecimal income = transactionRepository.sumByUserAndSourceAndTypeExcludingTransaction(
                                        userAccount.getId(), moneySource.getId(), TransactionType.INCOME,
                                        excludedTransactionId);
                    BigDecimal expense = transactionRepository.sumByUserAndSourceAndTypeExcludingTransaction(
                                        userAccount.getId(), moneySource.getId(), TransactionType.EXPENSE,
                                        excludedTransactionId);
                    BigDecimal transferIn = transferRepository.sumIncomingByUserAndSourceExcludingTransfer(
                                        userAccount.getId(), moneySource.getId(), null);
                    BigDecimal transferOut = transferRepository.sumOutgoingByUserAndSourceExcludingTransfer(
                                        userAccount.getId(), moneySource.getId(), null);
                    BigDecimal availableBalance = moneySource.getInitialBalance()
                                        .add(income == null ? BigDecimal.ZERO : income)
                                        .subtract(expense == null ? BigDecimal.ZERO : expense)
                                        .add(transferIn == null ? BigDecimal.ZERO : transferIn)
                                        .subtract(transferOut == null ? BigDecimal.ZERO : transferOut);
                    if (amount.compareTo(availableBalance) > 0) {
                              throw new IllegalArgumentException("Số dư nguồn tiền không đủ cho giao dịch chi tiêu.");
                    }
          }

          private void validateCategoryType(TransactionType transactionType, Category category) {
                    if (transactionType == TransactionType.INCOME
                                        && category.getType() != com.example.taichinhcanhan.entity.CategoryType.INCOME) {
                              throw new IllegalArgumentException("Giao dịch thu nhập phải dùng danh mục thu nhập.");
                    }
                    if (transactionType == TransactionType.EXPENSE
                                        && category.getType() != com.example.taichinhcanhan.entity.CategoryType.EXPENSE) {
                              throw new IllegalArgumentException("Giao dịch chi tiêu phải dùng danh mục chi tiêu.");
                    }
          }

          private void applyRequest(Transaction transaction,
                              TransactionRequest request,
                              Category category,
                              MoneySource moneySource) {
                    transaction.setType(request.getType());
                    transaction.setAmount(request.getAmount());
                    transaction.setCategory(category);
                    transaction.setMoneySource(moneySource);
                    transaction.setTransactionDate(request.getTransactionDate());
                    transaction.setDescription(normalizeDescription(request.getDescription()));
          }

          private String normalizeDescription(String description) {
                    if (description == null || description.isBlank()) {
                              return null;
                    }
                    return description.trim();
          }
}