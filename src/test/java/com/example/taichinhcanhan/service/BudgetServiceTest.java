package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.BudgetRequest;
import com.example.taichinhcanhan.dto.BudgetSummary;
import com.example.taichinhcanhan.entity.Budget;
import com.example.taichinhcanhan.entity.Category;
import com.example.taichinhcanhan.entity.CategoryType;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.BudgetRepository;
import com.example.taichinhcanhan.repository.CategoryRepository;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

          private static final LocalDate START_DATE = LocalDate.of(2026, 1, 1);
          private static final LocalDate END_DATE = LocalDate.of(2026, 1, 31);

          @Mock
          private BudgetRepository budgetRepository;

          @Mock
          private CategoryRepository categoryRepository;

          @Mock
          private TransactionRepository transactionRepository;

          private BudgetService budgetService;
          private UserAccount userAccount;
          private Category expenseCategory;

          @BeforeEach
          void setUp() {
                    budgetService = new BudgetService(budgetRepository, categoryRepository, transactionRepository);
                    userAccount = new UserAccount();
                    userAccount.setId(1L);
                    expenseCategory = category(5L, CategoryType.EXPENSE);
          }

          @Test
          void createsBudgetForOwnedExpenseCategory() {
                    stubCategory(expenseCategory);
                    when(budgetRepository.existsOverlappingBudget(1L, 5L, START_DATE, END_DATE, null))
                                        .thenReturn(false);
                    when(budgetRepository.save(any(Budget.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                    Budget created = budgetService.create(userAccount, request(5L));

                    assertSame(expenseCategory, created.getCategory());
                    assertSame(userAccount, created.getUserAccount());
                    verify(budgetRepository).save(created);
          }

          @Test
          void rejectsIncomeCategory() {
                    stubCategory(category(6L, CategoryType.INCOME));

                    assertThrows(IllegalArgumentException.class,
                                        () -> budgetService.create(userAccount, request(6L)));

                    verify(budgetRepository, never()).save(any(Budget.class));
          }

          @Test
          void rejectsBudgetWithoutCategory() {
                    assertThrows(IllegalArgumentException.class,
                                        () -> budgetService.create(userAccount, request(null)));

                    verify(categoryRepository, never()).findOwnedByIdForUpdate(any(), any());
                    verify(budgetRepository, never()).save(any(Budget.class));
          }

          @Test
          void rejectsCategoryOwnedByAnotherUser() {
                    when(categoryRepository.findOwnedByIdForUpdate(99L, 1L)).thenReturn(Optional.empty());

                    assertThrows(IllegalArgumentException.class,
                                        () -> budgetService.create(userAccount, request(99L)));

                    verify(budgetRepository, never()).save(any(Budget.class));
          }

          @Test
          void rejectsOverlappingBudgetForSameUserAndCategory() {
                    stubCategory(expenseCategory);
                    when(budgetRepository.existsOverlappingBudget(1L, 5L, START_DATE, END_DATE, null))
                                        .thenReturn(true);

                    assertThrows(IllegalArgumentException.class,
                                        () -> budgetService.create(userAccount, request(5L)));

                    verify(budgetRepository, never()).save(any(Budget.class));
          }

          @Test
          void recalculatesExpenseSpendForEachSummaryAndKeepsExistingThresholds() {
                    Budget budget = budget(40L, expenseCategory);
                    when(budgetRepository.findByIdAndUserAccountId(40L, 1L)).thenReturn(Optional.of(budget));
                    when(transactionRepository.sumExpenseForBudget(1L, 5L, START_DATE, END_DATE))
                                        .thenReturn(new BigDecimal("200.00"), new BigDecimal("800.00"),
                                                            new BigDecimal("1000.00"), BigDecimal.ZERO);

                    BudgetSummary afterAdd = budgetService.getSummary(40L, userAccount);
                    BudgetSummary afterEdit = budgetService.getSummary(40L, userAccount);
                    BudgetSummary atLimit = budgetService.getSummary(40L, userAccount);
                    BudgetSummary afterDelete = budgetService.getSummary(40L, userAccount);

                    assertEquals(new BigDecimal("200.00"), afterAdd.getSpent());
                    assertEquals(BudgetService.STATUS_NORMAL, afterAdd.getStatus());
                    assertEquals(new BigDecimal("800.00"), afterEdit.getSpent());
                    assertEquals(BudgetService.STATUS_WARNING, afterEdit.getStatus());
                    assertEquals(new BigDecimal("1000.00"), atLimit.getSpent());
                    assertEquals(BudgetService.STATUS_EXCEEDED, atLimit.getStatus());
                    assertEquals(BigDecimal.ZERO, afterDelete.getSpent());
                    assertEquals(BudgetService.STATUS_NORMAL, afterDelete.getStatus());
                    verify(transactionRepository, org.mockito.Mockito.times(4))
                                        .sumExpenseForBudget(1L, 5L, START_DATE, END_DATE);
          }

          @Test
          void budgetSpendComesOnlyFromExpenseTransactionAggregate() {
                    Budget budget = budget(41L, expenseCategory);
                    when(budgetRepository.findByIdAndUserAccountId(41L, 1L)).thenReturn(Optional.of(budget));
                    when(transactionRepository.sumExpenseForBudget(1L, 5L, START_DATE, END_DATE))
                                        .thenReturn(new BigDecimal("125.00"));

                    BudgetSummary summary = budgetService.getSummary(41L, userAccount);

                    assertEquals(new BigDecimal("125.00"), summary.getSpent());
                    verify(transactionRepository).sumExpenseForBudget(1L, 5L, START_DATE, END_DATE);
          }

          private void stubCategory(Category category) {
                    when(categoryRepository.findOwnedByIdForUpdate(category.getId(), 1L))
                                        .thenReturn(Optional.of(category));
          }

          private BudgetRequest request(Long categoryId) {
                    BudgetRequest request = new BudgetRequest();
                    request.setName("Budget");
                    request.setAmount(new BigDecimal("1000.00"));
                    request.setCategoryId(categoryId);
                    request.setStartDate(START_DATE);
                    request.setEndDate(END_DATE);
                    return request;
          }

          private Budget budget(Long id, Category category) {
                    Budget budget = new Budget();
                    budget.setId(id);
                    budget.setUserAccount(userAccount);
                    budget.setCategory(category);
                    budget.setAmount(new BigDecimal("1000.00"));
                    budget.setStartDate(START_DATE);
                    budget.setEndDate(END_DATE);
                    return budget;
          }

          private Category category(Long id, CategoryType type) {
                    Category category = new Category();
                    category.setId(id);
                    category.setUserAccount(userAccount);
                    category.setType(type);
                    return category;
          }
}