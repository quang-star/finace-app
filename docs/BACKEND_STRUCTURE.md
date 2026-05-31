# Backend Architecture & Structure

Tài liệu này đặc tả chi tiết kiến trúc phân tầng (Layered Architecture) áp dụng trên Spring Boot Backend REST API (`finance-backend`).

---

## 1. Kiến trúc Phân tầng (Layered Architecture)

Dự án áp dụng mô hình kiến trúc phân tầng kinh điển, đảm bảo tính tách biệt trách nhiệm (Separation of Concerns), dễ bảo trì, dễ viết unit test và an toàn khi nâng cấp:

```
                  +-----------------------------------+
                  |           CLIENT LAYER            |
                  |     (Android Application)         |
                  +-----------------+-----------------+
                                    |
                                    | (Yêu cầu HTTP REST)
                                    v
                  +-----------------------------------+
                  |         CONTROLLER LAYER          |
                  |  - REST Controllers (@RestController) |
                  |  - Xác thực & Phân quyền Security |
                  |  - Kiểm tra tính hợp lệ DTO       |
                  +-----------------+-----------------+
                                    |
                                    | (DTOs / Requests)
                                    v
                  +-----------------------------------+
                  |           SERVICE LAYER           |
                  |  - Business Services (@Service)   |
                  |  - Quản lý giao dịch dữ liệu     |
                  |  - Tích hợp Smile ML Library      |
                  +-----------------+-----------------+
                                    |
                                    | (Entities / JPA)
                                    v
                  +-----------------------------------+
                  |         REPOSITORY LAYER          |
                  |  - Spring Data JPA Interfaces     |
                  |  - Tương tác MySQL Database       |
                  +-----------------+-----------------+
                                    |
                                    | (MySQL Queries)
                                    v
                  +-----------------------------------+
                  |          DATABASE LAYER           |
                  |             (MySQL)               |
                  +-----------------------------------+
```

### Các lớp cốt lõi:
1. **Controller Layer (Tầng kiểm soát)**: Tiếp nhận các request từ Android Client, chuyển tiếp đến tầng Service thích hợp và trả về cấu trúc `ApiResponse` chuẩn hóa dưới dạng JSON.
2. **Service Layer (Tầng nghiệp vụ)**: Nơi chứa toàn bộ logic xử lý nghiệp vụ của hệ thống (Tính toán số dư, xử lý nghiệp vụ lặp lại, gọi các thuật toán học máy Random Forest). Tầng này độc lập hoàn toàn với cách thức hiển thị giao diện hay lưu trữ DB vật lý.
3. **Repository Layer (Tầng truy xuất dữ liệu)**: Kế thừa từ `JpaRepository` của Spring Data JPA để tự động hóa các câu lệnh SQL CRUD. Tận dụng cơ chế Hibernate để tự động ánh xạ quan hệ đối tượng (ORM).

---

## 2. Cấu trúc thư mục mã nguồn (Package Structure)

Mã nguồn Java của ứng dụng Spring Boot tại `src/main/java/com/example/financebackend/` được tổ chức sạch sẽ như sau:

```txt
com.example.financebackend/
│
├── FinanceBackendApplication.java # Lớp chạy chính của ứng dụng Spring Boot
│
├── controller/                   # Nơi định nghĩa các REST API Endpoints
│   ├── AuthController.java       # POST /api/auth/firebase-login
│   ├── UserController.java       # GET/PUT /api/users
│   ├── AccountController.java    # GET/POST/PUT/DELETE /api/accounts
│   ├── CategoryController.java   # GET/POST/PUT/DELETE /api/categories
│   ├── TransactionController.java # GET/POST/PUT/DELETE /api/transactions
│   ├── TransferController.java   # POST /api/transfers
│   ├── BudgetController.java     # GET/POST/PUT/DELETE /api/budgets
│   ├── ReportController.java     # GET /api/reports/...
│   ├── NotificationController.java # GET/PUT /api/notifications
│   ├── AiScanController.java     # POST /api/ai-scan/...
│   └── AiProductController.java  # POST /api/ai-product/...
│
├── service/                      # Nơi thực thi logic nghiệp vụ (Business Logic)
│   ├── UserService.java          # Đồng bộ dữ liệu người dùng từ Firebase
│   ├── AccountService.java       # CRUD ví, cập nhật số dư thực tế
│   ├── CategoryService.java      # CRUD danh mục, nạp danh mục hệ thống
│   ├── TransactionService.java   # CRUD giao dịch, tự động cập nhật số dư ví tương ứng
│   ├── TransferService.java      # Xử lý giao dịch chuyển tiền giữa hai ví
│   ├── BudgetService.java        # CRUD ngân sách, kiểm tra tiến độ chi tiêu
│   ├── ReportService.java        # Tổng hợp báo cáo, vẽ biểu đồ tròn/cột
│   ├── NotificationService.java  # Tạo thông báo nhắc nhở, cảnh báo ngân sách
│   └── ExpenseClassifierService.java # Tích hợp Random Forest để gợi ý danh mục
│
├── repository/                   # Tương tác cơ sở dữ liệu qua Spring Data JPA
│   ├── UserRepository.java
│   ├── AccountRepository.java
│   ├── CategoryRepository.java
│   ├── TransactionRepository.java
│   ├── BudgetRepository.java
│   ├── NotificationRepository.java
│   ├── TransferGroupRepository.java
│   ├── AiScanLogRepository.java
│   └── AiProductLogRepository.java
│
├── model/                        # Các thực thể cơ sở dữ liệu (JPA Entities)
│   ├── User.java                 # Ánh xạ bảng `users`
│   ├── Account.java              # Ánh xạ bảng `accounts`
│   ├── Category.java             # Ánh xạ bảng `categories`
│   ├── Transaction.java          # Ánh xạ bảng `transactions`
│   ├── Budget.java               # Ánh xạ bảng `budgets`
│   ├── TransferGroup.java        # Ánh xạ bảng `transfer_groups`
│   ├── TransactionImage.java     # Ánh xạ bảng `transaction_images`
│   ├── Notification.java         # Ánh xạ bảng `notifications`
│   ├── AiScanLog.java            # Ánh xạ bảng `ai_scan_logs`
│   └── AiProductLog.java         # Ánh xạ bảng `ai_product_logs`
│
├── dto/                          # Các đối tượng truyền dữ liệu (Data Transfer Objects)
│   ├── ApiResponse.java          # Cấu trúc JSON phản hồi chuẩn hóa toàn hệ thống
│   ├── UserDTO.java
│   ├── TransactionDTO.java
│   ├── BudgetDTO.java
│   ├── ReportDTO.java
│   ├── LoginRequest.java
│   ├── TransferRequest.java
│   └── CategoryPrediction.java   # DTO trả về kết quả dự báo của AI
│
├── config/                       # Các lớp cấu hình hệ thống
│   ├── FirebaseConfig.java       # Khởi tạo Firebase Admin SDK với Service Account
│   ├── FirebaseAuthFilter.java   # Filter chặn và verify Firebase ID Token trên mỗi Request
│   ├── SecurityConfig.java       # Cấu hình bảo mật phân quyền Spring Security
│   └── CorsConfig.java           # Cho phép Android Client local gọi API
│
└── ai/                           # Module máy học của Backend (Smile Integration)
    ├── TextFeatureExtractor.java # Chuyển văn bản thô OCR thành véc-tơ đặc trưng
    ├── ModelTrainer.java         # Huấn luyện mô hình Random Forest từ dữ liệu MySQL
    └── TrainingDataService.java  # Chuẩn bị bộ dữ liệu huấn luyện (Dataset)
```

---

## 3. Hệ thống xác thực bằng Firebase Admin SDK

Backend không thiết lập cơ chế đăng ký mật khẩu truyền thống hay tạo khóa token JWT riêng để giảm tải quản lý bảo mật.
1. **Xác thực Client-Side**: Ứng dụng Android thực hiện đăng nhập trực tiếp với Google Firebase Auth (qua Email/Password hoặc Google Sign-in). Khi thành công, Firebase cấp cho Client một khóa bảo mật ngắn hạn gọi là **Firebase ID Token**.
2. **Ủy quyền Server-Side**:
   - Khi Android gọi REST API, nó gửi ID Token này ở Header `Authorization: Bearer <Token>`.
   - Lớp `FirebaseAuthFilter.java` của Spring Boot đánh chặn request, lấy Token ra và gọi hàm `FirebaseAuth.getInstance().verifyIdToken(token)` của Firebase Admin SDK để giải mã.
   - Nếu token hợp lệ, lấy thông tin `firebase_uid`, tìm kiếm người dùng trong MySQL DB (nếu chưa có thì tự động tạo mới - cơ chế Đồng bộ tự động) và đưa đối tượng User này vào `SecurityContextHolder` của Spring Security để cấp quyền thực thi API tiếp theo.
