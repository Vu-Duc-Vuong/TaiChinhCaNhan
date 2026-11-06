package com.example.taichinhcanhan.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MoneySourceActivity {

          private final LocalDate date;
          private final LocalDateTime createdAt;
          private final String typeLabel;
          private final String title;
          private final String details;
          private final BigDecimal amount;
          private final boolean increasesBalance;

          public MoneySourceActivity(LocalDate date,
                              LocalDateTime createdAt,
                              String typeLabel,
                              String title,
                              String details,
                              BigDecimal amount,
                              boolean increasesBalance) {
                    this.date = date;
                    this.createdAt = createdAt;
                    this.typeLabel = typeLabel;
                    this.title = title;
                    this.details = details;
                    this.amount = amount;
                    this.increasesBalance = increasesBalance;
          }

          public LocalDate getDate() {
                    return date;
          }

          public LocalDateTime getCreatedAt() {
                    return createdAt;
          }

          public String getTypeLabel() {
                    return typeLabel;
          }

          public String getTitle() {
                    return title;
          }

          public String getDetails() {
                    return details;
          }

          public BigDecimal getAmount() {
                    return amount;
          }

          public boolean isIncreasesBalance() {
                    return increasesBalance;
          }
}