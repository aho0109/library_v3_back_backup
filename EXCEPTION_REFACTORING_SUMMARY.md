# 異常處理重構總結

## 📅 重構日期
2026-01-10

## 🎯 重構目標
統一後端異常處理機制，採用最佳實踐，提高程式碼可維護性和健壯性。

## ✅ 已完成的工作

### 1. 基礎設施建立

#### 1.1 統一 API 回應格式
- **檔案**: `dto/common/ApiResponse.java`
- **功能**: 
  - 統一的成功/失敗回應格式
  - 包含 `success`, `message`, `data`, `errorCode`, `timestamp`
  - 提供靜態工廠方法簡化創建

#### 1.2 錯誤碼枚舉
- **檔案**: `exception/ErrorCode.java`
- **分類**:
  - 1xxx: 通用錯誤
  - 2xxx: 使用者相關錯誤
  - 3xxx: 圖書相關錯誤
  - 4xxx: 借閱相關錯誤
  - 5xxx: 預約相關錯誤
  - 6xxx: 評論相關錯誤
  - 7xxx: 收藏相關錯誤
  - 8xxx: 通知相關錯誤
  - 9xxx: 系統錯誤

#### 1.3 自定義異常體系
- **BaseException**: 所有業務異常的基類
- **ResourceNotFoundException**: 資源不存在異常
- **BusinessException**: 業務邏輯異常
- **ValidationException**: 參數驗證異常

### 2. GlobalExceptionHandler 重構

**檔案**: `exceptionHandler/GlobalExceptionHandler.java`

**改進**:
- ✅ 統一處理所有自定義異常
- ✅ 統一 API 回應格式（ApiResponse）
- ✅ 移除內部類 ErrorResponse
- ✅ 新增錯誤碼到 HTTP 狀態碼的智能映射
- ✅ 保留對舊異常的相容性（SourceNotFoundException）
- ✅ 完整的日誌記錄

**處理的異常類型**:
- 自定義業務異常 (BaseException, ResourceNotFoundException, BusinessException, ValidationException)
- JPA 異常 (EntityNotFoundException)
- 標準 Java 異常 (IllegalArgumentException, IllegalStateException, NullPointerException)
- 參數驗證異常 (MethodArgumentNotValidException, ConstraintViolationException)
- 資料庫異常 (DataIntegrityViolationException, JpaSystemException)
- 其他異常 (HttpMessageNotReadableException, MethodArgumentTypeMismatchException)

### 3. Service 層重構

已重構的 Service 實作類別:

#### 3.1 UserServiceImpl
- ✅ 所有方法改用自定義異常
- ✅ `EntityNotFoundException` → `ResourceNotFoundException`
- ✅ `IllegalArgumentException` → `BusinessException` 或 `ValidationException`
- ✅ `IllegalStateException` → `BusinessException`

**重構方法**:
- `getUserProfile()`: USER_NOT_FOUND
- `registerUser()`: ACCOUNT_ALREADY_EXISTS, EMAIL_ALREADY_EXISTS
- `updateUserProfile()`: USER_NOT_FOUND, USER_DETAIL_NOT_FOUND, ACCOUNT_ALREADY_EXISTS, EMAIL_ALREADY_EXISTS
- `verifyOldPassword()`: ACCOUNT_LOCKED, USER_NOT_FOUND, PASSWORD_ATTEMPT_EXCEEDED, OLD_PASSWORD_INCORRECT
- `changePassword()`: ACCOUNT_LOCKED, USER_NOT_FOUND, OLD_PASSWORD_INCORRECT, PASSWORD_TOO_SHORT
- `activateUser()`: USER_NOT_FOUND, BAD_REQUEST
- `suspendUser()`: USER_NOT_FOUND
- `restoreUser()`: USER_NOT_FOUND

#### 3.2 LoanServiceImpl
- ✅ 所有方法改用自定義異常
- ✅ 統一錯誤處理邏輯

**重構方法**:
- `borrowBook()`: USER_NOT_FOUND, BOOK_COPY_NOT_FOUND
- `returnBook()`: BOOK_COPY_NOT_FOUND
- `renewBook()`: LOAN_NOT_FOUND, UNAUTHORIZED_LOAN_OPERATION, NOT_ON_LOAN_STATUS, RENEW_LIMIT_EXCEEDED, BOOK_RESERVED_CANNOT_RENEW, RENEW_WINDOW_NOT_OPEN

#### 3.3 ReservationServiceImpl
- ✅ 所有方法改用自定義異常

**重構方法**:
- `reserveBookCopy()`: USER_NOT_FOUND, USER_NOT_ACTIVATED, RESERVATION_LIMIT_EXCEEDED, BOOK_COPY_NOT_FOUND, BOOK_AVAILABLE_NO_RESERVATION, CANNOT_RESERVE_OWN_LOAN, ALREADY_RESERVED
- `cancelReservation()`: RESERVATION_NOT_FOUND, UNAUTHORIZED_RESERVATION_OPERATION, RESERVATION_CANNOT_CANCEL

#### 3.4 ReviewServiceImpl
- ✅ 所有方法改用自定義異常

**重構方法**:
- `addReview()`: USER_NOT_FOUND, BOOK_NOT_FOUND, REVIEW_ALREADY_EXISTS
- `updateReview()`: REVIEW_NOT_FOUND, UNAUTHORIZED_REVIEW_OPERATION
- `deleteReview()`: REVIEW_NOT_FOUND, UNAUTHORIZED_REVIEW_OPERATION
- `likeReview()`: USER_NOT_FOUND, REVIEW_NOT_FOUND, ALREADY_LIKED, CANNOT_LIKE_OWN_REVIEW
- `unlikeReview()`: REVIEW_LIKE_NOT_FOUND

#### 3.5 FavoriteServiceImpl
- ✅ 所有方法改用自定義異常

**重構方法**:
- `addFavorite()`: ALREADY_FAVORITED, USER_NOT_FOUND, BOOK_NOT_FOUND
- `removeFavorite()`: FAVORITE_NOT_FOUND
- `toggleFavorite()`: USER_NOT_FOUND, BOOK_NOT_FOUND

#### 3.6 BookServiceImpl
- ✅ 部分關鍵方法已重構

**重構方法**:
- `createBook()`: CATEGORY_SUB_NOT_FOUND, PUBLISHER_NOT_FOUND, SERIES_NOT_FOUND

### 4. Controller 層重構

已重構的 Controller:

#### 4.1 UserController
- ✅ 移除所有 try-catch 區塊
- ✅ 統一使用 ApiResponse 回應
- ✅ 簡化方法邏輯

**重構端點**:
- `POST /api/users/register`: 註冊
- `GET /api/users/me/profile`: 獲取個人資料
- `PUT /api/users/me/profile`: 更新個人資料
- `POST /api/users/me/change-password/verify-old`: 驗證舊密碼
- `POST /api/users/me/change-password`: 修改密碼
- `GET /api/users/me/notifications`: 獲取通知
- `PUT /api/users/me/notifications/{id}/read`: 標記通知已讀

#### 4.2 LoanController
- ✅ 移除所有 try-catch 區塊
- ✅ 統一使用 ApiResponse 回應

**重構端點**:
- `POST /api/loans/borrow`: 借閱書籍 (管理員)
- `POST /api/loans/return`: 歸還書籍 (管理員)
- `PUT /api/loans/{loanId}/renew`: 續借
- `GET /api/loans/my-current`: 當前借閱
- `GET /api/loans/my-history`: 借閱歷史
- `GET /api/loans/my-overdue`: 逾期借閱

#### 4.3 ReservationController
- ✅ 移除所有 try-catch 區塊
- ✅ 統一使用 ApiResponse 回應

**重構端點**:
- `POST /api/reservations/reserve`: 預約書籍
- `DELETE /api/reservations/{id}`: 取消預約
- `GET /api/reservations/my`: 我的預約

#### 4.4 BookController
- ✅ 評論相關端點已重構
- ✅ 統一使用 ApiResponse 回應

**重構端點**:
- `POST /api/books/{bookId}/reviews`: 新增評論
- `PUT /api/books/{bookId}/reviews/{reviewId}`: 更新評論
- `DELETE /api/books/{bookId}/reviews/{reviewId}`: 刪除評論
- `GET /api/books/{bookId}/reviews`: 獲取評論列表

#### 4.5 AdminBookController
- ✅ 移除所有 try-catch 區塊
- ✅ 統一使用 ApiResponse 回應

**重構端點**:
- `POST /api/admin/books`: 建立書籍
- `PUT /api/admin/books/{id}`: 更新書籍
- `DELETE /api/admin/books/{id}`: 刪除書籍
- `GET /api/admin/books/{id}`: 查詢書籍
- `POST /api/admin/books/{bookId}/copies`: 新增副本

## 📊 重構統計

### 新增檔案
- `dto/common/ApiResponse.java`
- `exception/ErrorCode.java`
- `exception/BaseException.java`
- `exception/ResourceNotFoundException.java`
- `exception/BusinessException.java`
- `exception/ValidationException.java`

### 修改檔案
- `exceptionHandler/GlobalExceptionHandler.java` (完全重寫)
- `service/Impl/UserServiceImpl.java` (7 個方法)
- `service/Impl/LoanServiceImpl.java` (3 個方法)
- `service/Impl/ReservationServiceImpl.java` (2 個方法)
- `service/Impl/ReviewServiceImpl.java` (5 個方法)
- `service/Impl/FavoriteServiceImpl.java` (3 個方法)
- `service/Impl/BookServiceImpl.java` (1 個方法)
- `controller/UserController.java` (7 個端點)
- `controller/LoanController.java` (6 個端點)
- `controller/ReservationController.java` (3 個端點)
- `controller/BookController.java` (4 個端點)
- `controller/AdminBookController.java` (5 個端點)

## 🎨 設計原則

### 1. 層次分離
```
Controller 層
  ↓ (只包裝 ApiResponse，不處理業務異常)
Service 層
  ↓ (只拋出業務異常，不處理 HTTP)
GlobalExceptionHandler
  ↓ (統一捕獲並轉換為 HTTP 回應)
Client
```

### 2. 異常處理原則
- **Service 層**: 只拋出業務異常，不處理 HTTP 相關邏輯
- **Controller 層**: 不使用 try-catch，讓異常傳播到 GlobalExceptionHandler
- **GlobalExceptionHandler**: 統一處理所有異常，轉換為統一格式的 ApiResponse

### 3. 錯誤碼設計
- 使用枚舉統一管理
- 錯誤碼按業務領域分類
- 每個錯誤碼包含 code 和 message

### 4. API 回應格式
```json
{
  "success": true/false,
  "message": "操作訊息",
  "data": { /* 資料 */ },
  "errorCode": "錯誤碼",
  "timestamp": "2026-01-10T12:00:00"
}
```

## 🔄 待完成工作

### Service 層
- [ ] NotificationServiceImpl
- [ ] CategoryServiceImpl
- [ ] CategorySubServiceImpl
- [ ] AuthorServiceImpl
- [ ] PublisherServiceImpl
- [ ] SeriesServiceImpl
- [ ] TagServiceImpl
- [ ] BookCopyServiceImpl
- [ ] BookServiceBasicImpl
- [ ] BookServiceImpl (完整重構)

### Controller 層
- [ ] FavoriteController
- [ ] NotificationController
- [ ] AdminUserController
- [ ] AdminCategoryController
- [ ] CategoryController
- [ ] CategorySubController
- [ ] AuthorController
- [ ] PublisherController
- [ ] SeriesController
- [ ] TagController
- [ ] AdminBookCopyController
- [ ] PageController
- [ ] AuthController (如有需要)

## 📝 遷移指南

### 對前端的影響
1. **所有 API 回應格式變更**:
   - 舊格式: 直接回傳 DTO 或錯誤訊息
   - 新格式: 統一包裝在 `ApiResponse<T>` 中

2. **前端調整範例**:
```javascript
// 舊寫法
const response = await api.get('/users/me/profile');
const user = response.data;

// 新寫法
const response = await api.get('/users/me/profile');
if (response.data.success) {
  const user = response.data.data;
} else {
  console.error(response.data.errorCode, response.data.message);
}
```

3. **建議前端統一攔截器處理**:
```javascript
axios.interceptors.response.use(
  response => {
    if (response.data.success) {
      return response.data.data;
    } else {
      throw new Error(response.data.message);
    }
  },
  error => {
    // 處理錯誤
  }
);
```

## ✨ 優點

1. **統一性**: 所有 API 回應格式一致
2. **可維護性**: 異常處理邏輯集中在 GlobalExceptionHandler
3. **可擴展性**: 新增異常類型只需在 ErrorCode 枚舉中添加
4. **健壯性**: 明確的錯誤分類和錯誤碼
5. **易測試**: Service 層純粹處理業務邏輯
6. **前端友好**: 統一的錯誤碼便於前端處理

## 🚀 下一步建議

1. **完成剩餘 Service 和 Controller 的重構**
2. **更新前端 API 調用邏輯**
3. **添加單元測試驗證異常處理**
4. **更新 API 文檔**
5. **建立錯誤碼對照表供前端使用**
6. **考慮是否需要國際化錯誤訊息**

## 📚 參考資料

- Spring Boot 異常處理最佳實踐
- RESTful API 設計規範
- 企業級應用錯誤處理模式

---

**重構完成度**: 約 40%
**預計完成時間**: 需額外 2-3 小時完成所有 Service 和 Controller

