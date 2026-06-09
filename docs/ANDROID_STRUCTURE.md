# Android App Architecture & Structure

Tài liệu này đặc tả cấu trúc mã nguồn và kiến trúc **MVVM (Model-View-ViewModel)** áp dụng trên ứng dụng Android Java (`PersonalFinanceApp`).

---

## 1. Kiến trúc MVVM (Architecture Design)

Ứng dụng tuân thủ nghiêm ngặt mô hình kiến trúc MVVM thuần Android Java để đảm bảo sự tách biệt giữa logic giao diện, logic nghiệp vụ và nguồn dữ liệu:

```
+-------------------------------------------------------------+
|                          VIEW LAYER                         |
|  [Activities] / [Fragments] / [XML Layouts]                 |
|  - Trách nhiệm duy nhất: Hiển thị UI và tiếp nhận tương tác |
+------------------------------+------------------------------+
                               |
                               | (Quan sát LiveData)
                               v
+-------------------------------------------------------------+
|                       VIEWMODEL LAYER                       |
|  [ViewModels]                                               |
|  - Giữ trạng thái UI bằng LiveData                           |
|  - Xử lý các logic điều hướng dòng dữ liệu                  |
+------------------------------+------------------------------+
                               |
                               | (Gọi hàm lấy dữ liệu)
                               v
+-------------------------------------------------------------+
|                      REPOSITORY LAYER                       |
|  [Repositories]                                             |
|  - Đầu mối duy nhất quản lý dữ liệu (Single Source of Truth)|
|  - Điều phối giữa Retrofit API Client và Cache (nếu có)     |
+------------------------------+------------------------------+
                               |
                               | (Gọi API HTTP REST)
                               v
+-------------------------------------------------------------+
|                         DATA LAYER                          |
|  [Retrofit Client / Firebase SDK]                           |
+-------------------------------------------------------------+
```

### Các quy tắc cốt lõi:
1. **View không trực tiếp xử lý dữ liệu**: Mọi Fragment và Activity chỉ lắng nghe (`observe`) các thay đổi từ `LiveData` trong ViewModel để cập nhật giao diện.
2. **ViewModel không chứa tham chiếu Context**: Không lưu trữ các đối tượng View hoặc Context trong ViewModel để tránh rò rỉ bộ nhớ (Memory Leak).
3. **Repository quản lý nguồn dữ liệu**: ViewModel không trực tiếp gọi API của Retrofit, mọi yêu cầu phải đi qua lớp Repository tương ứng.

---

## 2. Cấu trúc thư mục mã nguồn (Package Structure)

Mã nguồn Java của ứng dụng tại `app/src/main/java/com/example/personalfinanceapp/` được tổ chức ngăn nắp theo cấu trúc module chức năng:

```txt
com.example.personalfinanceapp/
│
├── activities/                   # Các màn hình Activity chính (Launcher, Auth, Main Host)
│   ├── SplashActivity.java       # Màn hình khởi động, điều phối đăng nhập
│   ├── LoginActivity.java        # Form đăng nhập Firebase/Google
│   ├── RegisterActivity.java     # Form đăng ký tài khoản mới
│   ├── MainActivity.java         # Activity chủ chứa BottomNavigationView và nạp Fragments
│   ├── ScanBillActivity.java     # Màn hình camera quét hóa đơn OCR
│   └── ScanProductActivity.java  # Màn hình camera quét vật thể YOLO
│
├── fragments/                    # Các Fragments đại diện cho các màn hình chức năng con
│   ├── home/                     # Module Trang chủ (Lưới lịch tháng, Dashboard số dư)
│   │   ├── HomeFragment.java
│   │   └── HomeViewModel.java
│   ├── transaction/              # Module Quản lý giao dịch thu/chi
│   │   ├── TransactionFragment.java
│   │   ├── AddTransactionFragment.java  # Dialog/BottomSheet thêm giao dịch
│   │   └── TransactionViewModel.java
│   ├── account/                  # Module Quản lý Ví/Tài khoản
│   │   ├── AccountFragment.java
│   │   └── AccountViewModel.java
│   ├── category/                 # Module Quản lý Danh mục
│   │   ├── CategoryFragment.java
│   │   └── CategoryViewModel.java
│   ├── budget/                   # Module Lập ngân sách và Tiến độ chi tiêu
│   │   ├── BudgetFragment.java
│   │   └── BudgetViewModel.java
│   ├── report/                   # Module Thống kê biểu đồ (MPAndroidChart)
│   │   ├── ReportFragment.java
│   │   └── ReportViewModel.java
│   └── profile/                  # Module Cá nhân, Thiết lập hệ thống, Đăng xuất
│       └── ProfileFragment.java
│
├── repositories/                 # Các lớp đầu mối lấy dữ liệu kết nối ViewModel với Retrofit API
│   ├── UserRepository.java
│   ├── AccountRepository.java
│   ├── TransactionRepository.java
│   ├── BudgetRepository.java
│   ├── ReportRepository.java
│
├── models/                       # Các thực thể dữ liệu (Data Models / POJOs / DTOs)
│   ├── User.java
│   ├── Account.java
│   ├── Category.java
│   ├── Transaction.java
│   ├── Budget.java
│
├── adapters/                     # Bộ điều phối hiển thị dữ liệu lên RecyclerView
│   ├── TransactionAdapter.java
│   ├── CategoryAdapter.java
│   └── HorizontalAccountAdapter.java
│
├── api/                          # Cấu hình mạng kết nối HTTP REST
│   ├── RetrofitClient.java       # Khởi tạo Retrofit instance (Singleton)
│   ├── ApiService.java           # Định nghĩa các REST endpoints và cuộc gọi HTTP
│   └── TokenInterceptor.java     # Tự động gắn Firebase ID Token vào Header Authorization
│
├── firebase/                     # Tiện ích xác thực Firebase Authentication
│   ├── FirebaseAuthHelper.java   # Hỗ trợ Đăng nhập, Đăng ký, Đăng nhập Google
│   └── FirebaseAuthCallback.java # Interface nhận phản hồi sự kiện Auth
│
├── ai/                           # Module xử lý trí tuệ nhân tạo cục bộ (Edge AI)
│   ├── ocr/                      # Google ML Kit Text Recognition
│   │   ├── OcrProcessor.java     # Chạy nhận diện chữ thô từ ảnh
│   │   └── BillParser.java       # Phân tích Regex trích xuất số tiền, ngày, merchant
│   ├── yolo/                     # TensorFlow Lite Object Detection
│   │   ├── YoloDetector.java     # Load mô hình .tflite, preprocess, postprocess NMS
│   │   └── BoundingBoxOverlay.java # Custom View vẽ khung nhận dạng thời gian thực
│   └── randomforest/             # Tiện ích tương tác bộ phân loại danh mục
│
└── utils/                        # Các lớp tiện ích dùng chung toàn ứng dụng
    ├── CurrencyFormatter.java    # Format số tiền VND (Ví dụ: 125,000 ₫)
    ├── DateUtils.java            # Biến đổi định dạng ngày tháng
    ├── SharedPrefManager.java    # Lưu cấu hình cục bộ của ứng dụng
    └── Constants.java            # Khai báo hằng số hệ thống (Base URL, keys)
```

---

## 3. Quản lý tài nguyên giao diện (Layout & Resources)

Các tệp giao diện XML tại `app/src/main/res/` được đặt tên chuẩn hóa theo tiền tố để dễ dàng tìm kiếm và quản lý:

- **Màn hình chính (Activities)**: Bắt đầu bằng `activity_` (Ví dụ: `activity_main.xml`, `activity_login.xml`, `activity_scan_bill.xml`).
- **Giao diện con (Fragments)**: Bắt đầu bằng `fragment_` (Ví dụ: `fragment_home.xml`, `fragment_report.xml`, `fragment_add_transaction.xml`).
- **Thành phần dòng hiển thị (RecyclerView Item Rows)**: Bắt đầu bằng `item_` (Ví dụ: `item_transaction.xml`, `item_account.xml`, `item_budget.xml`).
- **Bảng màu chủ đạo (Obsidian-Dark theme)**: Định nghĩa trong `values/colors.xml`, áp dụng bảng màu tối cao cấp của CapMoney (Màu nền đen Obsidian, màu chữ trắng sữa, màu nhấn xanh lục ngọc và xanh dương tài chính).
- **Hệ thống điều hướng**: Định nghĩa thực đơn điều hướng tại `menu/bottom_nav_menu.xml` nạp vào `BottomNavigationView`.
