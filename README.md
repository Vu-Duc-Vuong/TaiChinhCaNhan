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

## 5. Kiến trúc hệ thống
Ứng dụng theo mô hình **MVC nhiều tầng** (Controller – Service – Repository),
giao diện được render phía server bằng Thymeleaf.

```
Người dùng (trình duyệt)
   │  HTTP (form / HTML)
   ▼
Spring Boot (:8080)
   ├── Spring Security      # xác thực, phân quyền, mã hóa mật khẩu BCrypt
   ├── Controller           # nhận request, validate dữ liệu (DTO)
   │      ▼
   ├── Service              # xử lý nghiệp vụ (số dư, ngân sách, thống kê...)
   │      ▼
   ├── Repository           # Spring Data JPA / Hibernate
   │      ▼
   │   MySQL (Aiven, kết nối SSL)
   └── Thymeleaf            # render HTML, kèm Bootstrap + Chart.js (CDN)
```

Các thực thể chính và quan hệ:
- `UserAccount` sở hữu `MoneySource`, `Category`, `Transaction`, `Transfer`, `Budget`.
- `Transaction` thuộc một `MoneySource` và một `Category`.
- `Transfer` chuyển tiền từ `MoneySource` này sang `MoneySource` khác.
- `Budget` gắn với một `Category` chi tiêu theo khoảng thời gian.

## 6. Cấu trúc dự án
```
TaiChinhCaNhan/
├── pom.xml                         # Khai báo dependency Maven
├── mvnw, mvnw.cmd, .mvn/           # Maven Wrapper
├── src/
│   ├── main/
│   │   ├── java/com/example/taichinhcanhan/
│   │   │   ├── TaiChinhCaNhanApplication.java   # Điểm khởi chạy
│   │   │   ├── config/        # Cấu hình Spring Security, tài khoản mặc định
│   │   │   ├── controller/    # Xử lý request: Auth, Dashboard, MoneySource,
│   │   │   │                  #   Transaction, Category, Transfer, Budget,
│   │   │   │                  #   Statistics, UserAccount
│   │   │   ├── service/       # Logic nghiệp vụ cho từng chức năng
│   │   │   ├── repository/    # Truy cập dữ liệu (Spring Data JPA)
│   │   │   ├── entity/        # Entity JPA và enum (loại giao dịch, danh mục, nguồn tiền)
│   │   │   └── dto/           # Đối tượng request/response, dữ liệu thống kê
│   │   └── resources/
│   │       ├── application.properties   # Cấu hình ứng dụng và database
│   │       ├── templates/               # Giao diện Thymeleaf
│   │       │   ├── layout.html, fragments/   # Bố cục chung, header, sidebar...
│   │       │   ├── auth/, user/              # Đăng nhập, đăng ký, hồ sơ
│   │       │   ├── dashboard/, statistics/   # Tổng quan, thống kê
│   │       │   └── money-source/, transaction/,
│   │       │       category/, transfer/, budget/
│   │       └── static/                  # Tài nguyên tĩnh
│   │           ├── css/style.css
│   │           └── js/                  # auth.js, money-input.js
│   └── test/java/com/example/taichinhcanhan/
│       ├── service/                     # Unit test cho các service
│       └── AuthPageIntegrationTest.java # Test tích hợp trang đăng nhập/đăng ký
└── README.md
```

## 7. Cài đặt và chạy
1. Cài JDK 21.
2. Khởi chạy ứng dụng:
   - Trên Linux/macOS: `./mvnw spring-boot:run`
   - Trên Windows: `.\mvnw spring-boot:run`
3. Mở http://localhost:8080
4. Chạy kiểm thử: `./mvnw test`

## 8. Thành viên
- Vũ Đức Vượng - 24100383