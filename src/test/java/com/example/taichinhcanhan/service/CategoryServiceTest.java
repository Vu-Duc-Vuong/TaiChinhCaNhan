package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.CategoryRequest;
import com.example.taichinhcanhan.entity.Category;
import com.example.taichinhcanhan.entity.CategoryType;
import com.example.taichinhcanhan.entity.TransactionType;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.BudgetRepository;
import com.example.taichinhcanhan.repository.CategoryRepository;
import com.example.taichinhcanhan.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

          @Mock
          private CategoryRepository categoryRepository;

          @Mock
          private TransactionRepository transactionRepository;

          @Mock
          private BudgetRepository budgetRepository;

          private CategoryService categoryService;
          private UserAccount userAccount;
          private Category category;

          @BeforeEach
          void setUp() {
                    categoryService = new CategoryService(categoryRepository, transactionRepository,
                                        budgetRepository);
                    userAccount = new UserAccount();
                    userAccount.setId(1L);
                    category = category(5L, CategoryType.INCOME);
          }

          @Test
          void rejectsChangingTypeWhenExistingTransactionWouldBecomeInvalid() {
                    stubUpdateCategory();
                    when(categoryRepository.existsByUserAccountIdAndNameIgnoreCaseAndTypeAndIdNot(
                                        1L, "Salary", CategoryType.EXPENSE, 5L)).thenReturn(false);
                    when(transactionRepository.existsByCategoryIdAndTypeNot(5L, TransactionType.EXPENSE))
                                        .thenReturn(true);

                    assertThrows(IllegalArgumentException.class, () -> categoryService.update(
                                        5L, userAccount, request(CategoryType.EXPENSE)));

                    assertEquals(CategoryType.INCOME, category.getType());
                    verify(categoryRepository, never()).save(any(Category.class));
          }

          @Test
          void rejectsChangingExpenseCategoryUsedByExpenseTransactionToIncome() {
                    category.setType(CategoryType.EXPENSE);
                    stubUpdateCategory();
                    when(categoryRepository.existsByUserAccountIdAndNameIgnoreCaseAndTypeAndIdNot(
                                        1L, "Salary", CategoryType.INCOME, 5L)).thenReturn(false);
                    when(transactionRepository.existsByCategoryIdAndTypeNot(5L, TransactionType.INCOME))
                                        .thenReturn(true);

                    assertThrows(IllegalArgumentException.class, () -> categoryService.update(
                                        5L, userAccount, request(CategoryType.INCOME)));

                    assertEquals(CategoryType.EXPENSE, category.getType());
                    verify(categoryRepository, never()).save(any(Category.class));
          }

          @Test
          void allowsChangingTypeWhenNoExistingTransactionsConflict() {
                    stubUpdateCategory();
                    when(categoryRepository.existsByUserAccountIdAndNameIgnoreCaseAndTypeAndIdNot(
                                        1L, "Salary", CategoryType.EXPENSE, 5L)).thenReturn(false);
                    when(transactionRepository.existsByCategoryIdAndTypeNot(5L, TransactionType.EXPENSE))
                                        .thenReturn(false);
                    when(categoryRepository.save(category)).thenReturn(category);

                    Category updated = categoryService.update(5L, userAccount,
                                        request(CategoryType.EXPENSE));

                    assertEquals(CategoryType.EXPENSE, updated.getType());
                    verify(categoryRepository).save(category);
          }

          @Test
          void rejectsChangingBudgetCategoryToIncome() {
                    category.setType(CategoryType.EXPENSE);
                    stubUpdateCategory();
                    when(categoryRepository.existsByUserAccountIdAndNameIgnoreCaseAndTypeAndIdNot(
                                        1L, "Salary", CategoryType.INCOME, 5L)).thenReturn(false);
                    when(transactionRepository.existsByCategoryIdAndTypeNot(5L, TransactionType.INCOME))
                                        .thenReturn(false);
                    when(budgetRepository.existsByCategoryId(5L)).thenReturn(true);

                    assertThrows(IllegalArgumentException.class, () -> categoryService.update(
                                        5L, userAccount, request(CategoryType.INCOME)));

                    assertEquals(CategoryType.EXPENSE, category.getType());
                    verify(categoryRepository, never()).save(any(Category.class));
          }

          @Test
          void refusesDeletingCategoryUsedByTransaction() {
                    stubLockedCategory();
                    when(transactionRepository.existsByCategoryId(5L)).thenReturn(true);

                    assertThrows(IllegalStateException.class, () -> categoryService.delete(5L, userAccount));

                    verify(categoryRepository, never()).delete(category);
          }

          @Test
          void refusesDeletingCategoryUsedByBudget() {
                    stubLockedCategory();
                    when(transactionRepository.existsByCategoryId(5L)).thenReturn(false);
                    when(budgetRepository.existsByCategoryId(5L)).thenReturn(true);

                    assertThrows(IllegalStateException.class, () -> categoryService.delete(5L, userAccount));

                    verify(categoryRepository, never()).delete(category);
          }

          @Test
          void rejectsAccessToCategoryOwnedByAnotherUser() {
                    when(categoryRepository.findOwnedByIdForUpdate(5L, 1L)).thenReturn(Optional.empty());

                    assertThrows(IllegalArgumentException.class, () -> categoryService.delete(5L, userAccount));

                    verify(transactionRepository, never()).existsByCategoryId(5L);
          }

          private void stubUpdateCategory() {
                    when(categoryRepository.findOwnedByIdForUpdate(5L, 1L)).thenReturn(Optional.of(category));
          }

          private void stubLockedCategory() {
                    when(categoryRepository.findOwnedByIdForUpdate(5L, 1L)).thenReturn(Optional.of(category));
          }

          private CategoryRequest request(CategoryType type) {
                    CategoryRequest request = new CategoryRequest();
                    request.setName("Salary");
                    request.setType(type);
                    return request;
          }

          private Category category(Long id, CategoryType type) {
                    Category category = new Category();
                    category.setId(id);
                    category.setUserAccount(userAccount);
                    category.setName("Original");
                    category.setType(type);
                    return category;
          }
}