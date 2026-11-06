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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

          private final TransactionRepository transactionRepository;
          private final MoneySourceRepository moneySourceRepository;
          private final CategoryRepository categoryRepository;

          public TransactionService(TransactionRepository transactionRepository,
                              MoneySourceRepository moneySourceRepository,
                              CategoryRepository categoryRepository) {
                    this.transactionRepository = transactionRepository;
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
                    Category category = findOwnedCategory(request.getCategoryId(), userAccount);
                    MoneySource moneySource = findOwnedMoneySource(request.getMoneySourceId(), userAccount);
                    validateCategoryType(request.getType(), category);

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
                    Transaction transaction = findOwnedById(id, userAccount);
                    Category category = findOwnedCategory(request.getCategoryId(), userAccount);
                    MoneySource moneySource = findOwnedMoneySource(request.getMoneySourceId(), userAccount);
                    validateCategoryType(request.getType(), category);
                    applyRequest(transaction, request, category, moneySource);
                    return transactionRepository.save(transaction);
          }

          @Transactional
          public void delete(Long id, UserAccount userAccount) {
                    Transaction transaction = findOwnedById(id, userAccount);
                    transactionRepository.delete(transaction);
          }

          private Category findOwnedCategory(Long categoryId, UserAccount userAccount) {
                    return categoryRepository.findByIdAndUserAccountId(categoryId, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                            "Danh mục không thuộc người dùng hiện tại."));
          }

          private MoneySource findOwnedMoneySource(Long moneySourceId, UserAccount userAccount) {
                    return moneySourceRepository.findByIdAndUserAccountId(moneySourceId, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                            "Nguồn tiền không thuộc người dùng hiện tại."));
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