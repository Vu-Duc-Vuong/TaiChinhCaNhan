package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.BudgetSummary;
import com.example.taichinhcanhan.dto.CategoryStatistics;
import com.example.taichinhcanhan.dto.FinancialSummary;
import com.example.taichinhcanhan.dto.MonthlyStatistics;
import com.example.taichinhcanhan.dto.MoneySourceStatistics;
import com.example.taichinhcanhan.dto.StatisticsReport;
import com.example.taichinhcanhan.entity.MoneySourceType;
import com.example.taichinhcanhan.entity.TransactionType;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.MoneySourceRepository;
import com.example.taichinhcanhan.repository.TransactionRepository;
import com.example.taichinhcanhan.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

          private final TransactionRepository transactionRepository;
          private final TransferRepository transferRepository;
          private final MoneySourceRepository moneySourceRepository;
          private final BudgetService budgetService;

          public StatisticsService(TransactionRepository transactionRepository,
                              TransferRepository transferRepository,
                              MoneySourceRepository moneySourceRepository,
                              BudgetService budgetService) {
                    this.transactionRepository = transactionRepository;
                    this.transferRepository = transferRepository;
                    this.moneySourceRepository = moneySourceRepository;
                    this.budgetService = budgetService;
          }

          @Transactional(readOnly = true)
          public StatisticsReport buildReport(UserAccount userAccount, LocalDate startDate, LocalDate endDate) {
                    validateDateRange(startDate, endDate);

                    FinancialSummary summary = buildSummary(userAccount, startDate, endDate);
                    List<MonthlyStatistics> monthly = buildMonthly(userAccount, startDate, endDate);
                    List<MonthlyStatistics> yearly = buildYearly(userAccount, startDate, endDate);
                    List<CategoryStatistics> categories = buildCategories(userAccount, startDate, endDate,
                                        summary.getExpense());
                    List<MoneySourceStatistics> sources = buildSources(userAccount, startDate, endDate);
                    List<BudgetSummary> budgets = budgetService.search(userAccount, null, null, null, null, null);

                    return new StatisticsReport(startDate, endDate, summary, monthly, yearly, categories, sources,
                                        budgets);
          }

          @Transactional(readOnly = true)
          public FinancialSummary getSummary(UserAccount userAccount, LocalDate startDate, LocalDate endDate) {
                    validateDateRange(startDate, endDate);
                    return buildSummary(userAccount, startDate, endDate);
          }

          @Transactional(readOnly = true)
          public List<MonthlyStatistics> getMonthlyStatistics(UserAccount userAccount,
                              LocalDate startDate,
                              LocalDate endDate) {
                    validateDateRange(startDate, endDate);
                    return buildMonthly(userAccount, startDate, endDate);
          }

          private void validateDateRange(LocalDate startDate, LocalDate endDate) {
                    if (startDate.isAfter(endDate)) {
                              throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc.");
                    }
          }

          private FinancialSummary buildSummary(UserAccount userAccount, LocalDate startDate, LocalDate endDate) {
                    BigDecimal income = BigDecimal.ZERO;
                    BigDecimal expense = BigDecimal.ZERO;
                    long incomeCount = 0;
                    long expenseCount = 0;

                    for (Object[] row : transactionRepository.summarizeByType(userAccount.getId(), startDate,
                                        endDate)) {
                              TransactionType type = (TransactionType) row[0];
                              BigDecimal amount = decimal(row[1]);
                              long count = ((Number) row[2]).longValue();
                              if (type == TransactionType.INCOME) {
                                        income = amount;
                                        incomeCount = count;
                              } else {
                                        expense = amount;
                                        expenseCount = count;
                              }
                    }
                    return new FinancialSummary(income, expense, incomeCount, expenseCount);
          }

          private List<MonthlyStatistics> buildMonthly(UserAccount userAccount,
                              LocalDate startDate,
                              LocalDate endDate) {
                    Map<String, BigDecimal[]> values = new LinkedHashMap<>();
                    for (Object[] row : transactionRepository.summarizeByMonth(userAccount.getId(), startDate,
                                        endDate)) {
                              String period = String.format("%04d-%02d", ((Number) row[0]).intValue(),
                                                  ((Number) row[1]).intValue());
                              BigDecimal[] amounts = values.computeIfAbsent(period,
                                                  key -> new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
                              if (row[2] == TransactionType.INCOME) {
                                        amounts[0] = decimal(row[3]);
                              } else {
                                        amounts[1] = decimal(row[3]);
                              }
                    }
                    return values.entrySet().stream()
                                        .map(entry -> new MonthlyStatistics(entry.getKey(), entry.getValue()[0],
                                                            entry.getValue()[1]))
                                        .toList();
          }

          private List<MonthlyStatistics> buildYearly(UserAccount userAccount,
                              LocalDate startDate,
                              LocalDate endDate) {
                    Map<String, BigDecimal[]> values = new LinkedHashMap<>();
                    for (Object[] row : transactionRepository.summarizeByYear(userAccount.getId(), startDate,
                                        endDate)) {
                              String period = String.valueOf(((Number) row[0]).intValue());
                              BigDecimal[] amounts = values.computeIfAbsent(period,
                                                  key -> new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
                              if (row[1] == TransactionType.INCOME) {
                                        amounts[0] = decimal(row[2]);
                              } else {
                                        amounts[1] = decimal(row[2]);
                              }
                    }
                    return values.entrySet().stream()
                                        .map(entry -> new MonthlyStatistics(entry.getKey(), entry.getValue()[0],
                                                            entry.getValue()[1]))
                                        .toList();
          }

          private List<CategoryStatistics> buildCategories(UserAccount userAccount,
                              LocalDate startDate,
                              LocalDate endDate,
                              BigDecimal totalExpense) {
                    List<CategoryStatistics> result = new ArrayList<>();
                    for (Object[] row : transactionRepository.summarizeExpenseByCategory(
                                        userAccount.getId(), startDate, endDate)) {
                              BigDecimal amount = decimal(row[1]);
                              BigDecimal percent = totalExpense.signum() == 0
                                                  ? BigDecimal.ZERO
                                                  : amount.multiply(BigDecimal.valueOf(100)).divide(totalExpense, 2,
                                                                      RoundingMode.HALF_UP);
                              result.add(new CategoryStatistics((String) row[0], amount, percent));
                    }
                    return result;
          }

          private List<MoneySourceStatistics> buildSources(UserAccount userAccount,
                              LocalDate startDate,
                              LocalDate endDate) {
                    Map<Long, MoneySourceStatistics> values = new LinkedHashMap<>();
                    moneySourceRepository.searchByUserId(userAccount.getId(), null, null)
                                        .forEach(source -> values.put(source.getId(),
                                                            new MoneySourceStatistics(source.getId(), source.getName(),
                                                                                source.getType())));

                    for (Object[] row : transactionRepository.summarizeByMoneySource(userAccount.getId(), startDate,
                                        endDate)) {
                              Long sourceId = ((Number) row[0]).longValue();
                              String sourceName = (String) row[1];
                              MoneySourceType sourceType = (MoneySourceType) row[2];
                              MoneySourceStatistics statistics = values.computeIfAbsent(sourceId,
                                                  id -> new MoneySourceStatistics(id, sourceName, sourceType));
                              if (row[3] == TransactionType.INCOME) {
                                        statistics.addIncome(decimal(row[4]));
                              } else {
                                        statistics.addExpense(decimal(row[4]));
                              }
                    }
                    for (Object[] row : transferRepository.summarizeIncomingBySource(userAccount.getId(), startDate,
                                        endDate)) {
                              Long sourceId = ((Number) row[0]).longValue();
                              String sourceName = (String) row[1];
                              MoneySourceType sourceType = (MoneySourceType) row[2];
                              values.computeIfAbsent(sourceId,
                                                  id -> new MoneySourceStatistics(id, sourceName, sourceType))
                                                  .addTransferIn(decimal(row[3]));
                    }
                    for (Object[] row : transferRepository.summarizeOutgoingBySource(userAccount.getId(), startDate,
                                        endDate)) {
                              Long sourceId = ((Number) row[0]).longValue();
                              String sourceName = (String) row[1];
                              MoneySourceType sourceType = (MoneySourceType) row[2];
                              values.computeIfAbsent(sourceId,
                                                  id -> new MoneySourceStatistics(id, sourceName, sourceType))
                                                  .addTransferOut(decimal(row[3]));
                    }
                    return new ArrayList<>(values.values());
          }

          private BigDecimal decimal(Object value) {
                    return value == null ? BigDecimal.ZERO : (BigDecimal) value;
          }
}