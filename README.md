# Hệ thống quản lý tài chính cá nhân

## 1. Giới thiệu
Ứng dụng web giúp người dùng theo dõi và quản lý tài chính cá nhân: ghi chép
thu chi, quản lý nhiều nguồn tiền, đặt ngân sách và xem báo cáo thống kê.

## 2. Mục tiêu
- Quản lý các nguồn tiền (tiền mặt, ngân hàng, ví...).
- Quản lý thu nhập và chi tiêu.
- Theo dõi số dư theo từng nguồn tiền.
- Quản lý ngân sách theo danh mục chi tiêu, cảnh báo khi gần/vượt hạn mức.
- Thống kê tình hình tài chính theo tháng, danh mục, nguồn tiền.

## 3. Chức năng chính
- Quản lý tài khoản/người dùng (đăng ký, đăng nhập, hồ sơ, đổi mật khẩu).
- Quản lý nguồn tiền.
- Quản lý giao dịch thu/chi.
- Phân loại thu nhập và chi tiêu theo danh mục.
- Chuyển tiền giữa các nguồn tiền.
- Quản lý ngân sách.
- Thống kê và báo cáo (có biểu đồ).

## 4. Công nghệ sử dụng
- Java 21, Spring Boot 3.4.5, Maven Wrapper (Maven 3.9.9).
- Spring Data JPA, Spring Security, Spring Validation.
- Thymeleaf, HTML, CSS, Bootstrap 5, Chart.js.
- MySQL.
- JUnit / Spring Boot Test.

## 5. Cài đặt và chạy
1. Cài JDK 21
2. ./mvnw spring-boot:run        (Linux/macOS)
   mvnw.cmd spring-boot:run      (Windows)
3. Chạy kiểm thử: `./mvnw test`

## 6. Thành viên
- Vũ Đức Vượng - 24100383