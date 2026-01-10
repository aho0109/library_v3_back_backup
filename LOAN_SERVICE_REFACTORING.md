# LoanService 重構完成報告

## 📅 重構日期
2026-01-10

## 🎯 重構目標
將 `LoanServiceImpl` 的 `borrowBook()` 和 `returnBook()` 方法從**混合返回模式**重構為**統一異常處理模式**。

---

## ✅ 重構內容

### 1. **borrowBook() 方法重構**

#### 改進前（混合返回模式）
```java
// ❌ 失敗時返回 DTO
if (user.getStatus() == UserStatus.SUSPENDED) {
    return new BorrowRespDto(false, "使用者帳號已停權至 " + ..., null, null, null);
}

if (user.getStatus() != UserStatus.ACTIVE) {
    return new BorrowRespDto(false, "使用者帳號未啟用", null, null, null);
}

if (currentLoans >= limit) {
    return new BorrowRespDto(false, "借閱數量已達上限", null, null, null);
}

// ✅ 成功時返回 DTO
return new BorrowRespDto(true, "借閱成功", uniqueCode, savedLoan.getId(), title);
```

**問題**：
- ❌ 成功和失敗都用 `return`，語義不清
- ❌ Controller 需要判斷 `isSuccess()`
- ❌ 與其他 Service 方法（如 `renewBook()`）不一致

#### 改進後（統一異常處理）
```java
// ✅ 失敗時拋出異常
if (user.getStatus() == UserStatus.SUSPENDED) {
    throw new BusinessException(ErrorCode.USER_SUSPENDED, 
        "停權至: " + user.getSuspendedUntil());
}

if (user.getStatus() != UserStatus.ACTIVE) {
    throw new BusinessException(ErrorCode.USER_NOT_ACTIVATED);
}

if (currentLoans >= limit) {
    throw new BusinessException(ErrorCode.BORROW_LIMIT_EXCEEDED);
}

// ✅ 成功時只返回成功的 DTO
return new BorrowRespDto(true, "借閱成功", uniqueCode, savedLoan.getId(), title);
```

**優點**：
- ✅ 語義清晰：異常表示失敗，return 表示成功
- ✅ Controller 簡化：不需要判斷 `isSuccess()`
- ✅ 統一處理：與其他方法保持一致

---

### 2. **returnBook() 方法重構**

#### 改進前
```java
if (loanOpt.isEmpty()) {
    return new ReturnResponseDto(false, "此書無借出記錄", ...);
}
```

#### 改進後
```java
if (loanOpt.isEmpty()) {
    throw new BusinessException(ErrorCode.BOOK_NOT_BORROWED);
}
```

---

### 3. **Controller 層重構**

#### 改進前（需要判斷 success）
```java
@PostMapping("/borrow")
public ResponseEntity<ApiResponse<BorrowRespDto>> borrowBook(...) {
    BorrowRespDto response = loanService.borrowBook(...);
    if (response.isSuccess()) {
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    } else {
        return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
    }
}
```

#### 改進後（統一包裝）
```java
@PostMapping("/borrow")
public ResponseEntity<ApiResponse<BorrowRespDto>> borrowBook(...) {
    BorrowRespDto result = loanService.borrowBook(...);
    return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
}
```

**簡化**：
- ✅ 移除 `if-else` 判斷
- ✅ 失敗情況由 `GlobalExceptionHandler` 統一處理
- ✅ 程式碼減少 50%

---

## 📊 改進對比

| 項目 | 改進前 | 改進後 |
|------|--------|--------|
| **失敗處理** | 返回 DTO (success=false) | 拋出異常 |
| **成功處理** | 返回 DTO (success=true) | 返回 DTO (只有成功) |
| **Controller 邏輯** | 需判斷 isSuccess() | 直接包裝 ApiResponse |
| **語義清晰度** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **程式碼行數** | 較多 | 較少 |
| **一致性** | 與其他方法不一致 | 與整個專案一致 |
| **錯誤碼** | 無 | 有明確的錯誤碼 |

---

## 🎯 新增錯誤碼

為支援此次重構，確保以下錯誤碼已定義：

```java
// 使用者相關
USER_SUSPENDED("2005", "使用者帳號已停權"),
USER_NOT_ACTIVATED("2006", "帳號未啟用"),

// 借閱相關
BORROW_LIMIT_EXCEEDED("4002", "借閱數量已達上限"),
BOOK_ALREADY_BORROWED("4003", "此書已被借出"),
BOOK_NOT_BORROWED("4004", "此書無借出記錄"),

// 預約相關
BOOK_RESERVED_BY_OTHERS("5005", "此書已被其他使用者預約"),

// 圖書相關
BOOK_COPY_NOT_FOUND("3003", "圖書副本不存在"),
BOOK_COPY_UNAVAILABLE("3004", "圖書副本目前無法借閱"),
```

---

## 🔄 API 回應格式變化

### 借閱成功
```json
// 改進前
{
  "success": true,
  "message": "借閱成功",
  "borrowedBookUniqueCode": "BOOK001",
  "loanId": 123,
  "title": "哈利波特"
}

// 改進後
{
  "success": true,
  "message": "借閱成功",
  "data": {
    "success": true,
    "message": "借閱成功",
    "borrowedBookUniqueCode": "BOOK001",
    "loanId": 123,
    "title": "哈利波特"
  },
  "timestamp": "2026-01-10T15:30:00"
}
```

### 借閱失敗（停權）
```json
// 改進前
{
  "success": false,
  "message": "使用者帳號已停權至 2026-02-10T15:30:00",
  "borrowedBookUniqueCode": null,
  "loanId": null,
  "title": null
}

// 改進後
{
  "success": false,
  "message": "使用者帳號已停權: 停權至: 2026-02-10T15:30:00",
  "errorCode": "2005",
  "data": null,
  "timestamp": "2026-01-10T15:30:00"
}
```

### 借閱失敗（額度已滿）
```json
// 改進後
{
  "success": false,
  "message": "借閱數量已達上限",
  "errorCode": "4002",
  "data": null,
  "timestamp": "2026-01-10T15:30:00"
}
```

---

## ✅ 重構驗證

### Service 層
- ✅ 所有失敗情況拋出明確的業務異常
- ✅ 成功時只返回成功的 DTO
- ✅ 與 `renewBook()` 方法保持一致

### Controller 層
- ✅ 移除 `if-else` 判斷邏輯
- ✅ 統一使用 `ApiResponse<T>` 包裝
- ✅ 簡化為單一返回路徑

### 異常處理
- ✅ 所有異常由 `GlobalExceptionHandler` 統一處理
- ✅ 每個異常都有對應的錯誤碼
- ✅ 錯誤訊息清晰明確

---

## 🎨 設計模式應用

### 1. **異常驅動的控制流**
```java
// Service 層
public BorrowRespDto borrowBook(...) {
    validateUser(user);        // 失敗拋異常
    validateBookCopy(copy);    // 失敗拋異常
    createLoan(...);           // 執行業務邏輯
    return successDto;         // 返回成功結果
}
```

### 2. **單一職責原則**
- **Service**: 只負責業務邏輯和拋出異常
- **Controller**: 只負責 HTTP 包裝
- **GlobalExceptionHandler**: 只負責異常轉換

### 3. **統一回應格式**
```java
ApiResponse<T>
  ↓
成功: { success: true, data: T }
失敗: { success: false, errorCode: "xxx", message: "..." }
```

---

## 📝 前端影響

### 建議前端調整

**改進前**：
```javascript
const response = await api.post('/api/loans/borrow', data);
if (response.data.success) {
  // 處理成功
  console.log(response.data.borrowedBookUniqueCode);
} else {
  // 處理失敗
  alert(response.data.message);
}
```

**改進後**（使用攔截器）：
```javascript
try {
  const result = await api.post('/api/loans/borrow', data);
  // 攔截器自動解包 data.data
  console.log(result.borrowedBookUniqueCode);
  ElMessage.success('借閱成功');
} catch (error) {
  // 攔截器已處理錯誤顯示
  console.error(error.errorCode, error.message);
}
```

---

## 🚀 後續優化建議

### 1. **簡化 DTO 結構**（可選）
```java
// 當前
public class BorrowRespDto {
    private boolean success;  // ← 可以移除（已由 ApiResponse 處理）
    private String message;   // ← 可以移除（已由 ApiResponse 處理）
    private String borrowedBookUniqueCode;
    private Long loanId;
    private String title;
}

// 建議未來重構
public class BorrowResult {
    private String borrowedBookUniqueCode;
    private Long loanId;
    private String title;
}
```

### 2. **統一命名**
- `BorrowRespDto` → `BorrowResult`
- `ReturnResponseDto` → `ReturnResult`

### 3. **繼續重構其他類似方法**
檢查專案中是否還有其他使用混合返回模式的方法。

---

## ✅ 總結

### 重構成果
- ✅ **統一異常處理**：失敗拋異常，成功返回資料
- ✅ **簡化 Controller**：移除 if-else 判斷
- ✅ **明確錯誤碼**：每個失敗情況都有對應錯誤碼
- ✅ **提高一致性**：與整個專案異常處理體系一致

### 符合業界最佳實踐
- ✅ Service 層只關注業務邏輯
- ✅ Controller 層只關注 HTTP 包裝
- ✅ GlobalExceptionHandler 統一異常處理
- ✅ 清晰的分層架構

**重構完成！** 🎉

現在 `LoanServiceImpl` 的異常處理已與整個專案保持完全一致。

