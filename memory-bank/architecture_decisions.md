# Architectural Decisions Record (ADR)

Tài liệu này ghi nhận các quyết định kiến trúc quan trọng đã được thống nhất và thông qua trong dự án Personal Finance Management App. Mọi thay đổi kỹ thuật lớn trong tương lai bắt buộc phải tham chiếu và tuân thủ các quyết định này.

---

## ADR 1: Lựa chọn Ngôn ngữ & Kiến trúc trên Android

- **Trạng thái**: ĐÃ DUYỆT (APPROVED).
- **Quyết định**: 
  - Ứng dụng Android bắt buộc viết hoàn toàn bằng **Java**, không sử dụng Kotlin cho các lớp logic nghiệp vụ hoặc giao diện (chỉ cho phép cấu hình Gradle viết bằng Kotlin DSL nếu cần).
  - Áp dụng kiến trúc **MVVM** đơn giản, sử dụng các thành phần thuần Android Jetpack (`LiveData`, `ViewModel`, `Repository`).
- **Lý do lựa chọn**:
  - Đảm bảo tính ổn định và tính tương thích cao với toàn bộ kho mã nguồn hiện có của dự án.
  - Phù hợp với định hướng phát triển mã nguồn dễ tiếp cận, dễ hiểu và dễ bảo trì.
  - Hạn chế các rủi ro phát sinh từ việc AI tự ý nâng cấp hoặc convert mã nguồn sang Kotlin dẫn đến compile error trên Gradle.
- **Hệ quả**:
  - Không được chuyển đổi bất kỳ file `.java` nào sang `.kt`.
  - Không được đưa các thành phần Jetpack Compose vào dự án; giữ nguyên cơ chế Fragment-based điều hướng và giao diện XML truyền thống.

---

## ADR 2: Cơ chế Xác thực Người dùng (Authentication Strategy)

- **Trạng thái**: ĐÃ DUYỆT (APPROVED).
- **Quyết định**:
  - Sử dụng giải pháp xác thực đám mây **Firebase Authentication** làm nền tảng xác thực duy nhất cho người dùng trên cả ứng dụng Android di động và hệ thống API Backend.
  - Backend Spring Boot kiểm tra tính hợp lệ của token thông qua Firebase Admin SDK trên mỗi request. **Không thiết lập hệ thống phát hành token JWT riêng** hoặc lưu mật khẩu hash trong MySQL.
- **Lý do lựa chọn**:
  - Giảm thiểu rủi ro bảo mật liên quan đến việc lưu trữ mật khẩu trực tiếp trong cơ sở dữ liệu hệ thống.
  - Tiết kiệm thời gian phát triển các tính năng đăng nhập nâng cao (Đăng nhập Google, Đăng ký xác thực email, Quên mật khẩu).
  - Tận dụng cơ chế mã hóa và hạ tầng xác thực an toàn tuyệt đối của Google Firebase.
- **Hệ quả**:
  - Android Client phải lấy được `Firebase ID Token` sau khi đăng nhập và đính kèm vào Header `Authorization: Bearer <ID_Token>` của mọi API HTTP request.
  - Backend Spring Boot cấu hình Servlet Filter `FirebaseAuthFilter` chặn mọi request để giải mã token qua Firebase Admin SDK.

---

## ADR 3: Chiến lược Triển khai Trí tuệ Nhân tạo (AI Strategy)

- **Trạng thái**: ĐÃ DUYỆT (APPROVED).
- **Quyết định**:
  - Tận dụng tối đa giải pháp **Edge AI (AI Cục bộ on-device)** cho các tác vụ Vision nặng (Nhận dạng văn bản OCR bằng Google ML Kit, nhận diện vật thể YOLOv8 bằng TensorFlow Lite) chạy trực tiếp trên điện thoại Android.
  - Sử dụng thuật toán học máy **Random Forest** của thư viện Java **Smile** tích hợp trực tiếp vào Spring Boot Backend để thực hiện phân loại danh mục chi tiêu tự động.
  - Không kết nối với các mô hình ngôn ngữ lớn (LLM API) bên ngoài như OpenAI GPT hay Google Gemini để phân loại.
- **Lý do lựa chọn**:
  - Tốc độ xử lý của Edge AI cực nhanh (thời gian thực) do không mất độ trễ truyền dữ liệu ảnh gốc dung lượng lớn qua Internet.
  - Tiết kiệm tối đa băng thông mạng cho cả người dùng và máy chủ.
  - Đảm bảo tính bảo mật và riêng tư tuyệt đối cho dữ liệu tài chính của người dùng (ảnh hóa đơn/sản phẩm không bị gửi ra ngoài internet).
  - Mô hình học máy Random Forest Java Smile nhẹ nhàng, thực thi suy luận trực tiếp trên JVM cực nhanh mà không yêu cầu hạ tầng GPU máy chủ đắt đỏ.
- **Hệ quả**:
  - AI Agent cần thiết kế các lớp Helper nhận dạng chữ ML Kit cục bộ chuẩn hóa trên Android.
  - Chuẩn bị sẵn mô hình lượng tử hóa `.tflite` trong thư mục `assets/` của ứng dụng Android cho luồng YOLO.

---

## ADR 4: Cấu trúc Cơ sở Dữ liệu & Lưu trữ (Database Engine)

- **Trạng thái**: ĐÃ DUYỆT (APPROVED).
- **Quyết định**:
  - Sử dụng hệ quản trị cơ sở dữ liệu quan hệ **MySQL** làm công cụ lưu trữ dữ liệu nghiệp vụ chính.
  - Cấu hình JPA ở Backend theo cơ chế `spring.jpa.hibernate.ddl-auto=validate`. **Tuyệt đối không sử dụng `create` hoặc `update`** để tránh việc Hibernate tự động sửa đổi hoặc xóa bảng cơ sở dữ liệu thực tế.
- **Lý do lựa chọn**:
  - Bảo vệ an toàn tuyệt đối cho tính toàn vẹn của dữ liệu nghiệp vụ (Transactions, Budgets, Accounts) thông qua các ràng buộc khóa ngoại chặt chẽ.
  - Tránh rủi ro AI Agent tự ý thay đổi schema dẫn đến xung đột hoặc lỗi mất mát dữ liệu đang có.
- **Hệ quả**:
  - Mọi sự thay đổi về cấu trúc bảng bắt buộc phải được người dùng đồng ý và cập nhật thủ công thông qua tập lệnh nguồn [database/schema.sql](file:///d:/Desktop/studywithme/nam4/ki2/Mobile/quanlythuchi/database/schema.sql).
