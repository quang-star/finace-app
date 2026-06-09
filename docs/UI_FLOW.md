# User Interface & Navigation Flow

Tài liệu này mô tả chi tiết luồng trải nghiệm người dùng (UX Flow) và cơ chế điều hướng (Navigation) trong ứng dụng di động Android `PersonalFinanceApp` (Cập nhật: Bổ sung Đăng nhập Facebook, Tối giản 1 Ví duy nhất, Tạm hoãn AI).

---

## 1. Bản đồ luồng màn hình tổng quát (Overall Screen Map)

```mermaid
graph TD
    Splash["SplashActivity (Màn hình khởi động)"] -->| Chưa Đăng Nhập | Login["LoginActivity (Đăng nhập)"]
    Splash -->| Đã Đăng Nhập | Main["MainActivity (Trang chủ chính)"]
    
    Login -->| Bấm Đăng Ký | Register["RegisterActivity (Đăng ký tài khoản)"]
    Register -->| Đăng ký thành công | Login
    Login -->| Đăng nhập thành công | Main

    subgraph MainActivity Host (Bottom Navigation)
        Main --> Home["HomeFragment (Tổng quan & Lịch tháng)"]
        Main --> Transaction["TransactionFragment (Lịch sử giao dịch)"]
        Main --> Budget["BudgetFragment (Quản lý Ngân sách)"]
        Main --> Report["ReportFragment (Báo cáo biểu đồ)"]
        Main --> Profile["ProfileFragment (Cá nhân & Thông tin Ví)"]
    end
    
    Transaction -->| Bấm Nút Tròn FAB | AddTrans["AddTransactionFragment (BottomSheet Thêm mới - Ẩn chọn Ví)"]
```

---

## 2. Đặc tả chi tiết từng luồng nghiệp vụ (Business Flow Specification)

### 2.1. Luồng Khởi động & Xác thực (Auth Flow)
1. **Splash Screen**: 
   - Ứng dụng khởi động, hiển thị Logo CapMoney Obsidian-Dark trong 1.5 giây.
   - Gọi `FirebaseAuthHelper` kiểm tra xem có User Session hiện tại không.
   - **Kết quả**:
     - Nếu đã đăng nhập: Chuyển tiếp ngay tới `MainActivity`.
     - Nếu chưa đăng nhập: Chuyển tiếp tới `LoginActivity`.
2. **Đăng nhập (Login)**:
   - Người dùng đăng nhập bằng 3 phương thức linh hoạt:
     - Đăng nhập bằng Email/Password.
     - Đăng nhập nhanh bằng **Google Account**.
     - Đăng nhập nhanh bằng **Facebook Account**.
   - Hệ thống xác thực qua Firebase Auth, lấy ID Token gửi về Backend REST API `/api/auth/firebase-login` để đồng bộ user.
   - **Kết quả**: Thành công dẫn vào màn hình chính, thất bại hiển thị Toast thông báo lỗi tiếng Việt dễ hiểu.
3. **Đăng ký (Register)**:
   - Người dùng điền Họ tên, Email, Mật khẩu và Nhập lại mật khẩu để tạo tài khoản mới qua Firebase Auth.

---

### 2.2. Luồng Ghi chép giao dịch tối giản (Single Wallet Transaction Flow)
1. Từ màn hình `TransactionFragment`, người dùng nhấn vào nút tròn nổi màu ngọc lục bảo (Floating Action Button - FAB) biểu tượng dấu cộng `+`.
2. Hệ thống hiển thị một BottomSheetDialog `AddTransactionFragment` trượt nhẹ từ dưới màn hình lên:
   - **Các trường nhập liệu**: Số tiền (VND), Phân loại (Thu nhập / Chi tiêu), Danh mục (Ăn uống, Di chuyển, Mua sắm, Lương,... hiển thị dưới dạng lưới icon màu sắc sinh động), Ngày giao dịch (mặc định là hôm nay), Ghi chú.
   - **Tối giản hóa**: **Không hiển thị bộ chọn Ví / Tài khoản** (vì chỉ có 1 ví duy nhất). Khi lưu, hệ thống tự động gán ID của "Ví chính" cho giao dịch ở phần code ngầm.
3. Người dùng điền thông tin và nhấn "Lưu".
4. **Kết quả**: BottomSheet ẩn đi, gửi dữ liệu lên Backend, ví tiền mặc định cập nhật số dư, RecyclerView tại màn hình lịch sử tự động reload hiển thị giao dịch mới.

---

### 2.3. Quản lý Ví và Thông tin cá nhân (Profile & Single Wallet View)
1. Tại màn hình `ProfileFragment` (Cá nhân):
   - Hiển thị thông tin người dùng (Họ tên, Email, Ảnh đại diện đồng bộ từ Google/Facebook).
   - Hiển thị **Thông tin Ví chính** duy nhất: Tên ví ("Ví chính"), loại ví ("Tiền mặt/Mặc định"), Số dư hiện tại (được cập nhật tự động khi có thu/chi).
   - **Tối giản hóa**: Ẩn hoàn toàn nút Thêm ví mới, Sửa ví, Xóa ví và tính năng Chuyển khoản (vì chỉ có một ví duy nhất nên không cần chuyển tiền).
   - Nút "Đăng xuất" để thoát tài khoản và quay về màn hình Login.

---

### 2.4. Luồng quét AI
- Quét hóa đơn: Camera/Thư viện -> ML Kit OCR -> backend Gemini -> form thêm giao dịch.
- Quét sản phẩm: Camera/Thư viện -> YOLO/TFLite -> Random Forest local -> form thêm giao dịch.
