package com.example.taichinhcanhan.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

          @NotBlank(message = "Vui lòng nhập username.")
          @Size(min = 3, max = 50, message = "Username phải có từ 3 đến 50 ký tự.")
          private String username;

          @NotBlank(message = "Vui lòng nhập email.")
          @Email(message = "Email không đúng định dạng.")
          @Size(max = 100, message = "Email không được vượt quá 100 ký tự.")
          private String email;

          @NotBlank(message = "Vui lòng nhập họ và tên.")
          @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự.")
          private String fullName;

          @NotBlank(message = "Vui lòng nhập mật khẩu.")
          @Size(min = 8, max = 100, message = "Mật khẩu phải có ít nhất 8 ký tự.")
          private String password;

          @NotBlank(message = "Vui lòng xác nhận mật khẩu.")
          private String confirmPassword;

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

          public String getFullName() {
                    return fullName;
          }

          public void setFullName(String fullName) {
                    this.fullName = fullName;
          }

          public String getPassword() {
                    return password;
          }

          public void setPassword(String password) {
                    this.password = password;
          }

          public String getConfirmPassword() {
                    return confirmPassword;
          }

          public void setConfirmPassword(String confirmPassword) {
                    this.confirmPassword = confirmPassword;
          }
}