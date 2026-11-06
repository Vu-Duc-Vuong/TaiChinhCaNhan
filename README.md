# Hệ thống quản lý tài chính cá nhân

## 1. Giới thiệu

Hệ thống quản lý tài chính cá nhân hỗ trợ người dùng theo dõi và
quản lý tình hình tài chính cá nhân một cách thuận tiện.

## 2. Mục tiêu

- Quản lý các nguồn tiền.
- Quản lý thu nhập và chi tiêu.
- Theo dõi số dư.
- Quản lý ngân sách.
- Thống kê tình hình tài chính.

## 3. Chức năng chính

- Quản lý tài khoản/người dùng.
- Quản lý nguồn tiền.
- Quản lý giao dịch.
- Phân loại thu nhập và chi tiêu.
- Chuyển tiền giữa các nguồn tiền.
- Quản lý ngân sách.
- Thống kê và báo cáo.

## 4. Thành viên nhóm
  Vũ Đức Vượng - 24100383

## 5. Công nghệ sử dụng

- Java 21.
- Spring Boot 3.4.5.
- Maven Wrapper với Maven 3.9.9.
- Thymeleaf, HTML, CSS và Bootstrap.
- Spring Data JPA.
- MySQL.

## 6. Cài đặt môi trường

### 6.1. Cài đặt Java 21

Cài đặt JDK 21 và kiểm tra trong PowerShell:

```powershell
java -version
```

Kết quả cần hiển thị phiên bản Java 21.

### 6.2. Tạo database MySQL

Đăng nhập MySQL bằng tài khoản quản trị và chạy:

```sql
CREATE DATABASE personal_finance
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Giai đoạn 1 chưa có Entity nên ứng dụng chưa tạo bảng nghiệp vụ.

### 6.3. Cấu hình tài khoản MySQL

Không ghi mật khẩu MySQL vào source code. Trong PowerShell, thiết lập biến môi trường cho phiên làm việc hiện tại:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "mat-khau-mysql"
```

Nếu MySQL dùng URL khác, có thể thiết lập thêm:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/personal_finance?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true"
```

### 6.4. Build project bằng Maven Wrapper

Maven Wrapper sẽ tự tải Maven 3.9.9 trong lần chạy đầu tiên nếu Maven chưa được cài trên máy:

```powershell
.\mvnw.cmd clean package
```

### 6.5. Chạy ứng dụng

Sau khi đã thiết lập biến môi trường MySQL:

```powershell
.\mvnw.cmd spring-boot:run
```

Mở trình duyệt tại:

```text
http://localhost:8080/
```

Hoặc chạy file JAR sau khi build:

```powershell
java -jar target\personal-finance-0.0.1-SNAPSHOT.jar
```

Nếu thông tin MySQL không đúng, Spring Boot sẽ báo lỗi kết nối ngay khi khởi động.
