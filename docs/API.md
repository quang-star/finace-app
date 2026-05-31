# Spring Boot REST API Specification

Tài liệu này định nghĩa chi tiết các REST API Endpoints được cung cấp bởi Spring Boot Backend (`finance-backend`). Mọi endpoint ngoại trừ cổng xác thực đăng nhập đều yêu cầu đính kèm Firebase ID Token ở header `Authorization: Bearer <token>`.

---

## 1. Xác thực (Authentication)
Quản lý đăng nhập và đồng bộ người dùng.

### 1.1. Đăng nhập qua Firebase Token
- **Endpoint**: `POST /api/auth/firebase-login`
- **Xác thực**: Không yêu cầu Authorization header.
- **Request Body**:
```json
{
  "email": "user@example.com",
  "fullName": "Nguyen Van A"
}
```
*Lưu ý*: Token Firebase ID được đính kèm ở Header `Authorization: Bearer <Firebase_ID_Token>` để Backend giải mã và lấy `firebase_uid`.
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "userId": 1,
    "firebaseUid": "F9a8d7s6f5g4h3j2k1",
    "fullName": "Nguyen Van A",
    "email": "user@example.com",
    "avatarUrl": "https://example.com/avatar.jpg"
  }
}
```

---

## 2. Quản lý Ví / Tài khoản (Accounts)

### 2.1. Lấy danh sách ví của User
- **Endpoint**: `GET /api/accounts`
- **Response (200 OK)**:
```json
[
  {
    "accountId": 1,
    "accountName": "Ví tiền mặt",
    "accountType": "cash",
    "balance": 1500000.00,
    "currency": "VND"
  },
  {
    "accountId": 2,
    "accountName": "ATM Vietcombank",
    "accountType": "bank",
    "balance": 12450000.00,
    "currency": "VND"
  }
]
```

### 2.2. Tạo ví mới
- **Endpoint**: `POST /api/accounts`
- **Request Body**:
```json
{
  "accountName": "Ví Momo",
  "accountType": "cash",
  "balance": 500000.00,
  "currency": "VND"
}
```
- **Response (201 Created)**:
```json
{
  "accountId": 3,
  "accountName": "Ví Momo",
  "accountType": "cash",
  "balance": 500000.00,
  "currency": "VND"
}
```

### 2.3. Cập nhật ví hoặc Xóa ví
- **Endpoints**:
  - `PUT /api/accounts/{id}`
  - `DELETE /api/accounts/{id}`

---

## 3. Danh mục Thu/Chi (Categories)

### 3.1. Lấy danh sách danh mục (Bao gồm mặc định và tự tạo)
- **Endpoint**: `GET /api/categories`
- **Response (200 OK)**:
```json
[
  {
    "categoryId": 1,
    "categoryName": "Ăn uống",
    "categoryType": "expense",
    "icon": "ic_food",
    "color": "#FF5733",
    "isDefault": true
  },
  {
    "categoryId": 12,
    "categoryName": "Lương",
    "categoryType": "income",
    "icon": "ic_salary",
    "color": "#2ECC71",
    "isDefault": true
  }
]
```

### 3.2. Tạo danh mục mới
- **Endpoint**: `POST /api/categories`
- **Request Body**:
```json
{
  "categoryName": "Nuôi mèo",
  "categoryType": "expense",
  "icon": "ic_cat",
  "color": "#9B59B6"
}
```

---

## 4. Giao dịch tài chính (Transactions)

### 4.1. Lấy danh sách giao dịch có bộ lọc thời gian
- **Endpoint**: `GET /api/transactions?startDate=2026-05-01&endDate=2026-05-31`
- **Response (200 OK)**:
```json
[
  {
    "transactionId": 101,
    "accountId": 1,
    "accountName": "Ví tiền mặt",
    "categoryId": 1,
    "categoryName": "Ăn uống",
    "title": "Cơm tấm sườn bì chả",
    "amount": 45000.00,
    "transactionType": "expense",
    "transactionDate": "2026-05-24",
    "note": "Ăn trưa cùng đồng nghiệp",
    "status": "confirmed"
  }
]
```

### 4.2. Thêm giao dịch mới (Thu/Chi)
- **Endpoint**: `POST /api/transactions`
- **Request Body**:
```json
{
  "categoryId": 1,
  "title": "Mua cà phê sữa",
  "amount": 25000.00,
  "transactionType": "expense",
  "transactionDate": "2026-05-24",
  "note": "Cà phê sáng"
}
```
*Lưu ý an toàn (Safe Logic)*: Android Client **không gửi kèm `accountId`** trong request body. Backend sẽ tự động xác thực Firebase Token, gọi hàm `getOrCreateDefaultAccount(userId)` để xác định Ví chính của người dùng, tự động gán `account_id` trên server, thực hiện cộng/trừ số dư ví mặc định đó, và lưu giao dịch.
- **Response (201 Created)**:
```json
{
  "transactionId": 102,
  "accountId": 1,
  "title": "Mua cà phê sữa",
  "amount": 25000.00,
  "transactionType": "expense",
  "transactionDate": "2026-05-24",
  "status": "confirmed"
}
```

### 4.3. Chuyển tiền giữa các ví (Transfers)
- **Endpoint**: `POST /api/transfers`
- **Trạng thái**: Tạm thời ẩn trên giao diện Android UI. **Tuyệt đối không xóa** code API này ở phía Backend.
- **Request Body**:
```json
{
  "fromAccountId": 2,
  "toAccountId": 1,
  "amount": 200000.00,
  "transferDate": "2026-05-24",
  "note": "Rút tiền ATM tiêu vặt"
}
```
*Lưu ý*: API này vẫn tồn tại trên server để đảm bảo tính sẵn sàng, nhưng không được gọi từ ứng dụng Android do hệ thống đang chạy ở chế độ 1 ví duy nhất.
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Chuyển tiền thành công",
  "transferGroupId": 5
}
```

---

## 5. Thiết lập Ngân sách (Budgets)

### 5.1. Tạo ngân sách mới
- **Endpoint**: `POST /api/budgets`
- **Request Body**:
```json
{
  "categoryId": 1,
  "budgetName": "Ăn uống Tháng 5",
  "amountLimit": 3000000.00,
  "startDate": "2026-05-01",
  "endDate": "2026-05-31"
}
```
- **Response (201 Created)**:
```json
{
  "budgetId": 8,
  "categoryId": 1,
  "budgetName": "Ăn uống Tháng 5",
  "amountLimit": 3000000.00,
  "spentAmount": 1250000.00,
  "startDate": "2026-05-01",
  "endDate": "2026-05-31"
}
```

---

## 6. Thống kê & Báo cáo (Reports)

### 6.1. Lấy dữ liệu báo cáo chi tiêu theo Danh mục
- **Endpoint**: `GET /api/reports/by-category?startDate=2026-05-01&endDate=2026-05-31`
- **Response (200 OK)**:
```json
[
  {
    "categoryId": 1,
    "categoryName": "Ăn uống",
    "color": "#FF5733",
    "totalAmount": 1250000.00,
    "percentage": 58.14
  },
  {
    "categoryId": 3,
    "categoryName": "Di chuyển",
    "color": "#3498DB",
    "totalAmount": 900000.00,
    "percentage": 41.86
  }
]
```

---

## 7. Trí tuệ nhân tạo (AI Machine Learning)
*Trạng thái: Tạm thời ẩn trên giao diện Android UI. Tuyệt đối không xóa bất kỳ lớp AI nào ở Backend hay Android Client.*

### 7.1. Phân loại hóa đơn chi tiêu (OCR classification)
- **Endpoint**: `POST /api/ai-scan/classify`
- **Request Body**:
```json
{
  "rawOcrText": "SIÊU THỊ COOPMART\nHOA DON BAN LE\nSUA TUOI: 45,000\nBANH MI: 20,000\nTONG CONG: 65,000\nNGAY: 24/05/2026",
  "detectedMerchant": "Coopmart",
  "detectedAmount": 65000.00,
  "detectedDate": "2026-05-24"
}
```
- **Response (200 OK)**:
```json
{
  "suggestedCategoryId": 1,
  "suggestedCategoryName": "Ăn uống",
  "confidenceScore": 0.9421
}
```

### 7.2. Gửi phản hồi xác nhận/sửa lỗi danh mục (Feedback Loop)
Giúp cập nhật bộ dữ liệu huấn luyện để tái đào tạo mô hình Random Forest.
- **Endpoint**: `POST /api/ai-scan/feedback`
- **Request Body**:
```json
{
  "rawOcrText": "SIÊU THỊ COOPMART\n...",
  "detectedMerchant": "Coopmart",
  "detectedAmount": 65000.00,
  "userConfirmedCategoryId": 1,
  "confidenceScore": 0.9421,
  "wasCorrected": false
}
```
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Đã ghi nhận phản hồi để nâng cấp AI"
}
```

### 7.3. Phân loại sản phẩm YOLO (YOLO Object category mapping)
Gợi ý danh mục dựa trên nhãn nhận dạng của YOLO và giá tiền người dùng nhập.
- **Endpoint**: `POST /api/ai-product/classify`
- **Request Body**:
```json
{
  "detectedProduct": "coffee",
  "confidenceScore": 0.8924,
  "userEnteredPrice": 35000.00
}
```
- **Response (200 OK)**:
```json
{
  "suggestedCategoryId": 1,
  "suggestedCategoryName": "Ăn uống",
  "confidenceScore": 0.9812
}
```
