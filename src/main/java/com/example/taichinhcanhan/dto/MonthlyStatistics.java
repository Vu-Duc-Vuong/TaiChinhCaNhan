package com.example.taichinhcanhan.dto;

import java.math.BigDecimal;

public class MonthlyStatistics {

          private final String period;
          private final BigDecimal income;
          private final BigDecimal expense;
          private final BigDecimal difference;

          public MonthlyStatistics(String period, BigDecimal income, BigDecimal expense) {
                    this.period = period;
                    this.income = income;
                    this.expense = expense;
                    this.difference = income.subtract(expense);
          }

          public String getPeriod() {
                    return period;
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
}