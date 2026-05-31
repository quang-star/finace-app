# Technical Notes on AI & Machine Learning

Tài liệu này tổng hợp các hướng dẫn kỹ thuật chuyên sâu về việc tích hợp và phát triển các mô hình trí tuệ nhân tạo (Edge AI trên Android & Server-Side Machine Learning trên Spring Boot) trong dự án.

---

## 1. Hướng dẫn kỹ thuật YOLOv8/v9 TFLite (Android)

Mô hình YOLOv8 hoặc YOLOv9 được huấn luyện để nhận dạng các vật thể tiêu dùng phổ biến và xuất ra định dạng TensorFlow Lite (.tflite) đã lượng tử hóa (Float16 hoặc INT8) để tối ưu dung lượng và tốc độ suy luận.

### 1.1. Cấu trúc mô hình đầu vào & đầu ra (Model I/O Specs)
- **Đầu vào (Input Tensor)**: 
  - Kích thước mặc định: `1 x 320 x 320 x 3` hoặc `1 x 640 x 640 x 3` (kiểu dữ liệu `FLOAT32` hoặc `UINT8`).
  - Định dạng màu: RGB. Cần thực hiện chuẩn hóa pixel về đoạn `[0.0, 1.0]` bằng cách chia giá trị RGB cho 255.0 trước khi đưa vào mô hình.
- **Đầu ra (Output Tensor)**:
  - Thường là một tensor có dạng `1 x (4 + num_classes) x num_boxes` (Ví dụ với 80 lớp và 8400 boxes: `1 x 84 x 8400`).
  - Cần thực hiện phép hoán vị (Transpose) tensor đầu ra để dễ dàng lặp qua từng box: `[box_x, box_y, box_w, box_h, class_1_score, ..., class_N_score]`.

### 1.2. Giải thuật hậu xử lý (Post-Processing - Non-Maximum Suppression)
Do YOLO sinh ra hàng nghìn khung nhận dạng chồng chéo, lớp `YoloDetector.java` bắt buộc phải triển khai thuật toán NMS:
1. Duyệt qua toàn bộ boxes, tìm kiếm lớp có điểm số cao nhất (`classScore`).
2. Lọc bỏ các boxes có `classScore < confidenceThreshold` (ngưỡng tối thiểu đề xuất: `0.5`).
3. Sắp xếp danh sách boxes theo điểm số tin cậy giảm dần.
4. Sử dụng phép tính chỉ số giao chồng **IoU (Intersection over Union)** để loại bỏ các boxes có mức độ giao nhau lớn hơn `iouThreshold` (ngưỡng tối thiểu đề xuất: `0.45`):
   $$\text{IoU} = \frac{\text{Diện tích giao nhau (Intersection)}}{\text{Diện tích hợp nhau (Union)}}$$

### 1.3. Vẽ bounding box khớp màn hình (Coordinate Mapping)
Vì tọa độ bounding box trả về từ mô hình tương ứng với ảnh đầu vào (Ví dụ: 320x320), trong khi kích thước màn hình hiển thị Preview Camera thực tế là lớn hơn (Ví dụ: 1080x2160), lớp `BoundingBoxOverlay.java` cần ánh xạ tọa độ (Scale & Translate) chính xác:
- Tọa độ X thực tế = Tọa độ X mô hình * (Chiều rộng màn hình / Chiều rộng ảnh mô hình).
- Tọa độ Y thực tế = Tọa độ Y mô hình * (Chiều cao màn hình / Chiều cao ảnh mô hình).

---

## 2. Hướng dẫn kỹ thuật Google ML Kit OCR (Android)

Thư viện ML Kit của Google cung cấp bộ nhận diện chữ viết mạnh mẽ, đáng tin cậy chạy hoàn toàn offline trên thiết bị.

### 2.1. Phân tích văn bản thô (Bill Parsing Regex Rules)
Regex là công cụ chính để lọc dữ liệu thực tế từ văn bản hóa đơn lộn xộn. Các quy tắc Regex cần được tối ưu cho định dạng hóa đơn tiếng Việt:
- **Trích xuất số tiền (Total Amount)**:
  - Mẫu Regex tìm kiếm số tiền: `(?i)(tổng cộng|thanh toán|tiền mặt|cần trả|tổng tiền|total|cash)\D*([\d.,]+)\s*(VND|đ|d)?`
  - Sau khi tìm thấy chuỗi số khớp, lớp `BillParser` cần chuẩn hóa chuỗi (loại bỏ dấu chấm phân tách phần nghìn, dấu phẩy phân tách thập phân) để parse về kiểu dữ liệu Double trong Java:
    - Ví dụ: `"150.000"` hoặc `"150,000"` -> `"150000"`.
- **Trích xuất ngày giao dịch (Date)**:
  - Mẫu Regex khớp ngày tháng phổ biến: `\b(\d{1,2})[-/.](\d{1,2})[-/.](\d{4})\b` (Ví dụ: `24/05/2026`) hoặc định dạng ngược `\b(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})\b`.
- **Trích xuất Tên cửa hàng (Merchant)**:
  - Đọc 3 khối văn bản đầu tiên của kết quả OCR. Loại bỏ các dòng chứa số điện thoại hoặc địa chỉ để lấy tên thương hiệu.

---

## 3. Huấn luyện Random Forest bằng thư viện Smile (Backend)

Thư viện **Smile** (Java) cung cấp thuật toán Random Forest tối ưu chạy trực tiếp trên JVM.

### 3.1. Trích xuất đặc trưng văn bản (Text Featurization)
Hệ thống sử dụng bộ véc-tơ hóa đơn giản (Bag of Words / TF-IDF):
- **Bước 1**: Chuyển toàn bộ văn bản thô thành chữ thường (Lower-case), loại bỏ dấu tiếng Việt (để tăng tính tổng quát) và các ký tự đặc biệt.
- **Bước 2**: Tách từ và lọc bỏ các từ dừng (Stop words - từ vô nghĩa như "và", "của", "tại", "là").
- **Bước 3**: Xây dựng bộ từ khóa đặc trưng (Vocabulary) gồm 100 từ khóa chỉ thị danh mục tài chính phổ biến.
- **Bước 4**: Tạo véc-tơ tần suất từ (Term Frequency - TF) đại diện cho văn bản:
  $$TF(t) = \frac{\text{Số lần từ } t \text{ xuất hiện trong văn bản}}{\text{Tổng số từ trong văn bản}}$$

### 3.2. Cấu hình mô hình Smile Random Forest
Khi khởi tạo và huấn luyện mô hình bằng Smile, thiết lập các tham số tối ưu sau để tránh Overfitting và tối ưu RAM:
```java
// Cấu hình tham số huấn luyện Random Forest
int nTrees = 100; // Số lượng cây quyết định trong rừng
int maxDepth = 15; // Độ sâu tối đa của mỗi cây
int mtry = (int) Math.sqrt(numFeatures); // Số lượng đặc trưng ngẫu nhiên được chọn tại mỗi điểm phân nhánh
int maxNodes = 1000; // Số lượng nút lá tối đa

// Huấn luyện mô hình
RandomForest model = RandomForest.fit(Formula.lhs("category"), dataset, nTrees, mtry, SplitRule.GINI, maxDepth, maxNodes);
```

### 3.3. Cơ chế nâng cấp mô hình liên tục (Online Retraining Loop)
- Mỗi khi người dùng phản hồi xác nhận danh mục (Feedback) hoặc sửa đổi danh mục gợi ý sai, một cặp dữ liệu mới bao gồm `(raw_text_features, confirmed_category_id)` sẽ được lưu vào cơ sở dữ liệu MySQL.
- Thiết lập một tác vụ tự động lập lịch (Spring Scheduled Task) chạy vào lúc 2:00 AM hàng ngày để:
  1. Truy vấn toàn bộ dữ liệu phản hồi xác nhận mới phát sinh trong ngày.
  2. Gom nhóm với tập dữ liệu huấn luyện cũ.
  3. Chạy hàm huấn luyện lại mô hình (`ModelTrainer.train()`).
  4. Lưu đè tệp tin mô hình đã tuần tự hóa `random_forest_model.ser` lên đĩa để hệ thống sử dụng ngay lập tức vào ngày hôm sau.
