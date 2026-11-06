package com.example.taichinhcanhan.dto;

import com.example.taichinhcanhan.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequest {

          @NotNull(message = "Vui lòng chọn loại giao dịch.")
          private TransactionType type;

          @NotNull(message = "Vui lòng nhập số tiền.")
          @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0.")
          private BigDecimal amount;

          @NotNull(message = "Vui lòng chọn danh mục.")
          private Long categoryId;

          @NotNull(message = "Vui lòng chọn nguồn tiền.")
          private Long moneySourceId;

          @NotNull(message = "Vui lòng chọn ngày giao dịch.")
          private LocalDate transactionDate = LocalDate.now();

          @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự.")
          private String description;

          public TransactionType getType() {
                    return type;
          }

          public void setType(TransactionType type) {
                    this.type = type;
          }

          public BigDecimal getAmount() {
                    return amount;
          }

          public void setAmount(BigDecimal amount) {
                    this.amount = amount;
          }

          public Long getCategoryId() {
                    return categoryId;
          }

          public void setCategoryId(Long categoryId) {
                    this.categoryId = categoryId;
          }

          public Long getMoneySourceId() {
                    return moneySourceId;
          }

          public void setMoneySourceId(Long moneySourceId) {
                    this.moneySourceId = moneySourceId;
          }

          public LocalDate getTransactionDate() {
                    return transactionDate;
          }

          public void setTransactionDate(LocalDate transactionDate) {
                    this.transactionDate = transactionDate;
          }

          public String getDescription() {
                    return description;
          }

          public void setDescription(String description) {
                    this.description = description;
          }
}