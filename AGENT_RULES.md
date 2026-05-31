# Agent Rules

Tài liệu này định nghĩa các quy tắc bảo vệ và ràng buộc cốt lõi dành cho các trợ lý lập trình AI (như Antigravity/Codex) khi tương tác với codebase. Mọi mã nguồn được tạo mới hoặc chỉnh sửa bắt buộc phải tuân thủ nghiêm ngặt các quy tắc này để đảm bảo tính an toàn và không phá vỡ cấu trúc hiện tại.

---

## 1. Android Authentication Rules (Phase 1)
- **Firebase Auth**: Sử dụng Firebase Authentication làm cổng xác thực cho cả 3 phương thức: Đăng nhập truyền thống (Email/Mật khẩu), Đăng nhập Google, và Đăng nhập Facebook.
- **Token Retrieve**: Sau khi đăng nhập thành công ở Client, Android lấy `Firebase ID Token` và chuyển sang tầng mạng.
- **OkHttp Interceptor**: Sử dụng `TokenInterceptor.java` để tự động đính kèm ID Token vào Header `Authorization: Bearer <ID_Token>` cho mọi REST request.
- **No Custom JWT**: Không tự tạo cơ chế JWT riêng hoặc lưu hash mật khẩu người dùng ở database.
- **No Kotlin transition**: Không convert hoặc thêm mới bất kỳ file logic nào bằng Kotlin. Giữ nguyên **Java** cho ứng dụng Android.

---

## 2. Backend Security & Firebase Sync Rules (Phase 2)
- **Verify Token**: Spring Boot sử dụng Firebase Admin SDK để giải mã và xác thực Firebase ID Token.
- **Safe Provider Identification**:
  - **Bắt buộc**: Backend phải tự giải mã Firebase Token và lấy thông tin nhà cung cấp xác thực (`auth_provider`) từ thông tin provider dữ liệu của Firebase (Firebase provider data).
  - **Nghiêm cấm**: **Không tin tưởng** giá trị `auth_provider` do Android gửi lên trực tiếp từ Request Body để tránh rủi ro bảo mật giả mạo.
- **Auto Account Creation**: Ngay sau khi user được đồng bộ/tạo mới lần đầu trên MySQL, Backend sẽ tự động gọi hàm khởi tạo 1 Ví mặc định (`Ví chính`, số dư ban đầu = `0 ₫`) cho User này:
  - `account_name = "Ví chính"`
  - `account_type = "cash"`
  - `balance = 0.00`
  - `currency = "VND"`
- **Required Backend Function**: Backend bắt buộc phải có hàm sau để quản lý Ví mặc định của User:
  ```java
  Account getOrCreateDefaultAccount(Integer userId)
  ```

---

## 3. One Wallet Finance Flow Rules (Phase 3 & Phase 4)
- **No Database Deletion**:
  - **Nghiêm cấm** xóa bảng `accounts` và bảng `transfer_groups` trong MySQL.
  - **Nghiêm cấm** thay đổi cấu trúc tệp tin cơ sở dữ liệu `database/schema.sql` trừ khi được yêu cầu rõ ràng.
- **No Backend Transfer Code Deletion**:
  - **Nghiêm cấm** tự ý xóa code Backend liên quan đến tính năng chuyển tiền (như `TransferService.java`, `TransferController.java` hoặc các API chuyển tiền nếu đang tồn tại). Chúng ta chỉ dừng phơi bày (stop exposing) và không gọi các API này từ ứng dụng Android.
- **Android UI Simplification**:
  - Ẩn hoàn toàn tính năng quản lý nhiều ví trên màn hình Cá nhân. Chỉ hiển thị số dư của "Ví chính".
  - Ẩn tính năng chuyển khoản trên giao diện Android.
  - Ẩn bộ chọn Ví (account picker) trên biểu mẫu BottomSheet thêm giao dịch.
- **Safe Transaction Flow (Backend-managed Wallet)**:
  - **Android Client**: Gửi dữ liệu giao dịch lên REST API **không kèm theo `account_id`** (Android không tự quyết định ví).
  - **Spring Boot Backend**:
    1. Trích xuất thông tin User hiện tại từ Firebase decoded token.
    2. Gọi hàm `getOrCreateDefaultAccount(userId)` để lấy Ví chính của user.
    3. Tự động gán `account_id` của Ví chính vào đối tượng Transaction ở phía Server.
    4. Lưu Transaction vào MySQL.
    5. Cập nhật số dư thực tế cho "Ví chính".

---

## 4. AI Deferral Rules (Phase 5 & Phase 6)
- **No Code Deletion**:
  - **Tuyệt đối không xóa** các lớp hiện có liên quan đến AI như: `ScanBillActivity.java`, `ScanProductActivity.java`, `AiScanController.java`, `AiProductController.java` hoặc bất kỳ tệp tin ML/AI nào khác nếu đang tồn tại trong hệ thống.
- **UI Exposure**: Chỉ ẩn cổng vào (entry point) của các tính năng này trên giao diện Android hoặc hiển thị nhãn "Coming Soon / Tính năng đang phát triển" để tạm hoãn triển khai.
