package com.example.taichinhcanhan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BudgetRequest {

          @Size(max = 100, message = "Tên ngân sách không được vượt quá 100 ký tự.")
          private String name;

          @NotNull(message = "Vui lòng nhập số tiền ngân sách.")
          @DecimalMin(value = "0.01", message = "Ngân sách phải lớn hơn 0.")
          private BigDecimal amount;

          @NotNull(message = "Vui lòng chọn danh mục chi tiêu cho ngân sách.")
          private Long categoryId;

          @NotNull(message = "Vui lòng chọn ngày bắt đầu.")
          private LocalDate startDate;

          @NotNull(message = "Vui lòng chọn ngày kết thúc.")
          private LocalDate endDate;

          @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự.")
          private String description;

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

          public Long getCategoryId() {
                    return categoryId;
          }

          public void setCategoryId(Long categoryId) {
                    this.categoryId = categoryId;
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
}