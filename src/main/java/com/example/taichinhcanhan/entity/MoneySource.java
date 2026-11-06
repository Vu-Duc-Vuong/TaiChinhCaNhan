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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "money_sources")
public class MoneySource {

          @Id
          @GeneratedValue(strategy = GenerationType.IDENTITY)
          private Long id;

          @NotBlank
          @Size(max = 100)
          @Column(nullable = false, length = 100)
          private String name;

          @NotNull
          @Enumerated(EnumType.STRING)
          @Column(nullable = false, length = 30)
          private MoneySourceType type;

          @NotNull
          @DecimalMin(value = "0.00")
          @Column(name = "initial_balance", nullable = false, precision = 15, scale = 2)
          private BigDecimal initialBalance = BigDecimal.ZERO;

          @Transient
          private BigDecimal currentBalance = BigDecimal.ZERO;

          @Size(max = 255)
          @Column(length = 255)
          private String description;

          @Column(name = "created_at", nullable = false, updatable = false)
          private LocalDateTime createdAt;

          @Column(name = "updated_at", nullable = false)
          private LocalDateTime updatedAt;

          @NotNull
          @ManyToOne(fetch = FetchType.LAZY, optional = false)
          @JoinColumn(name = "user_id", nullable = false)
          private UserAccount userAccount;

          public MoneySource() {
          }

          @PrePersist
          protected void onCreate() {
                    LocalDateTime now = LocalDateTime.now();
                    createdAt = now;
                    updatedAt = now;
          }

          @PreUpdate
          protected void onUpdate() {
                    updatedAt = LocalDateTime.now();
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

          public MoneySourceType getType() {
                    return type;
          }

          public void setType(MoneySourceType type) {
                    this.type = type;
          }

          public BigDecimal getInitialBalance() {
                    return initialBalance;
          }

          public void setInitialBalance(BigDecimal initialBalance) {
                    this.initialBalance = initialBalance;
          }

          public BigDecimal getCurrentBalance() {
                    return currentBalance;
          }

          public void setCurrentBalance(BigDecimal currentBalance) {
                    this.currentBalance = currentBalance;
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

          public LocalDateTime getUpdatedAt() {
                    return updatedAt;
          }

          public void setUpdatedAt(LocalDateTime updatedAt) {
                    this.updatedAt = updatedAt;
          }

          public UserAccount getUserAccount() {
                    return userAccount;
          }

          public void setUserAccount(UserAccount userAccount) {
                    this.userAccount = userAccount;
          }
}