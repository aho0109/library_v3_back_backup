package org.matsuzaka.library_v3_back.model.repositoryDao;


import org.matsuzaka.library_v3_back.dto.loanDTO.BookCopyRespDto;
import org.matsuzaka.library_v3_back.model.entity.Book;
import org.matsuzaka.library_v3_back.model.entity.BookCopy;
import org.matsuzaka.library_v3_back.model.enums.BookCopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    // 原有方法 - 查詢單本書的可用副本數量
    long countByBookIdAndStatus(Long bookId, BookCopyStatus status);

    // 新增方法 - 批量查詢多本書的可用副本數量
    @Query("SELECT bc.book.id AS bookId, COUNT(bc) AS count FROM BookCopy bc " +
            "WHERE bc.book.id IN :bookIds AND bc.status = 'A' " +
            "GROUP BY bc.book.id")
    List<Object[]> countAvailableCopiesByBookIds(@Param("bookIds") List<Long> bookIds);

    // 新增方法 - 查詢指定書籍的所有副本
    List<BookCopy> findByBookId(Long bookId);
    
    // Find by Book entity
    List<BookCopy> findByBook(Book book);

    // 新增方法 - 批量查詢多本書的總副本數量
    @Query("SELECT bc.book.id AS bookId, COUNT(bc) AS totalCount FROM BookCopy bc " +
            "WHERE bc.book.id IN :bookIds " +
            "GROUP BY bc.book.id")
    List<Object[]> countTotalCopiesByBookIds(@Param("bookIds") List<Long> bookIds);



    // 新增方法 - 查詢副本的目前借閱者
    @Query("SELECT l.user.account FROM Loan l " +
            "WHERE l.bookCopy.id = :copyId AND l.returnDate IS NULL " +
            "ORDER BY l.loanDate DESC")
    Optional<String> findCurrentBorrowerByCopyId(@Param("copyId") Long copyId);

    // 新增方法 - 批量查詢多個副本的目前借閱者
    @Query("SELECT l.bookCopy.id, l.user.account FROM Loan l " +
            "WHERE l.bookCopy.id IN :copyIds AND l.returnDate IS NULL")
    List<Object[]> findCurrentBorrowersByCopyIds(@Param("copyIds") List<Long> copyIds);

    // 新增方法 - 批量查詢多個副本的借閱詳細資訊
    @Query("SELECT l.bookCopy.id, l.user.account, l.loanDate, l.dueDate, l.returnDate    , l.id, l.user.id " +
            "FROM Loan l " +
            "WHERE l.bookCopy.id IN :copyIds AND l.returnDate IS NULL")
    List<Object[]> findCurrentLoanDetailsByCopyIds(@Param("copyIds") List<Long> copyIds);



    // 根據唯一編碼查詢
    Optional<BookCopy> findByUniqueCode(String uniqueCode);

    // 根據狀態查詢
    List<BookCopy> findByStatus(BookCopyStatus status);

    // 根據書籍ID和狀態查詢
    List<BookCopy> findByBookIdAndStatus(Long bookId, BookCopyStatus status);

    // 檢查唯一編碼是否存在
    boolean existsByUniqueCode(String uniqueCode);


    //使用 Optional 的情況：
    //當查詢預期只返回一個結果時（或零個結果）
    //通常用於根據唯一識別碼（如 ID、unique code）查詢
    //幫助更優雅地處理"查無資料"的情況
    //強制開發者處理 null 的情況

    //使用 List 的情況：
    //當查詢可能返回多個結果時
    //例如根據書籍 ID 查詢所有副本、根據狀態查詢等
    //這些情況下，List 可以容納多個結果，而 Optional 只能容納一個或零個結果
    //用於集合查詢（如按狀態、分類等查詢）
    //注意：在使用 List 查詢時，Spring Data JPA 會自動處理查無資料的情況，返回空列表而不是 null
    //這樣可以避免 null 檢查的麻煩，讓程式碼更簡潔
    //可以直接使用 stream 操作

    // 特定書籍借閱狀態，原始
    List<BookCopy> findAllByBookId(Long bookId);

    // 特定書籍借閱狀態，改 DTO ，只顯示 uniqueCode 和 status
    @Query(value = "SELECT bc.unique_code AS '書籍碼',\n" +
            "       CASE bc.status\n" +
            "           WHEN 'A' THEN '可借閱'\n" +
            "           WHEN 'L' THEN '已借出'\n" +
            "           WHEN 'R' THEN '已預約'\n" +
            "           ELSE '未知狀態'\n" +
            "       END AS '借閱狀態'\n" +
            "FROM book_copy bc\n" +
            "WHERE book_id = :bookId;", nativeQuery = true)
    List<BookCopyRespDto> findUniCodeAndStatusByBookId(@Param("bookId") Long bookId);



    /* 借閱功能 */

    // 2. 查找 ID 最小的可借閱書籍副本
    @Query(value = "SELECT * FROM book_copy WHERE book_id = :bookId AND status = 'A' ORDER BY id ASC LIMIT 1", nativeQuery = true)
    Optional<BookCopy> findFirstAvailableCopyByBookId(@Param("bookId") Long bookId);
    // 3. 更新書籍副本狀態為 'L' (On Loan)
    @Modifying
    @Query(value = "UPDATE book_copy SET status = 'L' WHERE id = :copyId", nativeQuery = true)
    int updateBookCopyStatusToOnLoan(@Param("copyId") Long copyId);

    /* 歸還功能 */

    // 1. 更新書籍副本狀態為 'A' (Available)
    @Modifying
    @Query(value = "UPDATE book_copy SET status = 'A' WHERE id = :copyId", nativeQuery = true)
    int updateBookCopyStatusToAvailable(@Param("copyId") Long copyId);

    // 2. 更新 loan 表中的 return_date
    // 已經在 LoanServiceImpl 中實現了，這裡不需要重複實現
}

