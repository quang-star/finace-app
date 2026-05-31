# Spend Category Classification Rules

Tài liệu này định nghĩa hệ thống các danh mục thu chi mặc định, danh sách các từ khóa đặc trưng (keywords) phục vụ bộ lọc Regex thô trên ứng dụng Android, và các quy tắc ánh xạ danh mục tự động của mô hình máy học Random Forest trên máy chủ.

---

## 1. Hệ thống Danh mục Mặc định (Default Category System)

Dự án cài đặt sẵn các danh mục thu chi mặc định của hệ thống (`is_default = TRUE`, `user_id = NULL`) để người dùng có thể sử dụng ngay sau khi đăng ký tài khoản mới:

### 1.1. Danh mục Chi tiêu (Expense Categories)

| Mã Danh mục | Tên Danh mục | Loại dòng tiền | Ý nghĩa sử dụng | Icon đề xuất | Mã màu HEX |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `1` | **Ăn uống** | `expense` | Bữa ăn, cà phê, siêu thị, thực phẩm | `ic_food` | `#FF5733` (Đỏ cam) |
| `2` | **Di chuyển** | `expense` | Xăng xe, sửa xe, Grab, taxi, xe buýt | `ic_transport` | `#3498DB` (Xanh dương) |
| `3` | **Mua sắm** | `expense` | Quần áo, giày dép, đồ điện tử | `ic_shopping` | `#9B59B6` (Tím) |
| `4` | **Hóa đơn** | `expense` | Điện, nước, internet, điện thoại | `ic_bill` | `#F1C40F` (Vàng) |
| `5` | **Giải trí** | `expense` | Xem phim, du lịch, sự kiện, trò chơi | `ic_entertainment`| `#E74C3C` (Đỏ) |
| `6` | **Sức khỏe** | `expense` | Thuốc men, khám bệnh, bảo hiểm | `ic_health` | `#2ECC71` (Xanh lá) |
| `7` | **Giáo dục** | `expense` | Học phí, sách vở, khóa học | `ic_education` | `#1ABC9C` (Màu ngọc) |
| `8` | **Nhà ở** | `expense` | Tiền thuê nhà, sửa nhà, nội thất | `ic_home` | `#E67E22` (Cam) |
| `9` | **Khác (Chi)** | `expense` | Các khoản chi tiêu nhỏ lẻ khác | `ic_other` | `#95A5A6` (Xám) |

### 1.2. Danh mục Thu nhập (Income Categories)

| Mã Danh mục | Tên Danh mục | Loại dòng tiền | Ý nghĩa sử dụng | Icon đề xuất | Mã màu HEX |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `10` | **Lương** | `income` | Lương hàng tháng, thưởng công việc | `ic_salary` | `#27AE60` (Xanh lá đậm)|
| `11` | **Kinh doanh** | `income` | Doanh thu bán hàng, dịch vụ tự do | `ic_business` | `#8E44AD` (Tím đậm) |
| `12` | **Đầu tư** | `income` | Lợi nhuận cổ phiếu, lãi tiết kiệm | `ic_investment` | `#D35400` (Cam đất) |
| `13` | **Khác (Thu)** | `income` | Tiền được tặng, các nguồn thu khác | `ic_other_income` | `#7F8C8D` (Xám đậm) |

---

## 2. Quy tắc lọc từ khóa nhanh trên Android (Regex Keyword Mapping)

Trước khi gửi dữ liệu thô lên Backend, ứng dụng Android có thể thực hiện kiểm tra từ khóa thô bằng Regex để phản hồi ngay lập tức cho người dùng (Fall-back logic khi mất kết nối mạng):

```
+-----------------------------------------------------------------------------+
| Tên Cửa Hàng / Nội Dung Giao Dịch                                          |
+--------------------------------------|--------------------------------------+
                                       | (Regex Match)
                                       v
                     +-----------------+-----------------+
                     | Khớp từ khóa?                     |
                     +--------+-----------------+--------+
                              | Có              | Không
                              v                 v
               +--------------+----+     +------+-----------------------------+
               | Gợi ý ngay        |     | Gửi API Backend                    |
               | danh mục tương ứng|     | Chạy Random Forest Classifier       |
               +-------------------+     +------------------------------------+
```

### Bảng tra cứu từ khóa thô (Keyword Lookup Table):

| Danh mục gợi ý | Mẫu biểu thức chính quy (Regex Pattern - Không phân biệt chữ hoa thường) | Ví dụ cụ thể |
| :--- | :--- | :--- |
| **Ăn uống** | `\b(com|bun|pho|lau|nuoc|tra|sua|cafe|highlands|starbucks|coopmart|bachhoaxanh|winmart|an sang|an toi|pizza|kfc)\b` | "Highlands Coffee", "Cơm tấm", "Bún bò" |
| **Di chuyển** | `\b(xang|dau|grab|gojek|taxi|mai linh|be|xe bus|ve xe|sua xe|thay nhot|gui xe|do xang)\b` | "Đổ xăng Petrolimex", "GrabBike", "Gửi xe" |
| **Hóa đơn** | `\b(dien|nuoc|internet|wifi|viettel|mobifone|vinaphone|cuoc|thue bao|netflix|spotify)\b` | "Tiền điện tháng 5", "Cước Internet" |
| **Mua sắm** | `\b(quan|ao|giay|dep|shopee|lazada|tiki|dien thoai|laptop|tai nghe|gia dung|sieu thi)\b` | "Mua quần jean Shopee", "Giày thể thao" |
| **Sức khỏe** | `\b(thuoc|pharmacity|an khang|benh vien|kham|nha khoa|bac si|vitamin)\b` | "Mua thuốc Pharmacity", "Khám răng" |

---

## 3. Quy tắc ánh xạ phân loại tự động của Random Forest (Backend)

Khi Backend nhận chuỗi văn bản thô từ OCR hóa đơn hoặc nhãn sản phẩm YOLO, bộ trích xuất đặc trưng `TextFeatureExtractor` sẽ véc-tơ hóa văn bản thô thành 100 chiều đặc trưng dựa trên tần suất xuất hiện của bộ từ khóa mở rộng sau:

```java
public class TextFeatureExtractor {
    // 100 từ khóa đặc trưng đại diện cho các danh mục chi tiêu tiếng Việt
    private static final String[] FEATURE_KEYWORDS = {
        "com", "pho", "bun", "mi", "lau", "nuoc", "sua", "tra", "cafe", "coffee", "highlands", "starbucks", " Highlands",
        "grab", "taxi", "gojek", "xang", "xe", "petrolimex", "gui", "ve", "bus", "bay", "tau", "lop", "nhot",
        "quan", "ao", "shopee", "lazada", "tiki", "giay", "dep", "shop", "store", "mall", "mua", "ban", "dieu",
        "dien", "nuoc", "internet", "wifi", "viettel", "mobi", "vina", "cuoc", "truyen", "hinh", "cab", "cap",
        "rap", "phim", "cgv", "lotte", "ve", "game", "steam", "play", "music", "hat", "karaoke", "du", "lich",
        "thuoc", "pharmacity", "benh", "vien", "kham", "rang", "nha", "khoa", "bac", "si", "apothecary", "health",
        "hoc", "phi", "sach", "vo", "khoa", "luyen", "thi", "tieng", "anh", "ielts", "toeic", "course", "school",
        "nha", "phong", "tro", "thue", "sua", "nội", "that", "giuong", "tu", "ban", "ghe", "son", "gach",
        "luong", "thuong", "salary", "bonus", "lai", "tiet", "kiem", "co", "tuc", "chuyen", "khoan", "tang"
    };
}
```

### Cơ chế gán nhãn trọng số đặc trưng (Feature Weighting):
- **Trọng số từ khóa (Term Presence)**: Sự xuất hiện của các từ khóa chỉ thị trực tiếp danh mục sẽ đóng vai trò quyết định (độ quan trọng cao nhất trong các Decision Trees của Random Forest).
- **Khoảng tiền giao dịch (Amount Value)**: Giao dịch có giá trị lớn thường được mô hình đưa về danh mục *Nhà ở* hoặc *Mua sắm* thay vì *Ăn uống* (ngay cả khi có xuất hiện từ khóa chung chung).
- **Lịch sử người dùng**: Bộ huấn luyện ưu tiên nạp các confirmed transactions của chính user đó để cá nhân hóa mô hình phân loại (Ví dụ: Cùng một từ khóa "tiền phòng" nhưng có user phân loại vào `Nhà ở`, có user phân loại vào `Giải trí`).
