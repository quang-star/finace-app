# Decision Log: Architectural Decisions

Tài liệu này ghi nhận các quyết định kiến trúc và công nghệ cốt lõi đã được thống nhất và thực hiện trong dự án Personal Finance App.

---

## 📅 Lịch sử Quyết định (Decision History)

| Ngày | Quyết định | Trạng thái | Tác động |
| :--- | :--- | :--- | :--- |
| **2026-05-24** | [DR-05] Facebook Auth & One Wallet Flow | **ĐÃ THỰC HIỆN** | Tích hợp Đăng nhập Facebook, đồng bộ token an toàn phía server, tối giản giao diện thành 1 ví duy nhất ("Ví chính"), và tạm hoãn các tính năng AI OCR/YOLO |
| **2026-05-22** | [DR-04] Tái cấu trúc sang MVVM Android | **ĐÃ THỰC HIỆN** | Tách biệt hoàn toàn UI khỏi API/Logic dữ liệu, cài đặt LiveData & Repository |
| **2026-05-20** | [DR-03] Xác thực Firebase Auth Đơn giản hóa | **ĐÃ THỐNG NHẤT** | Chỉ dùng Firebase verification trên Server, loại bỏ JWT custom |
| **2026-05-20** | [DR-02] Pipeline AI Phân loại Chi tiêu | **ĐÃ THỐNG NHẤT** | OCR (ML Kit) + YOLO (TFLite on-device) + Random Forest (Smile ở Server) |
| **2026-05-20** | [DR-01] Môi trường Java 21 & Java 17 | **ĐÃ THỐNG NHẤT** | Backend sử dụng Java 21; Android app tương thích Java 17 |

---

## 🔍 Chi tiết các Quyết định Kỹ thuật

### [DR-05] Tích hợp Đăng nhập Facebook & Đơn giản hóa Ví mặc định (One Wallet Flow)
*   **Bối cảnh**: Người dùng mong muốn mở rộng các tùy chọn đăng nhập bằng Facebook (bên cạnh Email và Google), đồng thời muốn một luồng quản lý tài chính cực kỳ đơn giản trên giao diện (chỉ dùng duy nhất một ví, không cần chọn ví phức tạp hay chuyển khoản qua lại), trong khi vẫn duy trì tính tương thích ngược với cấu trúc cơ sở dữ liệu hiện có và tạm hoãn phát triển các tính năng AI.
*   **Quyết định**:
    *   **Đăng nhập Facebook**: Tích hợp Facebook Login SDK thông qua Firebase Authentication ở Android Client. Token ID được sinh ra từ Firebase sau khi đăng nhập thành công sẽ được tự động đính kèm thông qua `TokenInterceptor` lên máy chủ.
    *   **Xác thực an toàn tuyệt đối**: Backend Spring Boot trích xuất và giải mã ID Token bằng Firebase Admin SDK, tự động xác định `auth_provider` dựa trên thông tin định danh thực tế trong token (không phụ thuộc hay tin tưởng vào Request Body từ client gửi lên).
    *   **Một ví duy nhất ("Ví chính")**: Về mặt cơ sở dữ liệu, bảng `accounts` vẫn được giữ nguyên để tương thích ngược. Khi người dùng mới đồng bộ lần đầu, Backend sẽ tự động gọi `getOrCreateDefaultAccount(userId)` tạo ra một ví mặc định tên là **"Ví chính"** với số dư ban đầu là 0 VND.
    *   **Tối giản hóa Android UI**: Ẩn hoàn toàn bộ chọn ví spinner trên `AddTransactionFragment.java` (gửi request không kèm `accountId`), ẩn các tính năng chuyển khoản (Transfer) ở lịch sử giao dịch và ẩn quản lý ví ở Profile.
    *   **Xử lý Server-side**: Khi Backend nhận được yêu cầu tạo giao dịch không có `accountId`, nó tự động phân bổ giao dịch đó vào **"Ví chính"** của user và cộng/trừ số dư tương ứng.
    *   **Tạm hoãn các tính năng AI**: Các module OCR hóa đơn và YOLO nhận diện sản phẩm được đặt ở trạng thái **Tạm hoãn (On Hold)**, ẩn các entry point trên UI Android bằng cách hiển thị thông báo toast `"Coming soon!"` và giữ nguyên toàn bộ các tệp mã nguồn AI hiện tại.
*   **Hệ quả**: Hệ thống vận hành cực kỳ trơn tru, luồng nghiệp vụ tài chính được đơn giản hóa tối đa giúp tăng trải nghiệm người dùng, trong khi cơ sở dữ liệu và cấu trúc nghiệp vụ cơ bản không bị xáo trộn.

---

### [DR-04] Tái cấu trúc sang MVVM cho ứng dụng Android
*   **Bối cảnh**: Ban đầu, mã nguồn Android được viết theo lối MVC truyền thống (gọi Retrofit API và lưu dữ liệu trực tiếp trong Fragment/Activity), dẫn đến code UI bị quá tải, dễ crash khi xoay màn hình và rất khó bảo trì.
*   **Quyết định**: 
    *   Tách hoàn toàn phần giao tiếp API sang các lớp trong package `repository/`.
    *   Sử dụng `androidx.lifecycle.ViewModel` để duy trì dữ liệu qua các thay đổi vòng đời của UI.
    *   Sử dụng `LiveData` làm cầu nối phản ứng giữa Repository -> ViewModel -> View.
*   **Hệ quả**: Code UI cực kỳ gọn nhẹ và rõ ràng (chỉ còn logic vẽ giao diện và observe dữ liệu). Khắc phục triệt để hiện tượng mất dữ liệu khi xoay màn hình. Đã được kiểm chứng biên dịch thành công 100%.

---

### [DR-03] Xác thực Firebase Auth đơn giản hóa trên Backend
*   **Bối cảnh**: Cần bảo vệ các API endpoints trên Spring Boot backend nhưng vẫn giữ quy trình đơn giản nhất cho Android Client.
*   **Quyết định**: 
    *   Android Client đăng nhập qua Firebase SDK (Email hoặc Google) -> lấy Firebase ID Token.
    *   Với mỗi request API, Client gửi ID Token này ở header `Authorization: Bearer <ID_TOKEN>`.
    *   Spring Boot Backend triển khai một servlet filter xác thực gọi là `FirebaseAuthFilter` để trích xuất token này, verify thông qua **Firebase Admin SDK** và đồng bộ user vào database MySQL cục bộ. Không cần viết thêm cơ chế tự cấp phát JWT riêng của server.
*   **Hệ quả**: Bảo mật tuyệt đối dựa trên hạ tầng Firebase mà không tốn công triển khai/quản lý bảo mật phức tạp trên backend.

---

### [DR-02] Lựa chọn Pipeline AI Phân loại Chi tiêu
*   **Bối cảnh**: Cần hỗ trợ người dùng nhập liệu tự động thông qua quét hóa đơn và chụp ảnh sản phẩm nhưng cần tối ưu hóa chi phí API và đảm bảo tốc độ phản hồi.
*   **Quyết định**: 
    *   **Không dùng Gemini API**: Do chi phí gọi mạng tốn kém và phụ thuộc internet 100%.
    *   **ML Kit OCR**: Chạy nhận dạng chữ trên thiết bị di động (on-device) rất nhanh và miễn phí.
    *   **YOLO TFLite**: Thực hiện nhận diện nhãn sản phẩm cục bộ trực tiếp trên Android qua CameraX để tăng tính trải nghiệm tức thì.
    *   **Smile Library (Random Forest)**: Đặt ở Spring Boot Backend. Đây là thư viện học máy mã nguồn mở nhẹ nhàng cho Java, rất phù hợp để phân loại danh mục chi tiêu tự động dựa trên từ khóa hóa đơn/sản phẩm mà không cần cài đặt các máy chủ Python (như Flask/FastAPI) cồng kềnh.
*   **Hệ quả**: Hệ thống gọn nhẹ, chạy nhanh, độc lập về ngôn ngữ (100% viết bằng Java cho cả Mobile lẫn Backend).

---

### [DR-01] Khớp nối các phiên bản Java giữa Client và Server
*   **Bối cảnh**: Đảm bảo sự tương thích tốt nhất giữa hai môi trường chạy mã nguồn độc lập.
*   **Quyết định**:
    *   Spring Boot Backend: Sử dụng **Java 21** (LTS mới nhất tại thời điểm viết bài) nhằm tận dụng Virtual Threads và cải tiến hiệu năng Spring Boot 3.x.
    *   Android Client: Sử dụng **Java 17 compatibility** (do Android Gradle Plugin 8.x hiện tại hỗ trợ tốt nhất Java 17).
*   **Hệ quả**: Tận dụng tối đa các công nghệ hiện đại ở mỗi nền tảng mà không gặp phải bất kỳ xung đột biên dịch hay gradle nào.
