package com.example.taichinhcanhan.dto;

import java.math.BigDecimal;

public class FinancialSummary {

          private final BigDecimal income;
          private final BigDecimal expense;
          private final BigDecimal difference;
          private final long incomeCount;
          private final long expenseCount;

          public FinancialSummary(BigDecimal income,
                              BigDecimal expense,
                              long incomeCount,
                              long expenseCount) {
                    this.income = income;
                    this.expense = expense;
                    this.difference = income.subtract(expense);
                    this.incomeCount = incomeCount;
                    this.expenseCount = expenseCount;
          }

          public BigDecimal getIncome() {
                    return income;
          }

          public BigDecimal getExpense() {
                    return expense;
          }

          public BigDecimal getDifference() {
                    return difference;
          }

          public long getIncomeCount() {
                    return incomeCount;
          }

          public long getExpenseCount() {
                    return expenseCount;
          }
}