package com.example.taichinhcanhan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "budgets")
public class Budget {

          @Id
          @GeneratedValue(strategy = GenerationType.IDENTITY)
          private Long id;

          @Size(max = 100)
          @Column(length = 100)
          private String name;

          @NotNull
          @DecimalMin(value = "0.01")
          @Column(nullable = false, precision = 15, scale = 2)
          private BigDecimal amount;

          @NotNull
          @Column(name = "start_date", nullable = false)
          private LocalDate startDate;

          @NotNull
          @Column(name = "end_date", nullable = false)
          private LocalDate endDate;

          @Size(max = 255)
          @Column(length = 255)
          private String description;

          @Column(name = "created_at", nullable = false, updatable = false)
          private LocalDateTime createdAt;

          @NotNull
          @ManyToOne(fetch = FetchType.LAZY, optional = false)
          @JoinColumn(name = "user_id", nullable = false)
          private UserAccount userAccount;

          @ManyToOne(fetch = FetchType.LAZY)
          @JoinColumn(name = "category_id")
          private Category category;

          public Budget() {
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

          public String getName() {
                    return name;
          }

          public void setName(String name) {
                    this.name = name;
          }

          public BigDecimal getAmount() {
                    return amount;
          }

          public void setAmount(BigDecimal amount) {
                    this.amount = amount;
          }

          public LocalDate getStartDate() {
                    return startDate;
          }

          public void setStartDate(LocalDate startDate) {
                    this.startDate = startDate;
          }

          public LocalDate getEndDate() {
                    return endDate;
          }

          public void setEndDate(LocalDate endDate) {
                    this.endDate = endDate;
          }

          public String getDescription() {
                    return description;
          }

          public void setDescription(String description) {
                    this.description = description;
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

          public Category getCategory() {
                    return category;
          }

          public void setCategory(Category category) {
                    this.category = category;
          }
}