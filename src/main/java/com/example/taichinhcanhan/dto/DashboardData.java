package com.example.taichinhcanhan.dto;

import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;

public class DashboardData {

          private final BigDecimal totalAssets;
          private final FinancialSummary monthlySummary;
          private final List<MoneySource> moneySources;
          private final List<Transaction> recentTransactions;
          private final List<MonthlyStatistics> monthlyStatistics;
          private final String currentMonthLabel;

          public DashboardData(BigDecimal totalAssets,
                              FinancialSummary monthlySummary,
                              List<MoneySource> moneySources,
                              List<Transaction> recentTransactions,
                              List<MonthlyStatistics> monthlyStatistics,
                              String currentMonthLabel) {
                    this.totalAssets = totalAssets;
                    this.monthlySummary = monthlySummary;
                    this.moneySources = List.copyOf(moneySources);
                    this.recentTransactions = List.copyOf(recentTransactions);
                    this.monthlyStatistics = List.copyOf(monthlyStatistics);
                    this.currentMonthLabel = currentMonthLabel;
          }

          public BigDecimal getTotalAssets() {
                    return totalAssets;
          }

          public FinancialSummary getMonthlySummary() {
                    return monthlySummary;
          }

          public List<MoneySource> getMoneySources() {
                    return moneySources;
          }

          public List<Transaction> getRecentTransactions() {
                    return recentTransactions;
          }

          public List<MonthlyStatistics> getMonthlyStatistics() {
                    return monthlyStatistics;
          }

          public String getCurrentMonthLabel() {
                    return currentMonthLabel;
          }
}