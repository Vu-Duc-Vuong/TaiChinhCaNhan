package com.example.taichinhcanhan.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

          @NotBlank(message = "Vui lòng nhập email.")
          @Email(message = "Email không đúng định dạng.")
          @Size(max = 100, message = "Email không được vượt quá 100 ký tự.")
          private String email;

          @NotBlank(message = "Vui lòng nhập họ và tên.")
          @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự.")
          private String fullName;

          public String getEmail() {
                    return email;
          }

          public void setEmail(String email) {
                    this.email = email;
          }

          public String getFullName() {
                    return fullName;
          }

          public void setFullName(String fullName) {
                    this.fullName = fullName;
          }
}