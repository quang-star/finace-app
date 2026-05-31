# Project Progress: Milestones & Tasks

Tài liệu này ghi nhận toàn bộ tiến độ dự án, các cột mốc đã hoàn thành, những tính năng đang phát triển và lộ trình hoàn thiện hệ thống Personal Finance App.

---

## 📊 Trạng thái Dự án (Project Status at a Glance)

*   **Tái cấu trúc MVVM Android**: 100% Hoàn thành
*   **Thiết lập cấu trúc Agent Memory**: 100% Hoàn thành
*   **Nâng cấp Giao diện CapMoney Obsidian-Dark**: 100% Hoàn thành
*   **Tích hợp Auth (Email, Google, Facebook)**: 100% Hoàn thành
*   **Hạ tầng Core Android UI & API (Chế độ 1 Ví)**: 100% Hoàn thành
*   **Backend Spring Boot Core & Firebase Sync**: 100% Hoàn thành
*   **Nhánh AI YOLO Product Recognition**: Tạm hoãn (On Hold - Bảo toàn mã nguồn, ẩn UI)
*   **Nhánh AI Scan Bill (OCR + Random Forest)**: Tạm hoãn (On Hold - Bảo toàn mã nguồn, ẩn UI)

---

## 🎯 Cột mốc & Checklists Chi tiết

### Cột mốc 1: Tái cấu trúc MVVM Android (`PersonalFinanceApp`)
- [x] Cài đặt dependencies `lifecycle-viewmodel` và `lifecycle-livedata` trong `app/build.gradle`.
- [x] Tạo gói `repository/` và triển khai 4 tầng Repository giao tiếp API:
    - [x] `AuthRepository.java` (Đồng bộ user).
    - [x] `TransactionRepository.java` (Các giao dịch, báo cáo, scan feedback).
    - [x] `AccountRepository.java` (Ví, tài khoản).
    - [x] `BudgetRepository.java` (Ngân sách chi tiêu).
- [x] Tạo gói `viewmodel/` và xây dựng 5 ViewModel quản lý trạng thái LiveData:
    - [x] `AuthViewModel.java` (Trạng thái đăng nhập/ký).
    - [x] `HomeViewModel.java` (Thống kê và giao dịch gần đây của Dashboard).
    - [x] `TransactionViewModel.java` (Danh sách, thêm/sửa/xóa giao dịch, chuyển khoản).
    - [x] `BudgetViewModel.java` (Quản lý ngân sách và cảnh báo hạn mức).
    - [x] `AccountViewModel.java` (CRUD ví tài khoản, thông tin Profile).
- [x] Cập nhật các Views (Activities/Fragments) để observe LiveData:
    - [x] `LoginActivity.java` & `RegisterActivity.java`
    - [x] `HomeFragment.java`
    - [x] `TransactionFragment.java` & `AddTransactionFragment.java`
    - [x] `BudgetFragment.java` & `AddBudgetFragment.java`
    - [x] `ProfileFragment.java` & `AddAccountFragment.java`
- [x] Kiểm thử đóng gói bằng Gradle: Biên dịch debug APK (`gradlew.bat assembleDebug`) thành công 100%, không phát sinh lỗi biên dịch.

---

### Cột mốc 2: Thiết lập Agent Memory (Memory Bank)
- [x] Tạo thư mục `memory-bank/` tại gốc dự án.
- [x] Tạo tệp `productContext.md` (Bối cảnh sản phẩm).
- [x] Tạo tệp `systemPatterns.md` (Kiến trúc & Các pattern áp dụng).
- [x] Tạo tệp `decisionLog.md` (Lịch sử quyết định kỹ thuật).
- [x] Tạo tệp `activeContext.md` (Trạng thái hiện tại và tiêu điểm).
- [x] Tạo tệp `progress.md` (Theo dõi tiến độ & Checklists).
- [x] Tạo tệp `.clinerules` (Chỉ dẫn AI cho Roo Code / Cline).
- [x] Tạo tệp `.cursorrules` (Chỉ dẫn AI cho Cursor / Windsurf).

---

### Cột mốc 2.5: Nâng cấp Giao diện CapMoney Obsidian-Dark (Android Client)
- [x] Cập nhật hệ thống màu sắc và theme tối obsidian trong `colors.xml` và `themes.xml`.
- [x] Tái thiết kế 5 Fragment chính khớp nối 100% với 5 ảnh chụp màn hình thực tế:
    - [x] **Trang chủ (HomeFragment)**: Lời chào động, grid Lịch tháng recycler, wallet chips, split thu/chi cards, và nút FAB.
    - [x] **Thống kê (TransactionFragment)**: Doughnut Chart 78% thin ring, progress indicators cho danh mục chi tiêu.
    - [x] **Tài khoản (AccountFragment)**: Segmented tabs ví/khoản vay, card tổng số dư lớn, danh sách ví kèm icon và dấu sao ví mặc định.
    - [x] **Ngân sách (BudgetFragment)**: Card thêm ngân sách viền nét đứt mượt, empty placeholder card với wallet icon mờ.
    - [x] **Cá nhân (ProfileFragment)**: Glowing circular avatar, edit pencil icon, grid tổng quan 2x2, premium banner card, Apple ID & Friends rows.
- [x] Khắc phục triệt để lỗi import thư viện đồ thị `MPAndroidChart` và biên dịch Gradle thành công 100% (`BUILD SUCCESSFUL`).

---

### Cột mốc 2.7: Tích hợp Facebook Login & Đơn giản hóa Ví mặc định (One Wallet Flow)
- [x] Tích hợp thành công **Facebook Login SDK** qua Firebase Auth trên Android.
- [x] Phát triển nút đăng nhập Facebook Obsidian-Dark trên `LoginActivity.java`.
- [x] Cấu hình `TokenInterceptor` tự động đẩy ID Token lên Backend.
- [x] Triển khai verify Firebase token phía backend bằng Firebase Admin SDK, giải mã và đồng bộ an toàn (nhận dạng provider thực tế từ token).
- [x] Phát triển logic tự động tạo **"Ví chính"** mặc định (`balance = 0`) khi user mới đăng nhập lần đầu.
- [x] Ẩn chọn ví trên `AddTransactionFragment.java` và tự động phân bổ vào "Ví chính" ở phía máy chủ Spring Boot.
- [x] Ẩn các tính năng Chuyển khoản và quản lý nhiều ví ở UI để tối giản hóa nghiệp vụ.
- [x] Biên dịch Gradle Android và Maven Spring Boot thành công 100%.

---

### Cột mốc 3: Module AI Nhận diện Sản phẩm YOLO (Android & Backend)
- [-] Chuẩn bị tệp mô hình `yolo_product.tflite` và danh mục nhãn `product_labels.txt` trong thư mục `assets/` của Android. (Tạm hoãn)
- [-] Xây dựng lớp AI Helper `YoloDetector.java` để thực thi mô hình TFLite và lọc kết quả qua thuật toán NMS. (Tạm hoãn)
- [-] Thiết kế View vẽ bounding box đè lên camera preview (`BoundingBoxOverlay.java`). (Tạm hoãn)
- [-] Hoàn thiện giao diện CameraX `ScanProductActivity.java` và tích hợp form nhập giá khi nhận dạng sản phẩm thành công. (Tạm hoãn)
- [-] Tạo API backend `POST /api/ai-product/classify` nhận diện danh mục từ tên sản phẩm YOLO. (Tạm hoãn)
- [-] Tạo API backend `POST /api/ai-product/log` lưu trữ vết quét của YOLO. (Tạm hoãn)

---

### Cột mốc 4: Module AI Scan Bill & Random Forest (Android & Backend)
- [-] Tích hợp thư viện Google ML Kit Text Recognition cục bộ trên Android. (Tạm hoãn)
- [-] Thiết kế logic xử lý ảnh và chụp hình `ScanBillActivity.java`. (Tạm hoãn)
- [-] Phát triển công cụ lọc chuỗi Regex `BillParser.java` trích xuất thông tin tổng tiền, ngày hóa đơn và tên cửa hàng từ text thô. (Tạm hoãn)
- [-] Tích hợp thư viện học máy `Smile` (Smile Random Forest) vào Spring Boot backend. (Tạm hoãn)
- [-] Viết `ExpenseClassifierService.java` huấn luyện mô hình dựa trên tệp `initial_training_data.csv` và dữ liệu giao dịch thực tế của người dùng. (Tạm hoãn)
- [-] Hiện thực hóa API phân loại `POST /api/ai-scan/classify`. (Tạm hoãn)
- [-] Hiện thực hóa API phản hồi `POST /api/ai-scan/feedback` hỗ trợ user sửa lỗi và lưu mẫu học mới để tự động cập nhật độ chính xác (retrain). (Tạm hoãn)
