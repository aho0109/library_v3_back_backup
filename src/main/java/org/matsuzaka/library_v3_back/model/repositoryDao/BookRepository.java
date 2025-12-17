package org.matsuzaka.library_v3_back.model.repositoryDao;


import org.matsuzaka.library_v3_back.model.entity.Book;
import org.matsuzaka.library_v3_back.model.entity.Series;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 書籍資料庫操作介面。
 * 繼承 JpaRepository 提供基礎 CRUD，同時繼承 JpaSpecificationExecutor 以支援動態查詢。
 */

/*
JPA Specification：這是 Spring Data JPA 處理動態查詢的最佳實踐。
它允許我們在 Service 層像組裝積木一樣，根據不同的輸入參數，動態地組合 WHERE 條件。這樣就不需要為每種可能的參數組合都寫一��� Repository 方法。
*/

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /**
     * 儘管 Specification 已經處理了 JOIN 的 WHERE 條件，
     * 但 EntityGraph 會告訴 JPA 在獲取結果時預先載入這些關聯實體的資料，
     * 從而在服務層將 Entity 轉換為 DTO 時，可以避免對每個關聯實體都發出額外的查詢（即 N+1 問題）。這確保了效能的最佳化。
     * @param spec 查詢條件
     * @param pageable 分頁參數
     * @return 分頁結果
     */
    @EntityGraph(attributePaths = {"authors", "tags", "publisher", "series", "categorySub"})
    Page<Book> findAll(Specification<Book> spec, Pageable pageable);

    /**
     * 根據 ISBN 查詢書籍。ISBN 為業務唯一識別碼，確保查詢的穩定性。
     * @param isbn 國際標準書號
     * @return Optional<Book>
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * 根據書籍標題模糊查詢。
     * @param title 書籍標題
     * @return 符合條件的書籍列表
     */
    List<Book> findByTitleContaining(String title);




    /* 首頁，各種預設熱門查詢 */

    /**
     * 查詢借閱前五名。
     * @param categoryId 分類ID
     * @return 借閱量前五名的書籍列表
     */
    @Query(value = "SELECT b FROM Book b " +
           "JOIN FETCH b.authors a " +
           "JOIN FETCH b.publisher p " +
           "LEFT JOIN FETCH b.series s " +
           "JOIN FETCH b.categorySub cs " +
           "JOIN FETCH cs.category c " +
           "LEFT JOIN b.bookCopies bc " +
           "LEFT JOIN Loan l ON l.bookCopy = bc " +
           "WHERE (:categoryId IS NULL OR c.id = :categoryId) " +
           "GROUP BY b.id " +
           "ORDER BY b.totalLoanCount DESC")
    @EntityGraph(attributePaths = {"authors", "publisher"})
    List<Book> findTop5Loan(@Param("categoryId") Long categoryId,  Pageable pageable);

    /**
     * 查詢最新前五名。
     * @param categoryId 分類ID
     * @return 最新上架前五名的書籍列表
     */
    @Query(value = "SELECT b FROM Book b " +
            "JOIN FETCH b.authors a " +
            "JOIN FETCH b.publisher p " +
            "LEFT JOIN FETCH b.series s " +
            "JOIN FETCH b.categorySub cs " +
            "JOIN FETCH cs.category c " +
            "WHERE (:categoryId IS NULL OR c.id = :categoryId) " +
            "ORDER BY b.addedDate DESC")
    @EntityGraph(attributePaths = {"authors", "publisher"})
    List<Book> findTop5New(@Param("categoryId") Long categoryId, Pageable pageable);


    /**
     * 根據書籍ID查詢詳細資訊(for 讀者端)。
     * 使用 JOIN FETCH 來避免 N+1 問題，確保在查詢書籍時，同時載入相關的作者、出版社、系列、分類子項、分類、書籍副本和標籤等關聯實體。
     * 參與到的table有：book, author, publisher, series, category, categorySub, bookCopy, tag
     */
    @Query("SELECT b FROM Book b " +
            "JOIN b.authors a " +
            "JOIN b.publisher p " +
            "LEFT JOIN b.series s " +
            "JOIN b.categorySub cs " +
            "JOIN cs.category c " +
            "LEFT JOIN b.bookCopies bc " +
            "LEFT JOIN b.tags t " +
            "WHERE b.id = :bookId")
    @EntityGraph(attributePaths = {"authors", "publisher", "series", "categorySub", "categorySub.category", "bookCopies", "tags"}) // 如果你查詢時需要 category 的資料，建議加上 "categorySub.category"
    Optional<Book> findOneByIdWithDetails(@Param("bookId") Long bookId);


    // 新增書籍
    /**
     * 將上一集的代表取消
     * 1 = true; 0 = false
     * 參數選擇使用 Series 和代表作標誌，這樣可以更發揮 JPA 的靈活。
     * */
    @EntityGraph(attributePaths = {"series"})
    Optional<Book> findBySeriesAndRepresentative(Series series, Boolean representative);


}