package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.TransactionRequest;
import com.example.taichinhcanhan.entity.Category;
import com.example.taichinhcanhan.entity.CategoryType;
import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.Transaction;
import com.example.taichinhcanhan.entity.TransactionType;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.CategoryRepository;
import com.example.taichinhcanhan.repository.MoneySourceRepository;
import com.example.taichinhcanhan.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

          @Mock
          private TransactionRepository transactionRepository;

          @Mock
          private MoneySourceRepository moneySourceRepository;

          @Mock
          private CategoryRepository categoryRepository;

          private TransactionService transactionService;
          private UserAccount userAccount;
          private MoneySource firstSource;
          private MoneySource secondSource;
          private Category incomeCategory;
          private Category expenseCategory;

          @BeforeEach
          void setUp() {
                    transactionService = new TransactionService(
                                        transactionRepository, moneySourceRepository, categoryRepository);

                    userAccount = new UserAccount();
                    userAccount.setId(1L);

                    firstSource = source(10L, "100.00");
                    secondSource = source(11L, "50.00");
                    incomeCategory = category(20L, CategoryType.INCOME);
                    expenseCategory = category(21L, CategoryType.EXPENSE);

          }

          @Test
          void createsIncomeWithoutChangingInitialBalance() {
                    stubCategory(20L);
                    stubSource(10L);
                    stubSave();
                    Transaction created = transactionService.create(userAccount,
                                        request(TransactionType.INCOME, "30.00", 20L, 10L));

                    assertEquals(TransactionType.INCOME, created.getType());
                    assertEquals(new BigDecimal("100.00"), firstSource.getInitialBalance());
                    verify(transactionRepository).save(created);
          }

          @Test
          void createsExpenseWhenBalanceIsSufficient() {
                    stubCategory(21L);
                    stubSource(10L);
                    stubBalance(10L, null, "0.00", "0.00");
                    stubSave();

                    Transaction created = transactionService.create(userAccount,
                                        request(TransactionType.EXPENSE, "40.00", 21L, 10L));

                    assertEquals(TransactionType.EXPENSE, created.getType());
                    verify(transactionRepository).save(created);
          }

          @Test
          void rejectsExpenseThatExceedsAvailableBalance() {
                    stubCategory(21L);
                    stubSource(10L);
                    stubBalance(10L, null, "0.00", "70.00");

                    assertThrows(IllegalArgumentException.class, () -> transactionService.create(userAccount,
                                        request(TransactionType.EXPENSE, "31.00", 21L, 10L)));

                    verify(transactionRepository, never()).save(any(Transaction.class));
          }

          @Test
          void updatesExpenseAmountUsingBalanceWithoutOldTransaction() {
                    Transaction existing = existingTransaction(30L, TransactionType.EXPENSE, "20.00", firstSource,
                                        expenseCategory);
                    stubCategory(21L);
                    stubExisting(existing);
                    stubBalance(10L, 30L, "0.00", "30.00");
                    stubSave();

                    Transaction updated = transactionService.update(30L, userAccount,
                                        request(TransactionType.EXPENSE, "40.00", 21L, 10L));

                    assertEquals(new BigDecimal("40.00"), updated.getAmount());
                    verify(transactionRepository).save(existing);
          }

          @Test
          void rejectsIncomeToExpenseChangeWhenAdjustedBalanceIsInsufficient() {
                    Transaction existing = existingTransaction(31L, TransactionType.INCOME, "30.00", firstSource,
                                        incomeCategory);
                    stubCategory(21L);
                    stubExisting(existing);
                    stubBalance(10L, 31L, "0.00", "90.00");

                    assertThrows(IllegalArgumentException.class, () -> transactionService.update(31L, userAccount,
                                        request(TransactionType.EXPENSE, "20.00", 21L, 10L)));

                    assertEquals(TransactionType.INCOME, existing.getType());
                    assertEquals(incomeCategory, existing.getCategory());
                    verify(transactionRepository, never()).save(any(Transaction.class));
          }

          @Test
          void allowsExpenseToIncomeChange() {
                    Transaction existing = existingTransaction(32L, TransactionType.EXPENSE, "90.00", firstSource,
                                        expenseCategory);
                    stubCategory(20L);
                    stubExisting(existing);
                    stubSave();

                    Transaction updated = transactionService.update(32L, userAccount,
                                        request(TransactionType.INCOME, "25.00", 20L, 10L));

                    assertEquals(TransactionType.INCOME, updated.getType());
                    assertEquals(incomeCategory, updated.getCategory());
                    verify(transactionRepository).save(existing);
          }

          @Test
          void updatesTransactionToAnotherOwnedSource() {
                    Transaction existing = existingTransaction(33L, TransactionType.INCOME, "30.00", firstSource,
                                        incomeCategory);
                    stubCategory(20L);
                    stubExisting(existing);
                    stubSource(11L);
                    stubSave();

                    Transaction updated = transactionService.update(33L, userAccount,
                                        request(TransactionType.INCOME, "30.00", 20L, 11L));

                    assertEquals(secondSource, updated.getMoneySource());
                    verify(moneySourceRepository).findOwnedByIdForUpdate(10L, 1L);
                    verify(moneySourceRepository).findOwnedByIdForUpdate(11L, 1L);
          }

          @Test
          void deletesTransactionAndLocksItsSource() {
                    Transaction existing = existingTransaction(34L, TransactionType.EXPENSE, "15.00", firstSource,
                                        expenseCategory);
                    stubExisting(existing);

                    transactionService.delete(34L, userAccount);

                    verify(moneySourceRepository).findOwnedByIdForUpdate(10L, 1L);
                    verify(transactionRepository).delete(existing);
          }

          @Test
          void rejectsSourceOwnedByAnotherUser() {
                    when(moneySourceRepository.findOwnedByIdForUpdate(99L, 1L)).thenReturn(Optional.empty());

                    assertThrows(IllegalArgumentException.class, () -> transactionService.create(userAccount,
                                        request(TransactionType.INCOME, "10.00", 20L, 99L)));

                    verify(transactionRepository, never()).save(any(Transaction.class));
          }

          private void stubBalance(Long sourceId, Long excludedTransactionId, String income, String expense) {
                    when(transactionRepository.sumByUserAndSourceAndTypeExcludingTransaction(
                                        1L, sourceId, TransactionType.INCOME, excludedTransactionId))
                                        .thenReturn(new BigDecimal(income));
                    when(transactionRepository.sumByUserAndSourceAndTypeExcludingTransaction(
                                        1L, sourceId, TransactionType.EXPENSE, excludedTransactionId))
                                        .thenReturn(new BigDecimal(expense));
          }

          private void stubExisting(Transaction transaction) {
                    when(transactionRepository.findOwnedByIdForUpdate(transaction.getId(), 1L))
                                        .thenReturn(Optional.of(transaction));
                    stubSource(transaction.getMoneySource().getId());
          }

          private void stubCategory(Long categoryId) {
                    Category category = categoryId.equals(20L) ? incomeCategory : expenseCategory;
                    when(categoryRepository.findByIdAndUserAccountId(categoryId, 1L))
                                        .thenReturn(Optional.of(category));
          }

          private void stubSource(Long sourceId) {
                    MoneySource source = sourceId.equals(10L) ? firstSource : secondSource;
                    when(moneySourceRepository.findOwnedByIdForUpdate(sourceId, 1L))
                                        .thenReturn(Optional.of(source));
          }

          private void stubSave() {
                    when(transactionRepository.save(any(Transaction.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));
          }

          private TransactionRequest request(TransactionType type, String amount, Long categoryId, Long sourceId) {
                    TransactionRequest request = new TransactionRequest();
                    request.setType(type);
                    request.setAmount(new BigDecimal(amount));
                    request.setCategoryId(categoryId);
                    request.setMoneySourceId(sourceId);
                    request.setTransactionDate(LocalDate.now());
                    return request;
          }

          private Transaction existingTransaction(Long id,
                              TransactionType type,
                              String amount,
                              MoneySource source,
                              Category category) {
                    Transaction transaction = new Transaction();
                    transaction.setId(id);
                    transaction.setUserAccount(userAccount);
                    transaction.setType(type);
                    transaction.setAmount(new BigDecimal(amount));
                    transaction.setMoneySource(source);
                    transaction.setCategory(category);
                    transaction.setTransactionDate(LocalDate.now());
                    return transaction;
          }

          private MoneySource source(Long id, String initialBalance) {
                    MoneySource source = new MoneySource();
                    source.setId(id);
                    source.setInitialBalance(new BigDecimal(initialBalance));
                    source.setUserAccount(userAccount);
                    return source;
          }

          private Category category(Long id, CategoryType type) {
                    Category category = new Category();
                    category.setId(id);
                    category.setType(type);
                    category.setUserAccount(userAccount);
                    return category;
          }
}