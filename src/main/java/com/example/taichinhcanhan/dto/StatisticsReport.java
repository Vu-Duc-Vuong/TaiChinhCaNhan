package com.example.taichinhcanhan.dto;

import java.time.LocalDate;
import java.util.List;

public class StatisticsReport {

          private final LocalDate startDate;
          private final LocalDate endDate;
          private final FinancialSummary summary;
          private final List<MonthlyStatistics> monthlyStatistics;
          private final List<MonthlyStatistics> yearlyStatistics;
          private final List<CategoryStatistics> categoryStatistics;
          private final List<MoneySourceStatistics> moneySourceStatistics;
          private final List<BudgetSummary> budgetSummaries;

          public StatisticsReport(LocalDate startDate,
                              LocalDate endDate,
                              FinancialSummary summary,
                              List<MonthlyStatistics> monthlyStatistics,
                              List<MonthlyStatistics> yearlyStatistics,
                              List<CategoryStatistics> categoryStatistics,
                              List<MoneySourceStatistics> moneySourceStatistics,
                              List<BudgetSummary> budgetSummaries) {
                    this.startDate = startDate;
                    this.endDate = endDate;
                    this.summary = summary;
                    this.monthlyStatistics = monthlyStatistics;
                    this.yearlyStatistics = yearlyStatistics;
                    this.categoryStatistics = categoryStatistics;
                    this.moneySourceStatistics = moneySourceStatistics;
                    this.budgetSummaries = budgetSummaries;
          }

          public LocalDate getStartDate() {
                    return startDate;
          }

          public LocalDate getEndDate() {
                    return endDate;
          }

          public FinancialSummary getSummary() {
                    return summary;
          }

          public List<MonthlyStatistics> getMonthlyStatistics() {
                    return monthlyStatistics;
          }

          public List<MonthlyStatistics> getYearlyStatistics() {
                    return yearlyStatistics;
          }

          public List<CategoryStatistics> getCategoryStatistics() {
                    return categoryStatistics;
          }

          public List<MoneySourceStatistics> getMoneySourceStatistics() {
                    return moneySourceStatistics;
          }

          public List<BudgetSummary> getBudgetSummaries() {
                    return budgetSummaries;
          }
}