package com.example.taichinhcanhan.dto;

import com.example.taichinhcanhan.entity.MoneySourceType;

import java.math.BigDecimal;

public class MoneySourceStatistics {

          private final Long sourceId;
          private final String sourceName;
          private final MoneySourceType sourceType;
          private BigDecimal income = BigDecimal.ZERO;
          private BigDecimal expense = BigDecimal.ZERO;
          private BigDecimal transferIn = BigDecimal.ZERO;
          private BigDecimal transferOut = BigDecimal.ZERO;

          public MoneySourceStatistics(Long sourceId, String sourceName, MoneySourceType sourceType) {
                    this.sourceId = sourceId;
                    this.sourceName = sourceName;
                    this.sourceType = sourceType;
          }

          public Long getSourceId() {
                    return sourceId;
          }

          public String getSourceName() {
                    return sourceName;
          }

          public MoneySourceType getSourceType() {
                    return sourceType;
          }

          public BigDecimal getIncome() {
                    return income;
          }

          public BigDecimal getExpense() {
                    return expense;
          }

          public BigDecimal getTransferIn() {
                    return transferIn;
          }

          public BigDecimal getTransferOut() {
                    return transferOut;
          }

          public BigDecimal getNetFlow() {
                    return income.add(transferIn).subtract(expense).subtract(transferOut);
          }

          public void addIncome(BigDecimal amount) {
                    income = income.add(amount);
          }

          public void addExpense(BigDecimal amount) {
                    expense = expense.add(amount);
          }

          public void addTransferIn(BigDecimal amount) {
                    transferIn = transferIn.add(amount);
          }

          public void addTransferOut(BigDecimal amount) {
                    transferOut = transferOut.add(amount);
          }
}