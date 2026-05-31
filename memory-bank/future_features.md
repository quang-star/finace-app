# Future Features & AI Roadmap

Tài liệu này ghi nhận lộ trình phát triển, các tính năng đề xuất và nâng cấp công nghệ dự kiến cho ứng dụng Personal Finance Management App trong các phiên bản tiếp theo (Phase 8 trở đi).

---

## 1. Nâng cấp Hệ thống Trí tuệ Nhân tạo (AI/ML Enhancements)

### 1.1. Tự động Tối ưu hóa Tham số Random Forest (Hyperparameter Tuning)
- **Mục tiêu**: Nâng cao độ chính xác phân loại chi tiêu lên trên 95% thông qua việc tối ưu bộ siêu tham số của Smile RandomForest.
- **Giải pháp**: Triển khai thuật toán **Grid Search** hoặc **Random Search** chạy ngầm hàng tuần trên Backend Spring Boot để tự động thử nghiệm và tìm ra cấu hình tối ưu nhất cho:
  - Số lượng cây quyết định (`nTrees`).
  - Độ sâu tối đa (`maxDepth`).
  - Ngưỡng cắt tỉa lá (`maxNodes`).
- **Thời gian dự kiến**: Phase 8.

### 1.2. Nhận diện Sản phẩm YOLOv8 Offline nâng cao (Advanced Edge YOLO)
- **Mục tiêu**: Nhận diện đồng thời nhiều sản phẩm khác nhau trong một khung hình camera (Multi-Object Detection) và tự động cộng dồn giá tiền.
- **Giải pháp**:
  - Huấn luyện lại mô hình YOLOv8-Nano với tập dữ liệu tùy chỉnh gồm 150 nhãn vật phẩm gia dụng và thực phẩm Việt Nam.
  - Sử dụng giải pháp **TensorFlow Lite Metadata** để tích hợp trực tiếp tên nhãn tiếng Việt vào trong tệp `.tflite`, tránh phụ thuộc vào tệp `product_labels.txt` bên ngoài.
  - Vẽ hộp bounding box có màu sắc thay đổi động theo từng nhóm nhãn (Ăn uống màu xanh, Mua sắm màu cam).
- **Thời gian dự kiến**: Phase 9.

### 1.3. Phân loại nâng cao bằng Mô hình Chuỗi Thời gian (Time Series Forecasting)
- **Mục tiêu**: Dự báo xu hướng chi tiêu của người dùng trong tháng tiếp theo và tự động cảnh báo nguy cơ cạn kiệt ngân sách sớm.
- **Giải pháp**:
  - Nghiên cứu và triển khai mô hình mạng nơ-ron hồi quy **LSTM (Long Short-Term Memory)** hoặc thuật toán thống kê **ARIMA** để phân tích chuỗi dữ liệu giao dịch theo thời gian.
  - Gợi ý hạn mức ngân sách thông minh (Smart Budget Suggestions) cho tháng sau dựa trên thói quen chi tiêu thực tế của các tháng trước.
- **Thời gian dự kiến**: Phase 10.

---

## 2. Các Tính năng Tiện ích mới (New Features)

### 2.1. Nhập liệu Giao dịch bằng Giọng nói (AI Voice Input)
- **Mục tiêu**: Cho phép người dùng đọc nhanh nội dung giao dịch để hệ thống tự ghi chép (Ví dụ: *"Tôi đã chi 50 nghìn ăn sáng bằng ví tiền mặt"*).
- **Giải pháp**:
  - Tích hợp thư viện **Android SpeechRecognizer** (hoặc Google Cloud Speech-to-Text API) để chuyển đổi giọng nói thành văn bản.
  - Xây dựng bộ phân tích cú pháp ngôn ngữ tự nhiên **NLP (Natural Language Processing)** đơn giản trên Spring Boot để trích xuất:
    - **Số tiền** (`50 nghìn` -> `50000`).
    - **Loại giao dịch** (`chi` -> `expense`, `thu/nhận` -> `income`).
    - **Tiêu đề** (`ăn sáng`).
    - **Ví áp dụng** (`ví tiền mặt`).
- **Thời gian dự kiến**: Phase 8.

### 2.2. Trích xuất Hóa đơn điện tử tự động (E-Invoice Automated Parsing)
- **Mục tiêu**: Tự động đọc và ghi nhận giao dịch từ hóa đơn điện tử (PDF, Email, QR Code của MoMo, VNPAY).
- **Giải pháp**:
  - Tích hợp bộ quét mã QR Code tiêu chuẩn của Ngân hàng Nhà nước (VietQR) tại màn hình quét. Khi quét mã QR, tự động phân tích chuỗi ký tự EMVCo để lấy số tiền, tên cửa hàng và tài khoản đích.
  - Viết bộ đọc PDF Parser tại Backend để trích xuất hóa đơn điện tử nhận từ email người dùng gửi tới.
- **Thời gian dự kiến**: Phase 9.

### 2.3. Hỗ trợ Đa tiền tệ & Tự động Cập nhật Tỷ giá (Multi-Currency & Exchange Rate)
- **Mục tiêu**: Giúp người dùng quản lý các ví tiền ngoại tệ (USD, EUR, JPY) và tự động quy đổi về VND trong biểu đồ báo cáo tổng hợp.
- **Giải pháp**:
  - Tích hợp API tỷ giá hối đoái công cộng (như *ExchangeRate-API* hoặc *Fixer.io*) vào Spring Boot.
  - Cập nhật tỷ giá tự động vào lúc 0:00 AM hàng ngày và lưu trữ vào MySQL.
- **Thời gian dự kiến**: Phase 10.
