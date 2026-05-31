# System Patterns: Architecture & Coding Standards

Tài liệu này chi tiết hóa kiến trúc hệ thống, cấu trúc dữ liệu và các quy chuẩn lập trình được áp dụng trong toàn bộ dự án Personal Finance App (cả Mobile App và Backend).

---

## 🏗️ Kiến trúc Tổng thể (Overall Architecture)

Hệ thống được thiết kế theo mô hình **Client-Server** chuẩn mực kết hợp với các dịch vụ bổ trợ đám mây và AI:

```mermaid
graph TD
    subgraph Client Layer (Android App)
        View[View: Activities/Fragments] <--> VM[ViewModel: Jetpack]
        VM <--> Repo[Repository Layer]
        Repo <--> API[Retrofit API Client]
        Repo <--> Local[SharedPrefs / Cache]
        View <--> FirebaseSDK[Firebase Auth SDK]
    end

    subgraph Cloud & Service Layer
        FirebaseSDK <--> FirebaseAuth[Firebase Authentication]
        API <--> Backend[Spring Boot REST Backend]
    end

    subgraph Backend Layer (Spring Boot)
        Controller[REST Controllers] <--> Service[Services Layer]
        Service <--> DBRepo[JPA Repositories]
        DBRepo <--> MySQL[(MySQL Database)]
        Service <--> SmileRF[Smile Random Forest Classifier]
        Service <--> FirebaseAdmin[Firebase Admin SDK]
        FirebaseAdmin <--> FirebaseAuth
    end
```

---

## 📱 1. Kiến trúc Mobile App (Android - MVVM Pattern)

Ứng dụng di động tuân thủ nghiêm ngặt mô hình **MVVM (Model-View-ViewModel)** chuẩn của Google Jetpack:

### Quy tắc phân chia trách nhiệm:
1.  **View (Activities / Fragments)**:
    *   Chỉ chịu trách nhiệm hiển thị UI, lắng nghe tương tác của người dùng.
    *   **Không** chứa logic nghiệp vụ, **không** gọi mạng hay xử lý dữ liệu thô trực tiếp.
    *   Sử dụng `ViewModelProvider` để liên kết với ViewModel và `observe` dữ liệu từ `LiveData` để tự động cập nhật UI.
2.  **ViewModel**:
    *   Kế thừa `androidx.lifecycle.ViewModel`.
    *   Nắm giữ các biến trạng thái dưới dạng `MutableLiveData` (nội bộ) và cung cấp `LiveData` (chỉ đọc) ra ngoài cho View.
    *   Giao tiếp với tầng `Repository` để yêu cầu lấy hoặc cập nhật dữ liệu.
3.  **Repository**:
    *   Đóng vai trò là nguồn dữ liệu duy nhất (Single Source of Truth) cho ViewModel.
    *   Quản lý việc gọi API từ `RetrofitClient` hoặc truy xuất cấu hình cục bộ (`SharedPrefManager`).
4.  **Model**:
    *   Định nghĩa các cấu trúc dữ liệu trao đổi (DTOs) đồng bộ với Backend.

### Quy chuẩn đặt tên & package:
*   `com.example.personalfinance.activities`: Ví dụ `LoginActivity.java`, `MainActivity.java`.
*   `com.example.personalfinance.fragments`: Ví dụ `HomeFragment.java`, `TransactionFragment.java`.
*   `com.example.personalfinance.viewmodel`: Ví dụ `TransactionViewModel.java`, `HomeViewModel.java`.
*   `com.example.personalfinance.repository`: Ví dụ `TransactionRepository.java`, `AccountRepository.java`.
*   `com.example.personalfinance.api`: `RetrofitClient.java`, `ApiService.java`.
*   `com.example.personalfinance.models`: Thực thể dữ liệu.

---

## ☕ 2. Kiến trúc Backend (Spring Boot Layered Pattern)

Phần Backend sử dụng mô hình 3 lớp truyền thống của **Spring Boot**:

1.  **Controller Layer (`com.example.financebackend.controller`)**:
    *   Định nghĩa các REST API endpoints, quản lý Request/Response và HTTP status codes.
    *   Áp dụng `@RestController` và `@RequestMapping("/api/...")`.
2.  **Service Layer (`com.example.financebackend.service`)**:
    *   Thực hiện toàn bộ nghiệp vụ logic, tính toán số dư ví, kiểm tra vượt hạn mức ngân sách, và điều phối các tác vụ AI.
    *   Được đánh dấu bằng `@Service` và thực hiện quản lý transaction thông qua `@Transactional`.
3.  **Repository Layer (`com.example.financebackend.repository`)**:
    *   Kế thừa `JpaRepository` để thao tác trực tiếp với MySQL Database một cách dễ dàng qua Spring Data JPA.
4.  **Security & Filters (`com.example.financebackend.config`)**:
    *   Sử dụng `FirebaseAuthFilter` kế thừa `OncePerRequestFilter` để chặn mọi request lên server, trích xuất Firebase ID Token từ header `Authorization: Bearer <token>`, gọi Firebase Admin SDK để xác thực trực tiếp và thiết lập Context Security cho Spring.

---

## 🛢️ 3. Thiết kế Cơ sở dữ liệu (MySQL Schema)

Cơ sở dữ liệu sử dụng bảng mã `utf8mb4_unicode_ci` để hỗ trợ hiển thị tiếng Việt hoàn hảo. Hệ thống bảng chính gồm:

*   `users`: Lưu trữ người dùng đồng bộ từ Firebase Auth qua trường duy nhất `firebase_uid`.
*   `accounts`: Ví và tài khoản tiền tệ của người dùng. Có ràng buộc khóa ngoại tới `users(user_id)`.
*   `categories`: Danh mục thu chi (mặc định cho mọi user hoặc tự định nghĩa riêng).
*   `transactions`: Lưu vết toàn bộ giao dịch tài chính.
*   `transfer_groups`: Quản lý các giao dịch luân chuyển dòng tiền qua lại giữa các ví (chứa tham chiếu `from_account_id` và `to_account_id`).
*   `budgets`: Ngân sách chi tiêu giới hạn theo danh mục và thời gian của người dùng.
*   `ai_scan_logs`: Lưu trữ nhật ký phân tích hóa đơn của AI (raw text, merchant, confidence, suggested category).
*   `ai_product_logs`: Lưu trữ nhật ký phân tích sản phẩm từ YOLO camera (product, confidence, suggested category).
*   `notifications`: Lưu thông báo hệ thống và cảnh báo chi tiêu vượt ngưỡng.

---

## 🤖 4. Mô hình Xử lý Trí tuệ Nhân tạo (AI Pipeline Patterns)

Hệ thống AI được chia thành hai nhánh xử lý tối ưu về chi phí và tài nguyên:

### Nhánh 1: YOLO Product Recognition (Nhận diện vật thể camera)
*   **Android App**: Tích hợp camera realtime qua CameraX. Load tệp `yolo_product.tflite` cục bộ chạy inference trực tiếp on-device để vẽ khung bounding box và nhãn sản phẩm.
*   **Backend Server**: Khi Android gửi tên sản phẩm kèm giá tiền lên, Backend sử dụng module `Smile Library` (Random Forest) để phân tích từ khóa và gán nhãn danh mục chi tiêu tự động.

### Nhánh 2: AI Scan Bill (OCR quét hóa đơn)
*   **Android App**: Sử dụng **Google ML Kit Text Recognition** (on-device) để nhận diện văn bản từ ảnh hóa đơn. Sử dụng `BillParser` cục bộ chạy regex để trích xuất nhanh tổng số tiền, ngày hóa đơn và tên cửa hàng.
*   **Backend Server**: Nhận toàn bộ văn bản thô (raw OCR text) từ client gửi lên, chạy trích xuất đặc trưng TF-IDF và đưa qua mô hình `Random Forest Classifier` để trả về danh mục gợi ý có điểm số tin cậy (confidence score) cao nhất.

### Vòng lặp phản hồi (AI Feedback Loop):
Khi người dùng sửa lại danh mục gợi ý của AI trước khi xác nhận giao dịch, ứng dụng sẽ gửi một yêu cầu `POST /api/ai-scan/feedback` chứa danh mục đúng về Backend để cập nhật lại dữ liệu học và kích hoạt tiến trình huấn luyện lại mô hình (Retraining) định kỳ.
