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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class BudgetService {

          public static final String STATUS_NORMAL = "NORMAL";
          public static final String STATUS_WARNING = "WARNING";
          public static final String STATUS_EXCEEDED = "EXCEEDED";

          private static final BigDecimal WARNING_PERCENT = BigDecimal.valueOf(80);
          private static final BigDecimal EXCEEDED_PERCENT = BigDecimal.valueOf(100);

          private final BudgetRepository budgetRepository;
          private final CategoryRepository categoryRepository;
          private final TransactionRepository transactionRepository;

          public BudgetService(BudgetRepository budgetRepository,
                              CategoryRepository categoryRepository,
                              TransactionRepository transactionRepository) {
                    this.budgetRepository = budgetRepository;
                    this.categoryRepository = categoryRepository;
                    this.transactionRepository = transactionRepository;
          }

          @Transactional(readOnly = true)
          public List<BudgetSummary> search(UserAccount userAccount,
                              String keyword,
                              Long categoryId,
                              Boolean overall,
                              LocalDate startDate,
                              LocalDate endDate,
                              String status) {
                    String normalizedKeyword = keyword == null ? null : keyword.trim();
                    return budgetRepository.searchByUserId(
                                        userAccount.getId(), normalizedKeyword, categoryId, overall, startDate, endDate)
                                        .stream()
                                        .map(this::toSummary)
                                        .filter(summary -> status == null || status.isBlank()
                                                            || status.equals(summary.getStatus()))
                                        .toList();
          }

          @Transactional
          public Budget create(UserAccount userAccount, BudgetRequest request) {
                    validateDates(request.getStartDate(), request.getEndDate());
                    Category category = findCategoryForBudget(request.getCategoryId(), userAccount);
                    ensureNoOverlap(userAccount, request, category, null);

                    Budget budget = new Budget();
                    applyRequest(budget, request, category);
                    budget.setUserAccount(userAccount);
                    return budgetRepository.save(budget);
          }

          @Transactional(readOnly = true)
          public Budget findOwnedById(Long id, UserAccount userAccount) {
                    return budgetRepository.findByIdAndUserAccountId(id, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ngân sách."));
          }

          @Transactional(readOnly = true)
          public BudgetSummary getSummary(Long id, UserAccount userAccount) {
                    return toSummary(findOwnedById(id, userAccount));
          }

          @Transactional
          public Budget update(Long id, UserAccount userAccount, BudgetRequest request) {
                    Budget budget = findOwnedById(id, userAccount);
                    validateDates(request.getStartDate(), request.getEndDate());
                    Category category = findCategoryForBudget(request.getCategoryId(), userAccount);
                    ensureNoOverlap(userAccount, request, category, id);
                    applyRequest(budget, request, category);
                    return budgetRepository.save(budget);
          }

          @Transactional
          public void delete(Long id, UserAccount userAccount) {
                    Budget budget = findOwnedById(id, userAccount);
                    budgetRepository.delete(budget);
          }

          private BudgetSummary toSummary(Budget budget) {
                    Long categoryId = budget.getCategory() == null ? null : budget.getCategory().getId();
                    BigDecimal spent = transactionRepository.sumExpenseForBudget(
                                        budget.getUserAccount().getId(), categoryId, budget.getStartDate(),
                                        budget.getEndDate());
                    if (spent == null) {
                              spent = BigDecimal.ZERO;
                    }
                    BigDecimal remaining = budget.getAmount().subtract(spent);
                    BigDecimal usedPercent = spent.multiply(BigDecimal.valueOf(100))
                                        .divide(budget.getAmount(), 2, RoundingMode.HALF_UP);
                    return new BudgetSummary(budget, spent, remaining, usedPercent, resolveStatus(usedPercent));
          }

          private String resolveStatus(BigDecimal usedPercent) {
                    if (usedPercent.compareTo(EXCEEDED_PERCENT) >= 0) {
                              return STATUS_EXCEEDED;
                    }
                    if (usedPercent.compareTo(WARNING_PERCENT) >= 0) {
                              return STATUS_WARNING;
                    }
                    return STATUS_NORMAL;
          }

          private Category findCategoryForBudget(Long categoryId, UserAccount userAccount) {
                    if (categoryId == null) {
                              return null;
                    }
                    Category category = categoryRepository.findByIdAndUserAccountId(categoryId, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                            "Danh mục không thuộc người dùng hiện tại."));
                    if (category.getType() != CategoryType.EXPENSE) {
                              throw new IllegalArgumentException("Ngân sách chỉ được gắn với danh mục chi tiêu.");
                    }
                    return category;
          }

          private void ensureNoOverlap(UserAccount userAccount,
                              BudgetRequest request,
                              Category category,
                              Long budgetId) {
                    Long categoryId = category == null ? null : category.getId();
                    if (budgetRepository.existsOverlappingBudget(
                                        userAccount.getId(), categoryId, request.getStartDate(), request.getEndDate(),
                                        budgetId)) {
                              throw new IllegalArgumentException(
                                                  "Đã tồn tại ngân sách cùng phạm vi trong khoảng thời gian bị chồng lấn.");
                    }
          }

          private void validateDates(LocalDate startDate, LocalDate endDate) {
                    if (startDate.isAfter(endDate)) {
                              throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc.");
                    }
          }

          private void applyRequest(Budget budget, BudgetRequest request, Category category) {
                    budget.setName(normalizeName(request.getName()));
                    budget.setAmount(request.getAmount());
                    budget.setCategory(category);
                    budget.setStartDate(request.getStartDate());
                    budget.setEndDate(request.getEndDate());
                    budget.setDescription(normalizeDescription(request.getDescription()));
          }

          private String normalizeName(String name) {
                    if (name == null || name.isBlank()) {
                              return null;
                    }
                    return name.trim();
          }

          private String normalizeDescription(String description) {
                    if (description == null || description.isBlank()) {
                              return null;
                    }
                    return description.trim();
          }
}