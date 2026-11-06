package com.example.taichinhcanhan.dto;

import com.example.taichinhcanhan.entity.Budget;

import java.math.BigDecimal;

public class BudgetSummary {

          private final Budget budget;
          private final BigDecimal spent;
          private final BigDecimal remaining;
          private final BigDecimal usedPercent;
          private final String status;

          public BudgetSummary(Budget budget,
                              BigDecimal spent,
                              BigDecimal remaining,
                              BigDecimal usedPercent,
                              String status) {
                    this.budget = budget;
                    this.spent = spent;
                    this.remaining = remaining;
                    this.usedPercent = usedPercent;
                    this.status = status;
          }

          public Budget getBudget() {
                    return budget;
          }

          public BigDecimal getSpent() {
                    return spent;
          }

          public BigDecimal getRemaining() {
                    return remaining;
          }

          public BigDecimal getUsedPercent() {
                    return usedPercent;
          }

          public String getStatus() {
                    return status;
          }
}