# 書籍查詢 API 使用說明

## 端點
`GET /api/books`

## 查詢參數

### 篩選參數
- `keyword`: 搜尋關鍵字（書名或作者名）
- `mainCategoryId`: 主分類ID
- `subCategoryId`: 子分類ID
- `seriesDisplay`: 系列作顯示模式（1=僅代表作，0或null=全部）
- `authorId`: 作者ID
- `publisherId`: 出版社ID
- `tags`: 標籤ID列表（可多個）
- `seriesId`: 系列ID

### 分頁參數
- `page`: 頁碼，從 0 開始（預設：0）
- `size`: 每頁筆數（預設：20）

### 排序參數
使用 `sort` 參數，格式為 `欄位名,排序方向`

#### 可用的排序欄位

| 欄位名 | 說明 | 範例 |
|--------|------|------|
| `addedDate` | 上架日期 | `sort=addedDate,desc`（新到舊）<br>`sort=addedDate,asc`（舊到新） |
| `totalLoanCount` | 累計借閱次數 | `sort=totalLoanCount,desc`（熱門）<br>`sort=totalLoanCount,asc`（冷門） |
| `title` | 書名 | `sort=title,asc`（A-Z）<br>`sort=title,desc`（Z-A） |
| `publishYear` | 出版年份 | `sort=publishYear,desc`（新到舊）<br>`sort=publishYear,asc`（舊到新） |
| `averageRating` | 平均評分 | `sort=averageRating,desc`（高到低）<br>`sort=averageRating,asc`（低到高） |
| `ratingCount` | 評分人數 | `sort=ratingCount,desc`（多到少） |

## 範例

### 1. 基本查詢（使用預設排序：上架日期新到舊）
```
GET /api/books
```

### 2. 搜尋關鍵字，按熱門度排序
```
GET /api/books?keyword=哈利波特&sort=totalLoanCount,desc
```

### 3. 篩選分類，按上架日期舊到新排序
```
GET /api/books?mainCategoryId=1&sort=addedDate,asc
```

### 4. 第 2 頁，每頁 30 筆，按書名排序
```
GET /api/books?page=1&size=30&sort=title,asc
```

### 5. 複合排序（先按評分，再按借閱次數）
```
GET /api/books?sort=averageRating,desc&sort=totalLoanCount,desc
```

### 6. 特定作者，按出版年份降序
```
GET /api/books?authorId=5&sort=publishYear,desc
```

## 回應格式

```json
{
  "bookPage": {
    "content": [
      {
        "id": 1,
        "title": "書名",
        "imageUrl": "圖片URL",
        "authors": ["作者1", "作者2"],
        "publisherName": "出版社",
        "tags": ["標籤1", "標籤2"],
        "availableForLoan": true,
        "addedDate": "2024-01-01",
        "totalLoanCount": 50,
        "averageRating": 4.5,
        "ratingCount": 100
      }
    ],
    "currentPage": 0,
    "totalPages": 10,
    "totalElements": 200,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  },
  "stats": {
    "mainCategories": [...],
    "subCategories": [...],
    "authors": [...],
    "publishers": [...],
    "tags": [...]
  }
}
```

## 注意事項

1. **頁碼從 0 開始**：第一頁是 `page=0`，第二頁是 `page=1`
2. **預設排序**：如果不指定 `sort` 參數，預設按上架日期降序（新書優先）
3. **多重排序**：可以使用多個 `sort` 參數實現複合排序
4. **大小寫敏感**：欄位名稱必須完全匹配（使用 camelCase）

## 前端實現範例

### JavaScript/Axios
```javascript
// 獲取第一頁，按熱門度排序
axios.get('/api/books', {
  params: {
    page: 0,
    size: 20,
    sort: 'totalLoanCount,desc'
  }
})
.then(response => {
  const books = response.data.bookPage.content;
  const totalPages = response.data.bookPage.totalPages;
  // 處理資料...
});
```

### React 範例
```javascript
const [sortBy, setSortBy] = useState('addedDate');
const [sortDirection, setSortDirection] = useState('desc');

const fetchBooks = async (page = 0) => {
  const response = await fetch(
    `/api/books?page=${page}&size=20&sort=${sortBy},${sortDirection}`
  );
  const data = await response.json();
  return data;
};
```

## 常見排序場景

| 使用情境 | URL 參數 |
|---------|---------|
| 最新上架 | `sort=addedDate,desc` |
| 最早上架 | `sort=addedDate,asc` |
| 最熱門（借閱次數） | `sort=totalLoanCount,desc` |
| 最冷門 | `sort=totalLoanCount,asc` |
| 評分最高 | `sort=averageRating,desc` |
| 最多人評價 | `sort=ratingCount,desc` |
| 書名 A-Z | `sort=title,asc` |
| 最新出版 | `sort=publishYear,desc` |

