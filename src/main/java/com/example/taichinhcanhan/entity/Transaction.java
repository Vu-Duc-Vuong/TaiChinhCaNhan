package com.example.taichinhcanhan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_transactions")
public class Transaction {

          @Id
          @GeneratedValue(strategy = GenerationType.IDENTITY)
          private Long id;

          @NotNull
          @DecimalMin(value = "0.01")
          @Column(nullable = false, precision = 15, scale = 2)
          private BigDecimal amount;

          @Size(max = 255)
          @Column(length = 255)
          private String description;

          @NotNull
          @Column(name = "transaction_date", nullable = false)
          private LocalDate transactionDate;

          @NotNull
          @Enumerated(EnumType.STRING)
          @Column(nullable = false, length = 30)
          private TransactionType type;

          @Column(name = "created_at", nullable = false, updatable = false)
          private LocalDateTime createdAt;

          @NotNull
          @ManyToOne(fetch = FetchType.LAZY, optional = false)
          @JoinColumn(name = "user_id", nullable = false)
          private UserAccount userAccount;

          @NotNull
          @ManyToOne(fetch = FetchType.LAZY, optional = false)
          @JoinColumn(name = "money_source_id", nullable = false)
          private MoneySource moneySource;

          @NotNull
          @ManyToOne(fetch = FetchType.LAZY, optional = false)
          @JoinColumn(name = "category_id", nullable = false)
          private Category category;

          public Transaction() {
          }

          @PrePersist
          protected void onCreate() {
                    createdAt = LocalDateTime.now();
          }

          public Long getId() {
                    return id;
          }

          public void setId(Long id) {
                    this.id = id;
          }

          public BigDecimal getAmount() {
                    return amount;
          }

          public void setAmount(BigDecimal amount) {
                    this.amount = amount;
          }

          public String getDescription() {
                    return description;
          }

          public void setDescription(String description) {
                    this.description = description;
          }

          public LocalDate getTransactionDate() {
                    return transactionDate;
          }

          public void setTransactionDate(LocalDate transactionDate) {
                    this.transactionDate = transactionDate;
          }

          public TransactionType getType() {
                    return type;
          }

          public void setType(TransactionType type) {
                    this.type = type;
          }

          public LocalDateTime getCreatedAt() {
                    return createdAt;
          }

          public void setCreatedAt(LocalDateTime createdAt) {
                    this.createdAt = createdAt;
          }

          public UserAccount getUserAccount() {
                    return userAccount;
          }

          public void setUserAccount(UserAccount userAccount) {
                    this.userAccount = userAccount;
          }

          public MoneySource getMoneySource() {
                    return moneySource;
          }

          public void setMoneySource(MoneySource moneySource) {
                    this.moneySource = moneySource;
          }

          public Category getCategory() {
                    return category;
          }

          public void setCategory(Category category) {
                    this.category = category;
          }
}