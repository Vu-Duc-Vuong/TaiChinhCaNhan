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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

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
          private CategoryType type;

          @Size(max = 255)
          @Column(length = 255)
          private String description;

          @Column(name = "created_at", nullable = false, updatable = false)
          private LocalDateTime createdAt;

          @NotNull
          @ManyToOne(fetch = FetchType.LAZY, optional = false)
          @JoinColumn(name = "user_id", nullable = false)
          private UserAccount userAccount;

          @OneToMany(mappedBy = "category")
          private List<Transaction> transactions = new ArrayList<>();

          @OneToMany(mappedBy = "category")
          private List<Budget> budgets = new ArrayList<>();

          public Category() {
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

          public CategoryType getType() {
                    return type;
          }

          public void setType(CategoryType type) {
                    this.type = type;
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

          public List<Transaction> getTransactions() {
                    return transactions;
          }

          public void setTransactions(List<Transaction> transactions) {
                    this.transactions = transactions;
          }

          public List<Budget> getBudgets() {
                    return budgets;
          }

          public void setBudgets(List<Budget> budgets) {
                    this.budgets = budgets;
          }
}