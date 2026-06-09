# AI Pipelines

## Tổng quan

Hệ thống dùng hai pipeline độc lập:

- OCR hóa đơn: ML Kit chạy trên Android, Gemini phân tích văn bản tại backend.
- Quét sản phẩm: YOLO/TFLite và Random Forest đều chạy local trên Android.

## OCR hóa đơn

```mermaid
graph LR
    A["Camera hoặc thư viện"] --> B["ML Kit OCR trên Android"]
    B --> C["Raw OCR text"]
    C --> D["POST /api/ai-scan/classify"]
    D --> E["Gemini: merchant, amount, date, category"]
    E --> F["Lưu ai_scan_logs"]
    F --> G["Mở form giao dịch"]
    G --> H["POST /api/ai-scan/feedback khi lưu"]
```

Backend lưu log OCR để liên kết giao dịch và ghi nhận danh mục người dùng xác nhận. Bản demo không cung cấp endpoint quản trị để tái huấn luyện model.

## Quét sản phẩm

```mermaid
graph LR
    A["Camera hoặc thư viện"] --> B["YOLO/TFLite trên Android"]
    B --> C["Danh sách detection"]
    C --> D["Trích xuất vector đặc trưng"]
    D --> E["Random Forest local"]
    E --> F["Gợi ý categoryId"]
    F --> G["Mở form giao dịch"]
```

Pipeline sản phẩm không gọi backend AI và không lưu bảng log riêng. Model Random Forest được huấn luyện bằng các script trong `tools/`, sau đó export thành `ProductRandomForestModel.java` cho Android.
