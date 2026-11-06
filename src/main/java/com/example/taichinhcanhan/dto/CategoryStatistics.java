package com.example.taichinhcanhan.dto;

import java.math.BigDecimal;

public class CategoryStatistics {

          private final String categoryName;
          private final BigDecimal amount;
          private final BigDecimal percent;

          public CategoryStatistics(String categoryName, BigDecimal amount, BigDecimal percent) {
                    this.categoryName = categoryName;
                    this.amount = amount;
                    this.percent = percent;
          }

          public String getCategoryName() {
                    return categoryName;
          }

          public BigDecimal getAmount() {
                    return amount;
          }

          public BigDecimal getPercent() {
                    return percent;
          }
}