package com.example.taichinhcanhan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransferRequest {

          @NotNull(message = "Vui lòng chọn nguồn tiền chuyển đi.")
          private Long fromSourceId;

          @NotNull(message = "Vui lòng chọn nguồn tiền nhận.")
          private Long toSourceId;

          @NotNull(message = "Vui lòng nhập số tiền.")
          @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0.")
          private BigDecimal amount;

          @NotNull(message = "Vui lòng chọn ngày chuyển tiền.")
          private LocalDate transferDate = LocalDate.now();

          @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự.")
          private String description;

          public Long getFromSourceId() {
                    return fromSourceId;
          }

          public void setFromSourceId(Long fromSourceId) {
                    this.fromSourceId = fromSourceId;
          }

          public Long getToSourceId() {
                    return toSourceId;
          }

          public void setToSourceId(Long toSourceId) {
                    this.toSourceId = toSourceId;
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
}