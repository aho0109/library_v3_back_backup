//package com.matsuzaka.library_normal.service.Impl;
//
//import com.matsuzaka.library_normal.dto.queryDTO.BookListItemDTO;
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
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 書籍業務邏輯處理服務。
// * 負責處理複雜的書籍搜尋、篩選和統計邏輯，是 Controller 與 Repository 之間的橋樑。
// * 遵守最佳實踐，將業務邏輯與 JPA Entity 保持分離。
// */
//
///* JPA Specification：這是 Spring Data JPA 處理動態查詢的最佳實踐。它允許我們在 Service 層像組裝積木一樣，根據不同的輸入參數，動態地組合 WHERE 條件。這樣就不需要為每種可能的參數組合都寫一個 Repository 方法。
// Specification 註解：
// criteriaBuilder.like() 處理模糊搜尋。
// criteriaBuilder.or() 將多個條件用 OR 組合。
// root.join() 處理多對多關聯和多對一關聯的篩選。
// criteriaBuilder.and() 將所有條件用 AND 組合起來。
//
// 分頁和 DTO 轉換：bookRepository.findAll(spec, pageable) 會自動執行帶有動態條件的分頁查詢。
// booksPage.map() 則將每一筆查詢結果從 Book Entity 轉換成 BookListItemDTO，同時在轉換過程中加入了「是否可借閱」的業務邏輯。
// 職責分離：為了判斷書籍是否可借閱，我們注入了 BookCopyRepository。這是 Service 層處理跨多個實體業務邏輯的標準做法。
// */
//
//@Service
//public class BookServiceImpl02 implements BookService {
//
//    // 推薦的建構子注入方式，確保相依性不可變且明確
//    private final BookRepository bookRepository;
//    private final BookCopyRepository bookCopyRepository; // 新增 BookCopyRepository 相依性
//    private final EntityManager entityManager;
//
//
//    /**
//     * 建構子注入。
//     * Spring 會自動將 BookRepository 和 BookCopyRepository 注入到這個服務中。
//     *
//     * @param bookRepository 處理 Book 實體資料庫操作的 Repository
//     * @param bookCopyRepository 處理 BookCopy 實體資料庫操作的 Repository
//     */
//    public BookServiceImpl02(BookRepository bookRepository, BookCopyRepository bookCopyRepository, EntityManager entityManager) {
//        this.entityManager = entityManager;
//        this.bookRepository = bookRepository;
//        this.bookCopyRepository = bookCopyRepository;
//    }
//
//    /**
//     * 實現多重篩選、關鍵字搜尋和分頁功能的書籍搜尋方法。
//     * 這是服務層的核心方法，將多個可選參數組合成單一的資料庫查詢。
//     *
//     * @param keyword          使用者輸入的關鍵字
//     * @param mainCategoryId   主分類ID
//     * @param subCategoryId    子分類ID
//     * @param seriesDisplay    系列作顯示模式 (1:顯示系列, 0:不顯示系列)
//     * @param authorId         作者ID
//     * @param publisherId      出版社ID
//     * @param tagIds           標籤ID列表
//     * @param pageable         分頁和排序資訊 (來自 Controller 的 Pageable 參數)
//     * @return 包含書籍列表和分頁資訊的 Page 物件，其中書籍是 BookListItemDTO 類型
//     */
//    @Override
//    public Page<BookListItemDTO> searchAndFilterBooks(String keyword, Long mainCategoryId, Long subCategoryId, Integer seriesDisplay,
//                                                      Long authorId, Long publisherId, List<Long> tagIds, Pageable pageable) {
//
//        // JPA Specification 是一種型別安全的動態查詢方式。
//        // 它允許我們用 Java 物件的方式來動態組合 SQL 的 WHERE 條件。
//        Specification<Book> spec = (root, query, criteriaBuilder) -> {
//
//            // 手動添加 Fetch Join，這能解決 N+1 查詢問題
//            /*if (query.getRestriction() == null) {
//                root.fetch("authors", jakarta.persistence.criteria.JoinType.LEFT);
//                root.fetch("tags", jakarta.persistence.criteria.JoinType.LEFT);
//                root.fetch("publisher", jakarta.persistence.criteria.JoinType.LEFT);
//                root.fetch("series", jakarta.persistence.criteria.JoinType.LEFT);
//                root.fetch("categorySub", jakarta.persistence.criteria.JoinType.LEFT);
//                root.fetch("categorySub").fetch("category", jakarta.persistence.criteria.JoinType.LEFT);
//            }*/
//            // 關鍵修正: 移除 root.fetch() 語句。在 Pageable 查詢上，集合關聯應使用 @BatchSize。
//            // 只有在沒有 where 條件時，才應該 fetch，但這裡的業務邏輯已經讓查詢更複雜了。
//            // 我們依賴 @BatchSize 來優化效能，避免 fetch join 導致的問題。
//
//            List<Predicate> predicates = new ArrayList<>();
//
//            // 0. 根據 seriesDisplay 參數添加不同的篩選條件
//            if (seriesDisplay != null && seriesDisplay == 1) {
//                // 如果 seriesDisplay=1，只顯示代表作
//                predicates.add(criteriaBuilder.isTrue(root.get("representative")));
//            }
//            // seriesDisplay=0 或 null 時，沒有額外篩選，顯示所有書籍
//
//
//            // 1. 關鍵字篩選 (keyword)
//            if (keyword != null && !keyword.isEmpty()) {
//                String likeKeyword = "%" + keyword.toLowerCase() + "%";
//                // 檢查書籍標題是否包含關鍵字
//                Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeKeyword);
//
//                // 透過 JOIN 關聯作者實體，檢查作者名稱是否包含關鍵字
//                // 這需要先建立一個 JOIN 物件，避免重複 JOIN 且可以存取關聯實體的欄位
//                Join<Book, Author> authorsJoin = root.join("authors");
//                Predicate authorNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(authorsJoin.get("name")), likeKeyword);
//
//                // 使用 criteriaBuilder.or() 將多個篩選條件用 OR 邏輯組合
//                predicates.add(criteriaBuilder.or(titlePredicate, authorNamePredicate));
//            }
//
//            // 2. 分類篩選 (category)
//            if (mainCategoryId != null) {
//                // 透過串聯的 get() 方法來存取深層關聯的屬性
//                // 這裡會自動產生 SQL JOIN category_sub... JOIN category... 的語句
//                Join<CategorySub, Category> categoryJoin = root.join("categorySub").join("category");
//                predicates.add(criteriaBuilder.equal(root.get("categorySub").get("category").get("id"), mainCategoryId));
//            }
//            if (subCategoryId != null) {
//                Join<Book, CategorySub> subCategoryJoin = root.join("categorySub");
//                predicates.add(criteriaBuilder.equal(root.get("categorySub").get("id"), subCategoryId));
//            }
//
//            // 3. 系列作顯示篩選 (series_display)
////            if (seriesDisplay != null) {
////                if (seriesDisplay == 1) { // 顯示系列作
////                    // criteriaBuilder.isNotNull() 檢查 series_id 欄位是否不為 NULL
////                    predicates.add(criteriaBuilder.isNotNull(root.get("series")));
////                } else if (seriesDisplay == 0) { // 只顯示單行本
////                    // criteriaBuilder.isNull() 檢查 series_id 欄位是否為 NULL
////                    predicates.add(criteriaBuilder.isNull(root.get("series")));
////                }
////            }
//
//            // 4. 作者篩選 (authorId)
//            if (authorId != null) {
//                // 透過 JOIN 關聯 author 實體，篩選其 ID
//                Join<Book, Author> authorsJoin = root.join("authors");
//                predicates.add(criteriaBuilder.equal(authorsJoin.get("id"), authorId));
//            }
//
//            // 5. 出版社篩選 (publisherId)
//            if (publisherId != null) {
//                // 透過 JOIN 關聯 publisher 實體，篩選其 ID
//                Join<Book, Publisher> publisherJoin = root.join("publisher");
//                predicates.add(criteriaBuilder.equal(root.get("publisher").get("id"), publisherId));
//            }
//
//            // 6. 標籤篩選 (tagIds)
//            if (tagIds != null && !tagIds.isEmpty()) {
//                // 透過 JOIN 關聯 tags 實體
//                Join<Book, Tag> tagsJoin = root.join("tags");
//                // 使用 in() 方法來處理多個標籤 ID
//                predicates.add(tagsJoin.get("id").in(tagIds));
//            }
//
//            // 使用 criteriaBuilder.and() 將所有條件用 AND 邏輯組合起來，生成最終的 WHERE 條件
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        };
//
//        // 執行查詢並獲取分頁結果。findAll() 會自動處理 JOIN 和 WHERE
//        // 直接使用 JpaRepository 的分頁查詢，將邏輯推回資料庫
//        Page<Book> booksPage = bookRepository.findAll(spec, pageable);
//
//        // 將 JPA Entity 轉換為 DTO，並計算可借閱狀態
//        return booksPage.map(book -> {
//            boolean isAvailable = false;
//            // 只有單行本才需要查詢可借閱狀態
//            if (book.getSeries() == null) {
//                // 查詢可借閱的實體副本數量，這是 Service 層的業務邏輯
//                long availableCopiesCount = bookCopyRepository.countByBookIdAndStatus(book.getId(), BookCopy.BookCopyStatus.A);
//                isAvailable = availableCopiesCount > 0;
//            }
//            return BookListItemDTO.fromEntity(book, isAvailable);
//        });
//    }
//
//    @Override
//    public Page<BookListItemDTO> getBooksInSeries(Long seriesId, Pageable pageable) {
//        // 專門為系列作詳情頁面設計的查詢方法
//        Specification<Book> spec = (root, query, criteriaBuilder) -> {
//            /*root.fetch("authors", jakarta.persistence.criteria.JoinType.LEFT);
//            root.fetch("tags", jakarta.persistence.criteria.JoinType.LEFT);
//            root.fetch("publisher", jakarta.persistence.criteria.JoinType.LEFT);
//            root.fetch("series", jakarta.persistence.criteria.JoinType.LEFT);
//            root.fetch("categorySub", jakarta.persistence.criteria.JoinType.LEFT);
//            root.fetch("categorySub").fetch("category", jakarta.persistence.criteria.JoinType.LEFT);*/
//
//            return criteriaBuilder.equal(root.get("series").get("id"), seriesId);
//        };
//
//        Page<Book> seriesBooksPage = bookRepository.findAll(spec, pageable);
//
//        return seriesBooksPage.map(book -> {
//            boolean isAvailable = false;
//            if (book.getSeries() == null) {
//                long availableCopiesCount = bookCopyRepository.countByBookIdAndStatus(book.getId(), BookCopy.BookCopyStatus.A);
//                isAvailable = availableCopiesCount > 0;
//            }
//            return BookListItemDTO.fromEntity(book, isAvailable);
//        });
//    }
//
//
////    /**
////     * 獲取分面搜尋面板的統計資料。
////     * 這個方法需要執行多個高效能的 GROUP BY 查詢。
////     *
////     * @return 包含各類別、作者等統計數量的 DTO
////     */
////    @Override
////    public FacetedSearchStatsDTO getFacetedSearchStats(String keyword, Long mainCategoryId, Long subCategoryId, Integer seriesDisplay,
////                                                       Long authorId, Long publisherId, List<Long> tagIds) {
////        FacetedSearchStatsDTO stats = new FacetedSearchStatsDTO();
////
////        // 共用相同的 Specification 邏輯來確保篩選條件一致
////        Specification<Book> spec = createFilterSpecification(keyword, mainCategoryId, subCategoryId, seriesDisplay, authorId, publisherId, tagIds);
////
////        // 獲取各個分面的統計資料
////        stats.setCategories(getCategoryStats(spec));
////        stats.setSubCategories(getSubCategoryStats(spec));
////        stats.setSeries(getSeriesStats(spec));
////        stats.setAuthors(getAuthorStats(spec));
////        stats.setPublishers(getPublisherStats(spec));
////        stats.setTags(getTagStats(spec));
////
////        return stats;
////    }
//
//    /**
//     * 輔助方法：創建一個共用的 Specification 實例，用於主查詢和統計查詢。
//     * 這樣可以確保兩者的篩選邏輯是完全一致的。
//     */
//    private Specification<Book> createFilterSpecification(String keyword, Long mainCategoryId, Long subCategoryId, Integer seriesDisplay,
//                                                          Long authorId, Long publisherId, List<Long> tagIds) {
//        return (root, query, criteriaBuilder) -> {
//            // 在這裡的查詢中，我們不需要 fetch join
//            // 因為這些是獨立的統計查詢，只需要計數，不需要載入完整的 Entity
//            List<Predicate> predicates = new ArrayList<>();
//
//            // 根據 seriesDisplay 參數添加不同的篩選條件
//            if (seriesDisplay != null && seriesDisplay == 1) {
//                predicates.add(criteriaBuilder.isTrue(root.get("representative")));
//            }
//
//            // 關鍵字篩選
//            if (keyword != null && !keyword.isEmpty()) {
//                String likeKeyword = "%" + keyword.toLowerCase() + "%";
//                Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeKeyword);
//                Join<Book, Author> authorsJoin = root.join("authors");
//                Predicate authorNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(authorsJoin.get("name")), likeKeyword);
//                predicates.add(criteriaBuilder.or(titlePredicate, authorNamePredicate));
//            }
//
//            // 分類篩選
//            if (mainCategoryId != null) {
//                Join<CategorySub, Category> categoryJoin = root.join("categorySub").join("category");
//                predicates.add(criteriaBuilder.equal(categoryJoin.get("id"), mainCategoryId));
//            }
//            if (subCategoryId != null) {
//                Join<Book, CategorySub> subCategoryJoin = root.join("categorySub");
//                predicates.add(criteriaBuilder.equal(subCategoryJoin.get("id"), subCategoryId));
//            }
//
//            // 作者篩選
//            if (authorId != null) {
//                Join<Book, Author> authorsJoin = root.join("authors");
//                predicates.add(criteriaBuilder.equal(authorsJoin.get("id"), authorId));
//            }
//
//            // 出版社篩選
//            if (publisherId != null) {
//                Join<Book, Publisher> publisherJoin = root.join("publisher");
//                predicates.add(criteriaBuilder.equal(publisherJoin.get("id"), publisherId));
//            }
//
//            // 標籤篩選
//            if (tagIds != null && !tagIds.isEmpty()) {
//                Join<Book, Tag> tagsJoin = root.join("tags");
//                predicates.add(tagsJoin.get("id").in(tagIds));
//            }
//
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//
//    /**
//     * 輔助方法：獲取分類統計。
//     */
//    private List<StatItemDTO> getCategoryStats(Specification<Book> spec) {
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<StatItemDTO> query = cb.createQuery(StatItemDTO.class);
//        Root<Book> root = query.from(Book.class);
//
//        // 建立 JOIN
//        Join<Book, CategorySub> subCategoryJoin = root.join("categorySub");
//        Join<CategorySub, Category> categoryJoin = subCategoryJoin.join("category");
//
//        // 根據主分類 ID 和名稱進行分組
//        query.groupBy(categoryJoin.get("id"), categoryJoin.get("categoryTitle"));
//        query.multiselect(
//                categoryJoin.get("id"),
//                categoryJoin.get("categoryTitle"),
//                cb.count(root.get("id"))
//        );
//        query.where(spec.toPredicate(root, query, cb));
//
//        return entityManager.createQuery(query).getResultList();
//    }
//
//    /**
//     * 輔助方法：獲取子分類統計。
//     */
//    private List<StatItemDTO> getSubCategoryStats(Specification<Book> spec) {
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<StatItemDTO> query = cb.createQuery(StatItemDTO.class);
//        Root<Book> root = query.from(Book.class);
//
//        // 建立 JOIN
//        Join<Book, CategorySub> subCategoryJoin = root.join("categorySub");
//
//        // 根據子分類 ID 和名稱進行分組
//        query.groupBy(subCategoryJoin.get("id"), subCategoryJoin.get("categorySubTitle"));
//        query.multiselect(
//                subCategoryJoin.get("id"),
//                subCategoryJoin.get("categorySubTitle"),
//                cb.count(root.get("id"))
//        );
//        query.where(spec.toPredicate(root, query, cb));
//
//        return entityManager.createQuery(query).getResultList();
//    }
//
//    /**
//     * 輔助方法：獲取系列作統計。
//     */
//    private List<StatItemDTO> getSeriesStats(Specification<Book> spec) {
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<StatItemDTO> query = cb.createQuery(StatItemDTO.class);
//        Root<Book> root = query.from(Book.class);
//
//        // 建立 JOIN
//        Join<Book, Series> seriesJoin = root.join("series");
//
//        // 只統計代表作
//        Predicate representativePredicate = cb.isTrue(root.get("representative"));
//
//        query.groupBy(seriesJoin.get("id"), seriesJoin.get("title"));
//        query.multiselect(
//                seriesJoin.get("id"),
//                seriesJoin.get("title"),
//                cb.count(root.get("id"))
//        );
//
//        // 將篩選和代表作的條件結合
//        Predicate combinedPredicate = cb.and(spec.toPredicate(root, query, cb), representativePredicate);
//        query.where(combinedPredicate);
//
//        return entityManager.createQuery(query).getResultList();
//    }
//
//    /**
//     * 輔助方法：獲取作者統計。
//     */
//    private List<StatItemDTO> getAuthorStats(Specification<Book> spec) {
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<StatItemDTO> query = cb.createQuery(StatItemDTO.class);
//        Root<Book> root = query.from(Book.class);
//
//        // 建立 JOIN
//        Join<Book, Author> authorJoin = root.join("authors");
//
//        query.groupBy(authorJoin.get("id"), authorJoin.get("name"));
//        query.multiselect(
//                authorJoin.get("id"),
//                authorJoin.get("name"),
//                cb.count(root.get("id"))
//        );
//        query.where(spec.toPredicate(root, query, cb));
//
//        return entityManager.createQuery(query).getResultList();
//    }
//
//    /**
//     * 輔助方法：獲取出版社統計。
//     */
//    private List<StatItemDTO> getPublisherStats(Specification<Book> spec) {
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<StatItemDTO> query = cb.createQuery(StatItemDTO.class);
//        Root<Book> root = query.from(Book.class);
//
//        // 建立 JOIN
//        Join<Book, Publisher> publisherJoin = root.join("publisher");
//
//        query.groupBy(publisherJoin.get("id"), publisherJoin.get("pubName"));
//        query.multiselect(
//                publisherJoin.get("id"),
//                publisherJoin.get("pubName"),
//                cb.count(root.get("id"))
//        );
//        query.where(spec.toPredicate(root, query, cb));
//
//        return entityManager.createQuery(query).getResultList();
//    }
//
//    /**
//     * 輔助方法：獲取標籤統計。
//     */
//    private List<StatItemDTO> getTagStats(Specification<Book> spec) {
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<StatItemDTO> query = cb.createQuery(StatItemDTO.class);
//        Root<Book> root = query.from(Book.class);
//
//        // 建立 JOIN
//        Join<Book, Tag> tagJoin = root.join("tags");
//
//        query.groupBy(tagJoin.get("id"), tagJoin.get("title"));
//        query.multiselect(
//                tagJoin.get("id"),
//                tagJoin.get("title"),
//                cb.count(root.get("id"))
//        );
//        query.where(spec.toPredicate(root, query, cb));
//
//        return entityManager.createQuery(query).getResultList();
//    }
//}