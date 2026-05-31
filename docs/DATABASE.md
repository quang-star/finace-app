# Database Specification

Tài liệu này đặc tả chi tiết Cơ sở dữ liệu **MySQL** của dự án Personal Finance Management App, dựa trên tệp tin nguồn [database/schema.sql](file:///d:/Desktop/studywithme/nam4/ki2/Mobile/quanlythuchi/database/schema.sql).

---

## 1. Sơ đồ thực thể quan hệ (ERD Overview)

Cơ sở dữ liệu được thiết kế xoay quanh thực thể chính là `users`. Người dùng sở hữu nhiều `accounts` (ví), thiết lập các `categories` (danh mục chi tiêu), ghi chép các `transactions` (giao dịch), lập kế hoạch `budgets` (ngân sách), và ghi nhận nhật ký nhận diện AI thông qua `ai_scan_logs` và `ai_product_logs`.

---

## 2. Chi tiết các bảng (Table Schema)

### 2.1. users (Người dùng)
Lưu trữ thông tin người dùng được đồng bộ từ Firebase Authentication.

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `user_id` | INT | AUTO_INCREMENT, PRIMARY KEY | Khóa chính tự tăng của hệ thống |
| `firebase_uid` | VARCHAR(128) | UNIQUE, NULLABLE | ID duy nhất được cấp bởi Firebase Auth |
| `full_name` | VARCHAR(100) | NULLABLE | Họ và tên người dùng |
| `email` | VARCHAR(150) | NOT NULL, UNIQUE | Địa chỉ email đăng ký |
| `phone` | VARCHAR(20) | NULLABLE | Số điện thoại |
| `avatar_url` | TEXT | NULLABLE | Đường dẫn ảnh đại diện |
| `password_hash` | VARCHAR(255) | NULLABLE | Hash mật khẩu (nếu dùng auth truyền thống) |
| `auth_provider` | VARCHAR(30) | DEFAULT 'firebase' | Phương thức xác thực ('firebase', 'google') |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Ngày tạo tài khoản |
| `updated_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Ngày cập nhật gần nhất |

---

### 2.2. accounts (Tài khoản / Ví tiền)
Quản lý các tài khoản hoặc ví tài chính của người dùng (Ví dụ: Tiền mặt, Thẻ ngân hàng, Ví Momo).

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `account_id` | INT | AUTO_INCREMENT, PRIMARY KEY | Khóa chính |
| `user_id` | INT | FOREIGN KEY REFERENCES `users` | Người sở hữu ví |
| `account_name` | VARCHAR(100) | NOT NULL | Tên ví (Ví dụ: "ATM Vietcombank") |
| `account_type` | VARCHAR(50) | NULLABLE | Loại ví ("cash", "bank", "credit_card") |
| `balance` | DECIMAL(15,2) | DEFAULT 0.00 | Số dư hiện tại của ví |
| `currency` | VARCHAR(10) | DEFAULT 'VND' | Đơn vị tiền tệ |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Ngày tạo |
| `updated_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Ngày cập nhật số dư/thông tin |

---

### 2.3. categories (Danh mục Thu/Chi)
Danh mục để phân loại dòng tiền. Có thể là danh mục mặc định của hệ thống (`is_default = TRUE`, `user_id = NULL`) hoặc danh mục tự định nghĩa của từng user.

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `category_id` | INT | AUTO_INCREMENT, PRIMARY KEY | Khóa chính |
| `user_id` | INT | FOREIGN KEY REFERENCES `users`, NULLABLE | Người tạo (NULL nếu là danh mục hệ thống) |
| `category_name` | VARCHAR(100) | NOT NULL | Tên danh mục (Ví dụ: "Ăn uống", "Lương") |
| `category_type` | VARCHAR(20) | NOT NULL | Phân loại dòng tiền ("expense", "income") |
| `icon` | VARCHAR(100) | NULLABLE | Mã định danh icon giao diện |
| `color` | VARCHAR(20) | NULLABLE | Mã màu HEX để vẽ biểu đồ |
| `is_default` | BOOLEAN | DEFAULT FALSE | Đánh dấu danh mục hệ thống dùng chung |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Ngày tạo |

---

### 2.4. transfer_groups (Nhóm Chuyển tiền)
Khi người dùng chuyển khoản giữa các ví (Ví dụ: Rút tiền từ ATM về Ví tiền mặt), hệ thống sẽ tạo một bản ghi ở bảng này để liên kết 2 giao dịch (1 giao dịch giảm tiền ví gửi, 1 giao dịch tăng tiền ví nhận).

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `transfer_group_id` | INT | AUTO_INCREMENT, PRIMARY KEY | Khóa chính |
| `user_id` | INT | FOREIGN KEY REFERENCES `users` | Người thực hiện chuyển khoản |
| `from_account_id` | INT | FOREIGN KEY REFERENCES `accounts` | Ví nguồn (ví bị trừ tiền) |
| `to_account_id` | INT | FOREIGN KEY REFERENCES `accounts` | Ví đích (ví được cộng tiền) |
| `amount` | DECIMAL(15,2) | NOT NULL | Số tiền chuyển |
| `transfer_date` | DATE | NOT NULL | Ngày thực hiện |
| `note` | TEXT | NULLABLE | Ghi chú chuyển tiền |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Ngày tạo |

---

### 2.5. transactions (Giao dịch thu chi)
Bảng cốt lõi chứa toàn bộ lịch sử thu nhập, chi tiêu và chuyển khoản của người dùng.

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `transaction_id` | INT | AUTO_INCREMENT, PRIMARY KEY | Khóa chính |
| `user_id` | INT | FOREIGN KEY REFERENCES `users` | Người thực hiện |
| `account_id` | INT | FOREIGN KEY REFERENCES `accounts` | Ví tiền áp dụng |
| `category_id` | INT | FOREIGN KEY REFERENCES `categories`, NULLABLE | Danh mục (NULL nếu là chuyển tiền) |
| `transfer_group_id` | INT | FOREIGN KEY REFERENCES `transfer_groups`, NULLABLE | Nhóm chuyển tiền (nếu là giao dịch chuyển khoản) |
| `title` | VARCHAR(150) | NOT NULL | Tiêu đề giao dịch (Ví dụ: "Ăn trưa cơm tấm") |
| `amount` | DECIMAL(15,2) | NOT NULL | Số tiền giao dịch |
| `transaction_type` | VARCHAR(20) | NOT NULL | Loại giao dịch ("expense", "income", "transfer") |
| `transaction_date` | DATE | NOT NULL | Ngày giao dịch thực tế |
| `note` | TEXT | NULLABLE | Ghi chú chi tiết |
| `status` | VARCHAR(20) | DEFAULT 'confirmed' | Trạng thái ("confirmed", "draft") |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Ngày ghi nhận vào DB |
| `updated_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Ngày chỉnh sửa |

---

### 2.6. budgets (Ngân sách chi tiêu)
Hạn mức chi tiêu tối đa mà người dùng đặt ra cho một danh mục cụ thể hoặc tổng chi tiêu trong một khoảng thời gian.

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `budget_id` | INT | AUTO_INCREMENT, PRIMARY KEY | Khóa chính |
| `user_id` | INT | FOREIGN KEY REFERENCES `users` | Người thiết lập |
| `category_id` | INT | FOREIGN KEY REFERENCES `categories`, NULLABLE | Danh mục áp dụng (NULL nếu là ngân sách tổng) |
| `budget_name` | VARCHAR(100) | NOT NULL | Tên ngân sách (Ví dụ: "Ăn uống Tháng 5") |
| `amount_limit` | DECIMAL(15,2) | NOT NULL | Số tiền tối đa cho phép chi |
| `spent_amount` | DECIMAL(15,2) | DEFAULT 0.00 | Số tiền thực tế đã chi (được cập nhật liên tục) |
| `start_date` | DATE | NOT NULL | Ngày bắt đầu hiệu lực |
| `end_date` | DATE | NOT NULL | Ngày kết thúc hiệu lực |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Ngày tạo |
| `updated_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Ngày cập nhật |

---

### 2.7. transaction_images (Ảnh đính kèm giao dịch)
Lưu trữ liên kết ảnh chụp hóa đơn đi kèm các giao dịch thực tế và chuỗi ký tự nhận diện thô từ ML Kit OCR.

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `image_id` | INT | AUTO_INCREMENT, PRIMARY KEY | Khóa chính |
| `transaction_id` | INT | FOREIGN KEY REFERENCES `transactions` | Giao dịch đính kèm |
| `image_url` | TEXT | NOT NULL | Đường dẫn ảnh (Firebase Storage hoặc Local) |
| `ocr_text` | TEXT | NULLABLE | Dữ liệu văn bản thô nhận diện từ OCR |
| `uploaded_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Thời gian tải lên |

---

### 2.8. ai_scan_logs (Nhật ký Quét hóa đơn)
Lưu vết các hoạt động quét hóa đơn OCR để hỗ trợ phân loại bằng Random Forest và thực hiện vòng lặp cải thiện độ chính xác (Feedback Loop).

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `ai_scan_log_id` | INT | AUTO_INCREMENT, PRIMARY KEY | Khóa chính |
| `user_id` | INT | FOREIGN KEY REFERENCES `users` | Người dùng thực hiện |
| `transaction_id` | INT | FOREIGN KEY REFERENCES `transactions`, NULLABLE | Giao dịch được tạo từ log này (nếu có) |
| `raw_ocr_text` | TEXT | NULLABLE | Chuỗi văn bản thô quét được |
| `detected_merchant` | VARCHAR(150) | NULLABLE | Tên cửa hàng trích xuất qua Regex |
| `detected_amount` | DECIMAL(15,2) | NULLABLE | Số tiền trích xuất qua Regex |
| `detected_date` | DATE | NULLABLE | Ngày giao dịch trích xuất |
| `suggested_category_id` | INT | FOREIGN KEY REFERENCES `categories`, NULLABLE | Danh mục đề xuất bởi Random Forest |
| `actual_category_id` | INT | FOREIGN KEY REFERENCES `categories`, NULLABLE | Danh mục cuối cùng do người dùng xác nhận/chỉnh sửa |
| `was_corrected` | BOOLEAN | DEFAULT FALSE | Đánh dấu người dùng có sửa danh mục gợi ý hay không |
| `confirmed_at` | DATETIME | NULLABLE | Thời điểm người dùng xác nhận giao dịch OCR |
| `confidence_score` | DECIMAL(5,4) | NULLABLE | Độ tin cậy của dự báo (0.0000 - 1.0000) |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Ngày thực hiện |

---

### 2.9. ai_product_logs (Nhật ký Quét sản phẩm YOLO)
Lưu nhật ký khi người dùng sử dụng camera quét vật thể (YOLO) để gợi ý giao dịch.

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `ai_product_log_id` | INT | AUTO_INCREMENT, PRIMARY KEY | Khóa chính |
| `user_id` | INT | FOREIGN KEY REFERENCES `users` | Người quét |
| `transaction_id` | INT | FOREIGN KEY REFERENCES `transactions`, NULLABLE | Giao dịch được tạo thành công (nếu có) |
| `detected_product` | VARCHAR(150) | NULLABLE | Tên vật thể YOLO nhận diện (Ví dụ: "coffee") |
| `confidence_score` | DECIMAL(5,4) | NULLABLE | Độ tự tin của mô hình YOLO |
| `user_entered_price` | DECIMAL(15,2) | NULLABLE | Số tiền người dùng tự nhập tay |
| `suggested_category_id` | INT | FOREIGN KEY REFERENCES `categories`, NULLABLE | Danh mục đề xuất bởi Random Forest |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Ngày thực hiện |

---

### 2.10. recurring_transactions (Giao dịch lặp lại tự động)
Hệ thống tự động thêm giao dịch theo định kỳ được cấu hình (Ví dụ: Tiền thuê nhà 5 triệu vào ngày 1 hàng tháng).

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `recurring_id` | INT | AUTO_INCREMENT, PRIMARY KEY | Khóa chính |
| `user_id` | INT | FOREIGN KEY REFERENCES `users` | Người sở hữu |
| `account_id` | INT | FOREIGN KEY REFERENCES `accounts` | Ví áp dụng |
| `category_id` | INT | FOREIGN KEY REFERENCES `categories`, NULLABLE | Danh mục phân loại |
| `title` | VARCHAR(150) | NOT NULL | Tiêu đề giao dịch tự động |
| `amount` | DECIMAL(15,2) | NOT NULL | Số tiền lặp lại |
| `transaction_type` | VARCHAR(20) | NOT NULL | "expense" hoặc "income" |
| `repeat_type` | VARCHAR(20) | NOT NULL | Chu kỳ lặp lại ("daily", "weekly", "monthly", "yearly") |
| `repeat_interval` | INT | DEFAULT 1 | Khoảng cách chu kỳ (Ví dụ: Lặp lại mỗi 2 tháng) |
| `start_date` | DATE | NOT NULL | Ngày bắt đầu hiệu lực |
| `end_date` | DATE | NULLABLE | Ngày kết thúc (NULL nếu lặp vô hạn) |
| `next_run_date` | DATE | NOT NULL | Ngày thực thi tự động tiếp theo |
| `note` | TEXT | NULLABLE | Ghi chú giao dịch |
| `is_active` | BOOLEAN | DEFAULT TRUE | Trạng thái kích hoạt lặp |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Ngày tạo cấu hình |
| `updated_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Ngày cập nhật |

---

### 2.11. recurring_budgets (Ngân sách lặp lại tự động)
Hỗ trợ gia hạn tự động ngân sách khi hết kỳ (Ví dụ: Tự động gia hạn ngân sách "Ăn uống" mỗi tháng).

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `recurring_budget_id` | INT | AUTO_INCREMENT, PRIMARY KEY | Khóa chính |
| `user_id` | INT | FOREIGN KEY REFERENCES `users` | Người sở hữu |
| `category_id` | INT | FOREIGN KEY REFERENCES `categories`, NULLABLE | Danh mục áp dụng |
| `budget_name` | VARCHAR(100) | NOT NULL | Tên ngân sách |
| `amount_limit` | DECIMAL(15,2) | NOT NULL | Hạn mức ngân sách |
| `repeat_type` | VARCHAR(20) | NOT NULL | Chu kỳ gia hạn ("weekly", "monthly", "yearly") |
| `repeat_interval` | INT | DEFAULT 1 | Khoảng cách chu kỳ |
| `start_date` | DATE | NOT NULL | Ngày bắt đầu |
| `end_date` | DATE | NULLABLE | Ngày kết thúc |
| `next_run_date` | DATE | NOT NULL | Ngày gia hạn tiếp theo |
| `auto_create` | BOOLEAN | DEFAULT TRUE | Tự động tạo bản ghi ngân sách mới khi tới hạn |
| `is_active` | BOOLEAN | DEFAULT TRUE | Trạng thái kích hoạt |
| `note` | TEXT | NULLABLE | Ghi chú |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Ngày tạo cấu hình |
| `updated_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Ngày cập nhật |

---

### 2.12. notifications (Thông báo hệ thống)
Lưu trữ các thông báo gửi đến người dùng (cảnh báo chi tiêu, nhắc nhở định kỳ).

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `notification_id` | INT | AUTO_INCREMENT, PRIMARY KEY | Khóa chính |
| `user_id` | INT | FOREIGN KEY REFERENCES `users` | Người nhận thông báo |
| `title` | VARCHAR(150) | NOT NULL | Tiêu đề thông báo |
| `message` | TEXT | NOT NULL | Nội dung chi tiết |
| `notification_type` | VARCHAR(50) | NULLABLE | Thể loại ("budget_warning", "system") |
| `is_read` | BOOLEAN | DEFAULT FALSE | Đánh dấu đã đọc hay chưa |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Thời gian tạo thông báo |

---

## 3. Chỉ mục (Database Indexes)
Để tăng tốc truy vấn khi ứng dụng Android gọi các API với tần suất lớn, các chỉ mục sau đây đã được thiết lập:

- `idx_accounts_user_id` trên bảng `accounts(user_id)`: Tăng tốc load danh sách ví của từng user.
- `idx_categories_user_id` trên bảng `categories(user_id)`: Tăng tốc load danh mục thuộc về user.
- `idx_transactions_user_id` trên bảng `transactions(user_id)`: Tăng tốc lọc giao dịch của user.
- `idx_transactions_account_id` trên bảng `transactions(account_id)`: Hỗ trợ tính số dư ví và lọc giao dịch theo ví.
- `idx_transactions_transaction_date` trên bảng `transactions(transaction_date)`: Tối ưu bộ lọc tìm kiếm theo khoảng thời gian (ngày/tháng/năm).
- `idx_budgets_user_id` trên bảng `budgets(user_id)`: Tăng tốc kiểm tra hạn mức ngân sách của user.
- `idx_notifications_user_id` trên bảng `notifications(user_id)`: Tối ưu API tải thông báo chưa đọc.
- `idx_ai_product_logs_user_id` trên bảng `ai_product_logs(user_id)`: Hỗ trợ thống kê AI Product.

---

## 4. Các ràng buộc toàn vẹn (Integrity & Cascades)
- Khi một **User** bị xóa (`users` table), toàn bộ `accounts`, `categories`, `transfer_groups`, `transactions`, `budgets`, `ai_scan_logs`, `ai_product_logs`, `notifications`, `recurring_transactions`, `recurring_budgets` liên quan sẽ tự động bị xóa thông qua ràng buộc ngoại khóa `ON DELETE CASCADE`.
- Khi một **Transaction** bị xóa, ảnh hóa đơn liên quan (`transaction_images`) sẽ tự động xóa theo (`ON DELETE CASCADE`). Các nhật ký quét AI (`ai_scan_logs`, `ai_product_logs`) liên quan sẽ được set giá trị khóa ngoại về `NULL` (`ON DELETE SET NULL`) để giữ lại dữ liệu phục vụ phân tích máy học.
- Khi một **Category** bị xóa, các giao dịch và ngân sách liên kết sẽ được cập nhật khóa ngoại `category_id` về `NULL` (`ON DELETE SET NULL`) để tránh lỗi toàn vẹn dữ liệu.
