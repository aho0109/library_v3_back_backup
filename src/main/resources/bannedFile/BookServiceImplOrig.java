//package com.matsuzaka.library_normal.service.Impl;
//
//import com.matsuzaka.library_normal.dto.queryDTO.BookListItemDTO;
//import com.matsuzaka.library_normal.dto.queryDTO.BookSearchResponseDTO;
//import com.matsuzaka.library_normal.dto.queryDTO.FacetedSearchStatsDTO;
//import com.matsuzaka.library_normal.dto.queryDTO.PageResponseDTO;
//import com.matsuzaka.library_normal.dto.queryDTO.StatItemDTO;
//import com.matsuzaka.library_normal.model.entity.*;
//import com.matsuzaka.library_normal.model.repositoryDao.BookCopyRepository;
//import com.matsuzaka.library_normal.model.repositoryDao.BookRepository;
//import com.matsuzaka.library_normal.service.BookService;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.criteria.*;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.data.jpa.repository.EntityGraph;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * 職責說明:
// * 1. 作為應用程式的業務邏輯核心，負責處理書籍相關的複雜操作
// * 2. 實現多條件篩選搜尋功能，支援分頁與排序
// * 3. 提供分面搜尋(Faceted Search)統計功能，優化使用者體驗
// * 4. 將資料庫實體(Entity)轉換為傳輸物件(DTO)，確保層間解耦
// *
// * 設計原則:
// * - 單一職責原則(SRP): 專注於書籍相關業務邏輯
// * - 依賴注入(DI): 通過建構函數注入依賴，便於測試
// * - 關注點分離(SoC): 業務邏輯、資料存取、展示邏輯明確分離
// * - 最小知識原則(LoD): 只依賴必要的外部組件
// */
//@Service
//@Transactional(readOnly = true) // 預設為只讀交易，提高效能並減少資料庫鎖定
//public class BookServiceImpl implements BookService {
//
//    // 注入必要的存儲庫與工具
//    private final BookRepository bookRepository;       // 書籍資料庫操作
//    private final BookCopyRepository bookCopyRepository; // 書籍副本資料庫操作
//    private final EntityManager entityManager;         // JPA EntityManager，用於原生查詢
//    // 管理entity生命週期及對持久層進行CRUD操作。
//
//    public BookServiceImpl(BookRepository bookRepository, BookCopyRepository bookCopyRepository, EntityManager entityManager) {
//        this.bookRepository = bookRepository;
//        this.bookCopyRepository = bookCopyRepository;
//        this.entityManager = entityManager;
//    }
//
//    /**
//     * 主要的搜尋方法：結合書籍搜尋、篩選與統計功能。
//     *
//     * 功能說明:
//     * 1. 接收多種篩選條件，構建動態查詢
//     * 2. 執行分頁查詢，獲取符合條件的書籍列表
//     * 3. 批量查詢書籍的可借閱狀態，避免N+1查詢問題
//     * 4. 同時獲取分面統計數據，用於前端顯示篩選面板
//     * 5. 將結果封裝為 DTO 格式
//     *
//     * 效能優化:
//     * - 使用 Specification 構建動態查詢，避免硬編碼
//     * - 批量獲取書籍可借閱狀態，減少資料庫訪問次數
//     * - 通過 JOIN 優化關聯查詢，減少資料庫負荷
//     *
//     * @param keyword        搜尋關鍵字，用於匹配書名或作者名
//     * @param mainCategoryId 主分類ID，過濾特定分類的書籍
//     * @param subCategoryId  子分類ID，進一步細化分類過濾
//     * @param seriesDisplay  系列顯示模式，1表示僅顯示代表作
//     * @param authorId       作者ID，過濾特定作者的書籍
//     * @param publisherId    出版社ID，過濾特定出版社的書籍
//     * @param tagIds         標籤ID列表，支持多標籤過濾
//     * @param pageable       分頁和排序參數，包含頁碼、每頁大小和排序條件
//     * @return 包含書籍列表和分��統計的複合DTO
//     */
//    @Override
//    public BookSearchResponseDTO searchAndFilterAndGetStats(String keyword, Long mainCategoryId, Long subCategoryId, Integer seriesDisplay,
//                                                            Long authorId, Long publisherId, List<Long> tagIds, Pageable pageable) {
//
//        // 創建返回DTO對象
//        BookSearchResponseDTO responseDTO = new BookSearchResponseDTO();
//
//        // 1. 建立動態篩選條件，將所有參數轉換為Specification對象
//        Specification<Book> bookSpec = createFilterSpecification(keyword, mainCategoryId, subCategoryId, seriesDisplay, authorId, publisherId, tagIds);
//
//        // 2. 使用篩選條件執行分頁查詢
//        // 這裡 bookRepository 使用了 @EntityGraph 預加載相關實體，避免N+1問題
//        Page<Book> booksPage = bookRepository.findAll(bookSpec, pageable);
//
//        // 3. 批量獲取書籍的可借閱狀態
//        Map<Long, Boolean> bookAvailabilityMap = getBooksAvailabilityMap(booksPage.getContent());
//
//        // 4. 將Entity轉換為DTO，並注入可借閱狀態
//        Page<BookListItemDTO> bookListItemPage = booksPage.map(book -> {
//            //boolean isAvailable = false;
//            // 只處理非系列書籍的可借閱狀態，系列書籍的availableForLoan實際上沒有意義
//            //if (book.getSeries() == null) {
//                boolean isAvailable = bookAvailabilityMap.getOrDefault(book.getId(), false);
//            //}
//            // 使用靜態工廠方法將Entity轉換為DTO
//            return BookListItemDTO.fromEntity(book, isAvailable, seriesDisplay);
//        });
//
//        // 5. 獲取分面搜尋統計數據
//        FacetedSearchStatsDTO stats = getFacetedSearchStats(keyword, mainCategoryId, subCategoryId, seriesDisplay, authorId, publisherId, tagIds);
//
//        // 6. 組裝最終返回結果
//        responseDTO.setBookPage(PageResponseDTO.fromPage(bookListItemPage));  // 設置分頁書籍列表
//        responseDTO.setStats(stats);                                         // 設置分面統計數據
//
//        return responseDTO;
//    }
//
//    /**
//     * 獲取特定系列的所有書籍。
//     *
//     * 功能說明:
//     * 1. 根據系列ID查詢該系列的所有書籍
//     * 2. 對結果進行分頁和排序
//     * 3. 獲取每本書的可借閱狀態
//     *
//     * 實現邏輯:
//     * - 使用Specification構建簡單的等值查詢
//     * - 復用批量獲取可借閱狀態的邏輯
//     * - 將Entity轉換為前端友好的DTO
//     *
//     * @param seriesId 系列ID，唯一標識一個書籍系列
//     * @param pageable 分頁和排序參數
//     * @return 包含該系列所有書籍的分頁結果
//     */
//    @Override
//    @EntityGraph(attributePaths = {"series"})
//    public Page<BookListItemDTO> getBooksInSeries(Long seriesId, Pageable pageable) {
//        // 1. 創建查詢規範，只查詢指定系列的書籍
//        Specification<Book> spec = (root, query, criteriaBuilder) -> {
//            return criteriaBuilder.equal(root.get("series").get("id"), seriesId);
//        };
//
//        // 2. 執行分頁查詢
//        Page<Book> booksPage = bookRepository.findAll(spec, pageable);
//
//        // 3. 批量獲取書籍的可借閱狀態（已修正，不再過濾非系列書籍）
//        Map<Long, Boolean> availabilityMap = getBooksAvailabilityMap(booksPage.getContent());
//
//        // 4. 將Entity轉換為DTO並返回
//        return booksPage.map(book -> {
//            boolean isAvailable = false;
//            if (book.getSeries() == null) {
//                isAvailable = availabilityMap.getOrDefault(book.getId(), false);
//            }
//            return BookListItemDTO.fromEntity(book, isAvailable);
//        });
//    }
//
//    /**
//     * 批量獲取書籍的可借閱狀態。
//     *
//     * 功能說明:
//     * - 接收書籍列表，返回每本書的可借閱狀態
//     *
//     * 實現邏輯:
//     * - 提取所有非系列書籍的ID
//     * - 使用單次數據庫查詢獲取所有書籍的可用副本數量
//     * - 將結果轉換為Map，便於快速查找
//     *
//     * @param books 需要獲取可借閱狀態的書籍列表
//     * @return 書籍ID到可借閱狀態的映射
//     */
//    private Map<Long, Boolean> getBooksAvailabilityMap(List<Book> books) {
//        Map<Long, Boolean> availabilityMap = new HashMap<>();
//
//        // 獲取所有書籍的ID，不再過濾非系列書籍
//        List<Long> allBookIds = books.stream()
//                //.filter(book -> book.getSeries() == null)
//                .map(Book::getId)
//                .collect(Collectors.toList());
//
//        // 查詢所有書籍的可用副本數量
//        if (!allBookIds.isEmpty()) {
//            List<Object[]> availabilityCounts = bookCopyRepository.countAvailableCopiesByBookIds(allBookIds);
//
//            // 將查詢結果轉換為Map
//            for (Object[] result : availabilityCounts) {
//                Long bookId = (Long) result[0];
//                Long count = (Long) result[1];
//                // 將可用副本數量轉換為可借閱狀態(true表示有可借閱副本)
//                availabilityMap.put(bookId, count > 0);
//            }
//        }
//        return availabilityMap;
//    }
//
//    /**
//     * 獲取分面搜尋統計數據。
//     *
//     * 功能說明:
//     * - 為每種分面（分類、作者、出版社等）計算符合條件的書籍數量
//     *
//     * 實現策略:
//     * - 針對每種分面執行單獨的統計查詢
//     * - 所有查詢共用相同的篩選條件，保持結果一致性
//     * - 使用JPA Criteria API構建動態查詢
//     *
//     * 性能考量:
//     * - 雖然執行多次查詢���但每次查詢都經過優化，使用了GROUP BY和COUNT
//     * - 對於大型資料集，可考慮使用緩存機制或異步處理
//     *
//     * @param keyword 搜尋關鍵字
//     * @param mainCategoryId 主分類ID
//     * @param subCategoryId 子分類ID
//     * @param seriesDisplay 系列顯示模式
//     * @param authorId 作者ID
//     * @param publisherId 出版社ID
//     * @param tagIds 標籤ID列表
//     * @return 包含所有分面統計數據的DTO
//     */
//    private FacetedSearchStatsDTO getFacetedSearchStats(String keyword, Long mainCategoryId, Long subCategoryId, Integer seriesDisplay, Long authorId, Long publisherId, List<Long> tagIds) {
//        FacetedSearchStatsDTO facetedSearchStatsDTO = new FacetedSearchStatsDTO();
//
//        // 使用通用方法獲取各類統計數據
//        facetedSearchStatsDTO.setCategories(getEntityStats(keyword, mainCategoryId, subCategoryId, seriesDisplay, authorId, publisherId, tagIds,
//                "categorySub", "category", "id", "categoryTitle", false));
//
//        facetedSearchStatsDTO.setSubCategories(getEntityStats(keyword, mainCategoryId, subCategoryId, seriesDisplay, authorId, publisherId, tagIds,
//                "categorySub", null, "id", "categorySubTitle", false));
//
//        facetedSearchStatsDTO.setSeries(getEntityStats(keyword, mainCategoryId, subCategoryId, seriesDisplay, authorId, publisherId, tagIds,
//                "series", null, "id", "title", true));
//
//        facetedSearchStatsDTO.setAuthors(getEntityStats(keyword, mainCategoryId, subCategoryId, seriesDisplay, authorId, publisherId, tagIds,
//                "authors", null, "id", "name", false));
//
//        facetedSearchStatsDTO.setPublishers(getEntityStats(keyword, mainCategoryId, subCategoryId, seriesDisplay, authorId, publisherId, tagIds,
//                "publisher", null, "id", "pubName", false));
//
//        facetedSearchStatsDTO.setTags(getEntityStats(keyword, mainCategoryId, subCategoryId, seriesDisplay, authorId, publisherId, tagIds,
//                "tags", null, "id", "title", false));
//
//        return facetedSearchStatsDTO;
//    }
//
//    /**
//     * 通用統計查詢方法，用於獲取各種實體的統計數據。
//     *
//     * 功能說明:
//     * - 統一處理各種分面的統計查詢邏輯
//     * - 支持一級和二級關聯的統計
//     * - 可選擇是否只統計代表作
//     *
//     * 參數說明:
//     * @param keyword 搜尋關鍵字
//     * @param mainCategoryId 主分類ID
//     * @param subCategoryId 子分類ID
//     * @param seriesDisplay 系列顯示模式
//     * @param authorId 作者ID
//     * @param publisherId 出版社ID
//     * @param tagIds 標籤ID列表
//     * @param joinAttribute 要JOIN的書籍屬性名稱
//     * @param secondJoinAttribute 要二次 JOIN 的屬性名稱（可為null）
//     * @param idAttribute 要分組的 ID 屬性名稱
//     * @param nameAttribute 要分組的名稱屬性名稱
//     * @param representativeOnly 是否只統計代表作
//     * @return 統計結果列表
//     */
//    private List<StatItemDTO> getEntityStats(String keyword, Long mainCategoryId, Long subCategoryId, Integer seriesDisplay, Long authorId, Long publisherId, List<Long> tagIds,
//                                             String joinAttribute, String secondJoinAttribute, String idAttribute, String nameAttribute, boolean representativeOnly) {
//
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();    // 建構各種查詢條件如大於，小於，等於，不等於，like，邏輯運算 and，or，排序 asc，desc等
//        CriteriaQuery<StatItemDTO> query = cb.createQuery(StatItemDTO.class); // 組成頂層的查詢語句如select，from，where，order by，group by，having，distinct。
//        Root<Book> root = query.from(Book.class); // 查詢的根實體，這裡是Book實體
//
//        // 建立主要JOIN關係
//        Join<?, ?> mainJoin = root.join(joinAttribute, JoinType.LEFT);
//
//        // 如果需要二次JOIN，則建立二次JOIN關係
//        Join<?, ?> secondJoin = null;
//        if (secondJoinAttribute != null) {
//            secondJoin = mainJoin.join(secondJoinAttribute, JoinType.LEFT);
//        }
//
//        // 應用篩選條件
//        List<Predicate> predicates = new ArrayList<>();
//        Map<String, Join<?, ?>> joinMap = new HashMap<>();
//
//        // 記錄已創建的JOIN，避免在applyFilterConditions中重複創建
//        joinMap.put("Book." + joinAttribute, mainJoin);
//        if (secondJoinAttribute != null && secondJoin != null) {
//            joinMap.put(mainJoin.getJavaType().getSimpleName() + "." + secondJoinAttribute, secondJoin);
//        }
//
//        // 應用通用篩選條件
//        applyFilterConditions(root, query, cb, predicates, keyword, mainCategoryId, subCategoryId,
//                seriesDisplay, authorId, publisherId, tagIds, joinMap);
//
//        // 如果需要只統計代表作，添加額外條件
//        if (representativeOnly) {
//            predicates.add(cb.isTrue(root.get("representative")));
//        }
//
//        // 應用WHERE條件
//        if (!predicates.isEmpty()) {
//            query.where(cb.and(predicates.toArray(new Predicate[0])));
//        }
//
//        // 確定要分組和選擇的對象
//        Join<?, ?> targetJoin = secondJoin != null ? secondJoin : mainJoin;
//
//        // 按ID和名稱分組
//        query.groupBy(targetJoin.get(idAttribute), targetJoin.get(nameAttribute));
//
//        // 選擇查詢結果應包含的欄位
//        query.multiselect(
//                targetJoin.get(idAttribute),
//                targetJoin.get(nameAttribute),
//                cb.countDistinct(root.get("id"))
//        );
//
//        // 執行查詢並返回結果
//        return entityManager.createQuery(query).getResultList();
//    }
//
//    /**
//     * 創建動態篩選條件。
//     *
//     * 功能說明:
//     * - 根據輸入參數動態構建 SQL 查詢條件，支持多種篩選條件的組合，可處理複雜的關聯查詢邏輯
//     *
//     * 實現技術:
//     * - 使用 Spring Data JPA 的 Specification 模式
//     * - 利用 JPA Criteria API 構建動態查詢
//     * - 使用 Map 緩存 JOIN 關係，避免重複 JOIN
//     *
//     * 最佳實踐:
//     * - 將各種條件判斷模組化，提高代碼可讀性
//     * - 使用 LEFT JOIN 避免數據過濾過嚴
//     * - 對字符串進行小寫處理，確保大小寫不敏感的搜索
//     *
//     * @param keyword 搜尋關鍵字
//     * @param mainCategoryId 主分類ID
//     * @param subCategoryId 子分類ID
//     * @param seriesDisplay 系列顯示模式
//     * @param authorId 作者ID
//     * @param publisherId 出版社ID
//     * @param tagIds 標籤ID列表
//     * @return 包含所有篩選條件的Specification對象
//     */
//    private Specification<Book> createFilterSpecification(String keyword, Long mainCategoryId, Long subCategoryId, Integer seriesDisplay,
//                                                          Long authorId, Long publisherId, List<Long> tagIds) {
//        return (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//            Map<String, Join<?, ?>> joinMap = new HashMap<>();
//
//            // 應用通用篩選條件邏輯
//            applyFilterConditions(root, query, criteriaBuilder, predicates, keyword, mainCategoryId, subCategoryId,
//                    seriesDisplay, authorId, publisherId, tagIds, joinMap);
//
//            // 返回組合後的條件
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//
//    /**
//     * 獲取或創建JOIN關係。
//     *
//     * 功能說明:
//     * - 避免在同一查詢中重複JOIN相同的關聯
//     * - 緩存已創建的JOIN，提高查詢效率
//     *
//     * 泛型參數:
//     * - X: 源實體類型
//     * - Y: 目標實體類型
//     *
//     * 實現細節:
//     * - 使用Map緩存JOIN關係，鍵為"實體名.屬性名"
//     * - 如果JOIN已存在，則直接返回
//     * - 否則創建新的JOIN並添加到Map��
//     *
//     * @param from 源實體或已有的JOIN
//     * @param attribute 要JOIN的屬性名
//     * @param joinMap 存儲已創建JOIN的Map
//     * @return 新創建或已存在的JOIN對象
//     */
//    @SuppressWarnings("unchecked")
//    private <X, Y> Join<X, Y> getOrCreateJoin(From<?, X> from, String attribute, Map<String, Join<?, ?>> joinMap) {
//        // 創建唯一鍵，格式為"實體名.屬性名"
//        String key = from.getJavaType().getSimpleName() + "." + attribute;
//
//        // 檢查Map中是否已存在該JOIN
//        if (joinMap.containsKey(key)) {
//            // 如果存在，強制類型轉換並返回
//            return (Join<X, Y>) joinMap.get(key);
//        } else {
//            // 如果不存在，創建新的LEFT JOIN
//            // 使用LEFT JOIN確保即使關聯實體不存在也能返回主實體記錄
//            Join<X, Y> join = from.join(attribute, JoinType.LEFT);
//
//            // 將新創建的JOIN添加到Map中
//            joinMap.put(key, join);
//
//            return join;
//        }
//    }
//
//    /**
//     * 將篩選條件應用到查詢中。
//     *
//     * 功能說明:
//     * - 統一處理所有查詢的篩選條件邏輯
//     * - 確保主查詢和統計查詢使用相同的篩選規則
//     * - 避免代碼重複，提高可維護性
//     *
//     * 參數說明:
//     * - root: 查詢的根實體
//     * - query: 查詢對象
//     * - cb: 條件構建器
//     * - predicates: 存儲條件的列表
//     * - joinMap: 已存在的JOIN關係Map
//     * - 其餘參數: 各種篩選條件
//     */
//    private void applyFilterConditions(Root<Book> root, CriteriaQuery<?> query, CriteriaBuilder cb,
//                                       List<Predicate> predicates, String keyword, Long mainCategoryId, Long subCategoryId,
//                                       Integer seriesDisplay, Long authorId, Long publisherId, List<Long> tagIds,
//                                       Map<String, Join<?, ?>> joinMap) {
//        // 系列顯示模式篩選
//        if (seriesDisplay != null && seriesDisplay == 1) {
//            predicates.add(cb.isTrue(root.get("representative")));
//        }
//
//        // 關鍵字搜索篩選
//        if (keyword != null && !keyword.isEmpty()) {
//            String likeKeyword = "%" + keyword.toLowerCase() + "%";
//            Predicate titlePredicate = cb.like(cb.lower(root.get("title")), likeKeyword);
//
//            Join<Book, Author> authorsJoin = getOrCreateJoin(root, "authors", joinMap);
//            Predicate authorNamePredicate = cb.like(cb.lower(authorsJoin.get("name")), likeKeyword);
//            predicates.add(cb.or(titlePredicate, authorNamePredicate));
//        }
//
//        // 主分類篩選
//        if (mainCategoryId != null) {
//            Join<Book, CategorySub> subCategoryJoin = getOrCreateJoin(root, "categorySub", joinMap);
//            Join<CategorySub, Category> categoryJoin = getOrCreateJoin(subCategoryJoin, "category", joinMap);
//            predicates.add(cb.equal(categoryJoin.get("id"), mainCategoryId));
//        }
//
//        // 子分類篩選
//        if (subCategoryId != null) {
//            Join<Book, CategorySub> subCategoryJoin = getOrCreateJoin(root, "categorySub", joinMap);
//            predicates.add(cb.equal(subCategoryJoin.get("id"), subCategoryId));
//        }
//
//        // 作者篩選
//        if (authorId != null) {
//            Join<Book, Author> authorsJoin = getOrCreateJoin(root, "authors", joinMap);
//            predicates.add(cb.equal(authorsJoin.get("id"), authorId));
//        }
//
//        // 出版社篩選
//        if (publisherId != null) {
//            Join<Book, Publisher> publisherJoin = getOrCreateJoin(root, "publisher", joinMap);
//            predicates.add(cb.equal(publisherJoin.get("id"), publisherId));
//        }
//
//        // 標籤篩選
//        if (tagIds != null && !tagIds.isEmpty()) {
//            Join<Book, Tag> tagsJoin = getOrCreateJoin(root, "tags", joinMap);
//            predicates.add(tagsJoin.get("id").in(tagIds));
//        }
//    }
//}
//
