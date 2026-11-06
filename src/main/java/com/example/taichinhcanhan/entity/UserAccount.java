package com.example.taichinhcanhan.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

          @Id
          @GeneratedValue(strategy = GenerationType.IDENTITY)
          private Long id;

          @NotBlank
          @Size(max = 50)
          @Column(nullable = false, unique = true, length = 50)
          private String username;

          @NotBlank
          @Email
          @Size(max = 100)
          @Column(nullable = false, unique = true, length = 100)
          private String email;

          @NotBlank
          @Column(nullable = false, length = 255)
          private String password;

          @NotBlank
          @Size(max = 100)
          @Column(name = "full_name", nullable = false, length = 100)
          private String fullName;

          @Column(name = "created_at", nullable = false, updatable = false)
          private LocalDateTime createdAt;

          @Column(name = "updated_at", nullable = false)
          private LocalDateTime updatedAt;

          @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, orphanRemoval = true)
          private List<MoneySource> moneySources = new ArrayList<>();

          @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, orphanRemoval = true)
          private List<Category> categories = new ArrayList<>();

          @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, orphanRemoval = true)
          private List<Transaction> transactions = new ArrayList<>();

          @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, orphanRemoval = true)
          private List<Budget> budgets = new ArrayList<>();

          public UserAccount() {
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

          public String getUsername() {
                    return username;
          }

          public void setUsername(String username) {
                    this.username = username;
          }

          public String getEmail() {
                    return email;
          }

          public void setEmail(String email) {
                    this.email = email;
          }

          public String getPassword() {
                    return password;
          }

          public void setPassword(String password) {
                    this.password = password;
          }

          public String getFullName() {
                    return fullName;
          }

          public void setFullName(String fullName) {
                    this.fullName = fullName;
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

          public List<MoneySource> getMoneySources() {
                    return moneySources;
          }

          public void setMoneySources(List<MoneySource> moneySources) {
                    this.moneySources = moneySources;
          }

          public List<Category> getCategories() {
                    return categories;
          }

          public void setCategories(List<Category> categories) {
                    this.categories = categories;
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