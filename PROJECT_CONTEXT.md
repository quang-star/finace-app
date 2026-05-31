# Project Overview

Dự án **Personal Finance Management App** (Ứng dụng Quản lý Tài chính Cá nhân) là hệ thống giúp người dùng ghi chép chi tiêu, thiết lập ngân sách và thống kê dòng tiền một cách đơn giản, trực quan và bảo mật.

## Tech Stack
- **Android Client**: Java (API level 24+), Android MVVM, LiveData, Retrofit, OkHttp, Facebook SDK, Firebase Auth SDK.
- **Backend API**: Java Spring Boot (Java 21), Spring Data JPA, Spring Security (Firebase Auth integration), Lombok.
- **Database**: MySQL.
- **Authentication**: Firebase Authentication (Email/Password, Google Sign-In & Facebook Login).

## Architecture
Hệ thống được phát triển theo mô hình Client-Server:
1. **Android Application (MVVM)**: Giao diện người dùng tối giản, cao cấp theo phong cách obsidian-dark. Trách nhiệm chính là tương tác UI, ghi nhận thu chi gắn liền với **Ví mặc định duy nhất**, và gửi dữ liệu về backend thông qua REST API.
2. **Spring Boot REST API**: Tầng nghiệp vụ tập trung chịu trách nhiệm xác thực người dùng (verify Firebase token), tự động đồng bộ tài khoản, tự động tạo và quản lý **Ví chính** duy nhất cho từng user, quản lý ngân sách, và tổng hợp báo cáo đồ thị.

## Main Features
- **Facebook, Google & Email Login**: Cho phép người dùng đăng nhập linh hoạt bằng tài khoản Facebook, Google hoặc Email truyền thống an toàn qua Firebase Authentication.
- **Single Wallet Mode (Chế độ 1 Ví duy nhất)**: Tối giản hóa tối đa trải nghiệm người dùng. Khi đăng nhập lần đầu, hệ thống tự động cấp phát một ví duy nhất tên là **"Ví chính"**. Mọi thao tác thu chi sẽ tự động liên kết với ví này, loại bỏ các thao tác chuyển khoản hay chọn ví phức tạp.
- **Transactions**: Ghi chép chi tiết các giao dịch thu, chi, gắn nhãn danh mục, ghi chú thời gian thực.
- **Budgeting**: Thiết lập hạn mức chi tiêu theo từng danh mục, tự động tính toán tiến độ sử dụng và đẩy cảnh báo vượt hạn mức (80%, 100%).
- **Reports & Analytics**: Thống kê trực quan với biểu đồ hình quạt (phân bổ chi tiêu theo danh mục) và biểu đồ cột (xu hướng thu chi) sử dụng thư viện MPAndroidChart.

## Future AI Roadmap (Tạm hoãn)
1. **OCR Pipeline (Quét hóa đơn)**: Nhận diện văn bản cục bộ bằng ML Kit OCR -> Backend trích xuất số tiền, ngày, cửa hàng -> Gợi ý danh mục tự động bằng Random Forest.
2. **YOLO Pipeline (Quét sản phẩm)**: Nhận diện vật thể qua camera bằng YOLO TFLite cục bộ -> Điền tên nhanh -> Người dùng nhập giá -> Tự động lưu giao dịch.

## Database Schema
Hệ thống sử dụng các bảng cơ sở dữ liệu sau:
- `users`: Thông tin người dùng đồng bộ từ Firebase UID (hỗ trợ nhà cung cấp email, google, facebook).
- `accounts`: Chứa ví của người dùng (tự động khởi tạo duy nhất 1 "Ví chính" khi tạo tài khoản).
- `categories`: Danh mục phân loại thu chi (mặc định hoặc tự tạo).
- `transactions`: Lưu chi tiết giao dịch thu, chi gắn liền với Ví chính.
- `budgets`: Thiết lập giới hạn chi tiêu theo danh mục.
- `notifications`: Hệ thống thông báo nhắc nhở, cảnh báo ngân sách.

## Core Rules
- **Do not change database schema** unless requested. Giữ nguyên MySQL schema của `database/schema.sql`.
- **Do not convert Java to Kotlin** cho ứng dụng Android. Giữ nguyên toàn bộ mã nguồn là Java.
- **Do not replace Spring Boot** với bất kỳ backend framework nào khác.
- **Strictly use MVVM structure** cho Android và **Layered Architecture** cho Spring Boot.
