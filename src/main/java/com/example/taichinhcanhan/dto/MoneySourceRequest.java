package com.example.taichinhcanhan.dto;

import com.example.taichinhcanhan.entity.MoneySourceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class MoneySourceRequest {

          @NotBlank(message = "Vui lòng nhập tên nguồn tiền.")
          @Size(max = 100, message = "Tên nguồn tiền không được vượt quá 100 ký tự.")
          private String name;

          @NotNull(message = "Vui lòng chọn loại nguồn tiền.")
          private MoneySourceType type;

          @NotNull(message = "Vui lòng nhập số dư ban đầu.")
          @DecimalMin(value = "0.00", message = "Số dư ban đầu không được âm.")
          private BigDecimal initialBalance = BigDecimal.ZERO;

          @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự.")
          private String description;

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

          public String getDescription() {
                    return description;
          }

          public void setDescription(String description) {
                    this.description = description;
          }
}