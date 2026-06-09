# Personal Finance App

Ung dung quan ly tai chinh ca nhan su dung Android Java, Firebase Authentication, Spring Boot REST API, MySQL va AI Scan Bill.

## 1. Cong Nghe Su Dung

### Mobile App

- Android Studio
- Java
- Retrofit
- RecyclerView
- Firebase Authentication
- Google ML Kit OCR
- YOLO/TFLite
- Random Forest local trên Android

### Backend

- Java Spring Boot
- Spring Data JPA
- REST API
- JWT optional

### Database

- MySQL

### AI

- Google ML Kit OCR
- Gemini API optional
- Expense Classification

## 2. Kien Truc Tong The

```txt
Android App
    -> Retrofit API
Spring Boot Backend
    -> MySQL Database

Firebase Auth
    -> Google Login

AI Module
    -> OCR + Expense Classification
```

## 3. Cấu Trúc Thư Mục Dự Án Chuẩn

```txt
quanlythuchi/
│
├── PersonalFinanceApp/ (Ứng dụng Android)
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── AndroidManifest.xml
│   │   │   └── java/com/example/personalfinance/
│   │   │       ├── activities/      # Splash, Login, Register, Main, Scan
│   │   │       ├── fragments/       # Được tổ chức theo module con
│   │   │       │   ├── home/
│   │   │       │   ├── transaction/
│   │   │       │   ├── account/
│   │   │       │   ├── category/
│   │   │       │   ├── budget/
│   │   │       │   ├── recurring/
│   │   │       │   └── profile/
│   │   │       ├── repositories/    # Đã chuẩn hóa số nhiều
│   │   │       ├── viewmodels/      # Đã chuẩn hóa số nhiều
│   │   │       ├── models/          # Chứa data objects
│   │   │       ├── adapters/        # Lớp liên kết dữ liệu giao diện
│   │   │       ├── api/             # Lớp mạng Retrofit
│   │   │       ├── firebase/        # Helper xác thực
│   │   │       └── utils/           # Tiện ích chung
│   │   └── res/
│   └── build.gradle
│
├── finance-backend/ (Backend Spring Boot)
│   └── src/main/java/com/example/financebackend/
│       ├── controller/      # API Endpoints
│       ├── service/         # Nghiệp vụ logic
│       ├── repository/      # Spring Data JPA repositories
│       ├── model/           # JPA Entities
│       ├── dto/             # Data Transfer Objects
│       ├── config/          # Cấu hình Firebase & Security
│       └── FinanceBackendApplication.java
│
├── database/                # Cơ sở dữ liệu SQL
│   └── schema.sql
│
├── memory-bank/             # Tài liệu lưu trữ ngữ cảnh dự án
└── ui_references/           # Hình ảnh tham khảo giao diện
```

## 5. Database

Database name: `personal_finance_app`

Schema SQL nam trong file [database/schema.sql](database/schema.sql).

### Danh Sach Bang

- `users`: luu thong tin nguoi dung.
- `accounts`: quan ly vi va tai khoan tien.
- `categories`: danh muc thu chi.
- `transactions`: giao dich tai chinh.
- `budgets`: ngan sach chi tieu.
- `transaction_images`: anh hoa don cua giao dich.
- `ai_scan_logs`: log OCR va AI classification.
- `recurring_transactions`: giao dich dinh ky.

## 6. Firebase Authentication

Android dang nhap bang Firebase, lay `firebase_uid` va Firebase token, sau do gui ve Spring Boot de dong bo user vao MySQL.

```txt
Login Firebase
    -> Lay firebase_uid va token
    -> Goi Spring Boot API
    -> Luu hoac cap nhat user trong MySQL
```

## 7. AI Scan Bill

AI Scan Bill gom hai buoc:

- OCR: doc tong tien, ngay, cua hang va noi dung hoa don.
- Expense Classification: goi y danh muc nhu an uong, mua sam, di chuyen, giai tri, hoa don.

## 8. Flow Tao Giao Dich

```txt
User nhap giao dich
    -> AddTransactionFragment
    -> Retrofit API
    -> TransactionController
    -> TransactionService
    -> TransactionRepository
    -> MySQL Database
```

## 9. API Example

### Android Retrofit

```java
@POST("transactions/create")
Call<String> createTransaction(@Body Transaction transaction);
```

### Spring Boot Controller

```java
@PostMapping("/transactions/create")
public ResponseEntity<?> createTransaction(@RequestBody Transaction transaction) {
    transactionService.save(transaction);
    return ResponseEntity.ok("Success");
}
```

## 10. Tinh Nang Chinh

- Authentication: Google Login, Email Login.
- Transaction: them, sua, xoa giao dich.
- Budget: tao ngan sach, theo doi vuot ngan sach.
- Report: thong ke ngay, thong ke thang, bieu do chi tieu.
- Wallet: quan ly vi va so du.
- AI Scan Bill: scan hoa don, OCR text, AI phan loai chi tieu.
- AI Product Scan: YOLO va Random Forest chay local tren Android.

## 11. Muc Tieu Do An

- Quan ly tai chinh ca nhan.
- Theo doi thu chi.
- Quan ly ngan sach.
- Bao cao tai chinh.
- AI ho tro nhap lieu hoa don.
- Dong bo du lieu nguoi dung.
