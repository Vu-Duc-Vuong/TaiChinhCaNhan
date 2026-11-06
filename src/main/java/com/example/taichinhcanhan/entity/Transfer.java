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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
public class Transfer {

          @Id
          @GeneratedValue(strategy = GenerationType.IDENTITY)
          private Long id;

          @NotNull
          @DecimalMin(value = "0.01")
          @Column(nullable = false, precision = 15, scale = 2)
          private BigDecimal amount;

          @NotNull
          @Column(name = "transfer_date", nullable = false)
          private LocalDate transferDate;

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

          @NotNull
          @ManyToOne(fetch = FetchType.LAZY, optional = false)
          @JoinColumn(name = "from_source_id", nullable = false)
          private MoneySource fromSource;

          @NotNull
          @ManyToOne(fetch = FetchType.LAZY, optional = false)
          @JoinColumn(name = "to_source_id", nullable = false)
          private MoneySource toSource;

          public Transfer() {
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

          public BigDecimal getAmount() {
                    return amount;
          }

          public void setAmount(BigDecimal amount) {
                    this.amount = amount;
          }

          public LocalDate getTransferDate() {
                    return transferDate;
          }

          public void setTransferDate(LocalDate transferDate) {
                    this.transferDate = transferDate;
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

          public MoneySource getFromSource() {
                    return fromSource;
          }

          public void setFromSource(MoneySource fromSource) {
                    this.fromSource = fromSource;
          }

          public MoneySource getToSource() {
                    return toSource;
          }

          public void setToSource(MoneySource toSource) {
                    this.toSource = toSource;
          }
}