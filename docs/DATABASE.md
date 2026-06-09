# Database Specification

Nguồn schema: `database/schema.sql`.

## Các bảng

| Bảng | Mục đích |
| --- | --- |
| `users` | Người dùng đồng bộ từ Firebase |
| `accounts` | Ví và số dư |
| `categories` | Danh mục thu/chi |
| `transactions` | Giao dịch thu nhập hoặc chi tiêu |
| `budgets` | Hạn mức theo khoảng thời gian và danh mục |
| `transaction_images` | Ảnh đính kèm giao dịch |
| `ai_scan_logs` | Log OCR, kết quả Gemini và feedback |
| `recurring_transactions` | Cấu hình giao dịch định kỳ |

## Quan hệ chính

- User sở hữu accounts, transactions, budgets, ai_scan_logs và recurring_transactions.
- Transaction thuộc một account, có thể thuộc một category và có nhiều transaction_images.
- Ai scan log có thể liên kết với transaction được tạo và category người dùng xác nhận.
- Recurring transaction tạo transaction thật theo lịch chạy backend.

## Chỉ mục

- `idx_accounts_user_id`
- `idx_categories_user_id`
- `idx_transactions_user_id`
- `idx_transactions_account_id`
- `idx_transactions_transaction_date`
- `idx_budgets_user_id`

## Lưu ý triển khai

Schema nguồn không còn module chuyển tiền, thông báo, log YOLO backend hoặc ngân sách định kỳ. Việc cập nhật `schema.sql` không tự động xóa bảng/cột trên database đang chạy.
