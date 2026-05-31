# Current Tasks

Tài liệu này ghi nhận danh sách các đầu việc cần triển khai cho toàn bộ hệ thống quản lý tài chính cá nhân (Cập nhật: Tích hợp Đăng nhập Facebook, Tối giản 1 Ví duy nhất, Tạm hoãn các Module AI).

---

## 🛠️ Phase 0: Repo Structuring & Agent Configuration (Hoàn thành)
- [x] Phân tích kho mã nguồn hiện có và xác nhận cấu trúc thư mục
- [x] Tạo tập tin cấu hình ràng buộc AI `AGENT_RULES.md`
- [x] Cập nhật tổng quan ngữ cảnh dự án `PROJECT_CONTEXT.md`
- [x] Tạo tập tin quản lý nhiệm vụ `TASKS.md`
- [x] Bổ sung thư mục tài liệu đặc tả `docs/` (`DATABASE.md`, `API.md`, `AI_PIPELINE.md`, `ANDROID_STRUCTURE.md`, `BACKEND_STRUCTURE.md`, `UI_FLOW.md`)
- [x] Bổ sung thư mục bộ nhớ kỹ thuật `memory-bank/` (`ai_notes.md`, `architecture_decisions.md`, `category_rules.md`, `future_features.md`)
- [x] Thiết lập `.gitignore` đầy đủ cho Android, Backend và AI Python/ML

---

## 📱 Phase 1: Android Java Foundation & Auth (Bổ sung Đăng nhập Facebook) (Hoàn thành)
- [x] Tích hợp Firebase Auth SDK, Google Sign-In SDK và **Facebook Login SDK**
- [x] Thiết kế giao diện Obsidian-Dark cao cấp cho `LoginActivity.java` (Bao gồm các nút Đăng nhập bằng Email, nút Google và nút Facebook)
- [x] Tạo `FirebaseAuthHelper.java` đóng gói logic đăng nhập cho cả 3 nhà cung cấp (Email, Google, Facebook)
- [x] Tạo `SplashActivity.java` kiểm tra trạng thái đăng nhập, tự động chuyển tiếp thông minh
- [x] Cấu hình OkHttp `TokenInterceptor.java` tự động đính kèm Firebase ID Token vào Header Authorization
- [x] Khởi tạo `RetrofitClient.java` kết nối với API địa phương của máy chủ Spring Boot

---

## ⚙️ Phase 2: Backend Spring Boot REST API Foundation (Đồng bộ Facebook & Khởi tạo Ví mặc định) (Hoàn thành)
- [x] Cài đặt kết nối MySQL trong `application.properties`
- [x] Định nghĩa các JPA Entity khớp 100% với `schema.sql` (Giữ nguyên cấu trúc bảng accounts và transactions để tương thích ngược)
- [x] Tạo `FirebaseConfig.java` khởi tạo Firebase Admin SDK với Service Account key
- [x] Triển khai `FirebaseAuthFilter.java` chặn, giải mã Firebase token, hỗ trợ lấy thông tin từ cả Google và Facebook logins
- [x] **Logic tự động đồng bộ & tạo Ví mặc định**:
  - [x] Đồng bộ user mới đăng nhập lần đầu vào MySQL, lưu `auth_provider = 'facebook'` nếu đăng nhập bằng Facebook
  - [x] Tự động tạo và lưu 1 tài khoản/ví mặc định duy nhất tên là **"Ví chính"** gắn với user đó trong bảng `accounts`
- [x] Viết `SecurityConfig.java` bảo mật phân quyền hệ thống

---

## 💼 Phase 3: Core Financial Features (Tối giản 1 Ví duy nhất, Ẩn chọn Ví, Không Chuyển khoản) (Hoàn thành)
- [x] [Backend] Hoàn thiện `TransactionService.java` (Logic thu chi cộng trừ tiền áp dụng trực tiếp cho "Ví chính" mặc định của user)
- [x] [Backend] Hoàn thiện `TransactionController.java` cung cấp API CRUD giao dịch và lọc theo thời gian
- [x] [Android] **Tối giản hóa giao diện**:
  - [x] Ẩn hoàn toàn tính năng quản lý nhiều ví trên màn hình Cá nhân. Chỉ hiển thị số dư tổng của "Ví chính" duy nhất
  - [x] Thiết kế biểu mẫu BottomSheet `AddTransactionFragment.java` **ẩn hoàn toàn bộ chọn ví** (mặc định gửi ID "Ví chính" khi tạo giao dịch)
  - [x] Loại bỏ hoàn toàn tính năng Chuyển tiền giữa các ví (Không dùng bảng `transfer_groups`, không dùng `TransferService` hay `TransferController`)
- [x] [Android] Thiết kế lưới Lịch tháng tại Trang chủ hiển thị tóm tắt thu chi từng ngày
- [x] [Android] Triển khai RecyclerView hiển thị danh sách lịch sử giao dịch tại `TransactionFragment.java`

---

## 📊 Phase 4: Budget, Report & Alerts (Giữ nguyên) (Hoàn thành)
- [x] [Backend] Triển khai `BudgetService.java` tự động kiểm tra tiến độ tiêu dùng ngân sách của ví duy nhất
- [x] [Android] Thiết kế progress bar trực quan tiến độ sử dụng ngân sách tại `BudgetFragment.java`
- [x] [Android] Thiết lập hệ thống cảnh báo đẩy (Push/In-app) khi chi tiêu đạt ngưỡng 80% hoặc 100% ngân sách
- [x] [Backend] Phát triển `ReportService.java` tổng hợp dữ liệu thu chi theo nhóm danh mục của ví duy nhất
- [x] [Android] Triển khai đồ thị tròn PieChart phân bổ chi tiêu và BarChart xu hướng thu chi bằng thư viện `MPAndroidChart` tại `ReportFragment.java`

---

## ⏳ Phase 5 & Phase 6: AI OCR Scanning & YOLO Product Scan (Tạm hoãn - Chưa làm đợt này)
- [ ] [Tạm hoãn] Triển khai tính năng chụp hóa đơn AI OCR Scan Bill
- [ ] [Tạm hoãn] Triển khai tính năng quét sản phẩm AI YOLO Product Scan
