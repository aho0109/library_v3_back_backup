package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.dto.loanDTO.LoanItemRespDto;
import org.matsuzaka.library_v3_back.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    // 找出單一使用者特定書籍的未歸還記錄
    Optional<Loan> findByUserIdAndBookCopyIdAndReturnDateIsNull(Long userId, Long bookCopyId);

    // 找出所有未歸還且已逾期的借閱記錄 (用於排程任務)
    @Query("SELECT l FROM Loan l WHERE l.status = 'ON_LOAN' AND l.dueDate < :date")
    List<Loan> findByStatusAndDueDateBefore(Loan.LoanStatus status, LocalDate date);




    // 根據使用者ID查詢所有借閱記錄
    List<Loan> findByUserId(Long userId);

    // 查詢使用者的未歸還借閱記錄
    List<Loan> findByUserIdAndReturnDateIsNull(Long userId);

    // 根據書籍副本ID查詢借閱記錄
    List<Loan> findByBookCopyId(Long bookCopyId);

    // 查詢特定副本的當前借閱狀態（未歸還的記錄）
    Optional<Loan> findByBookCopyIdAndReturnDateIsNull(Long bookCopyId);

    // 查詢指定日期範圍內的借閱記錄
    List<Loan> findByLoanDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 查詢所有逾期未還的借閱記錄（如果有預計歸還日期的需求可以加上）
    List<Loan> findByReturnDateIsNull();


    // 個人借閱中 (我的書櫃)，新增 loanId
    @Query(value = """
                        SELECT l.id, b.id, b.title, b.image_url, bc.unique_code, l.loan_date, l.due_date, l.return_date, GROUP_CONCAT(DISTINCT a.name ORDER BY a.name SEPARATOR ', ') AS author_name
                        FROM loan l
                        JOIN book_copy bc ON l.book_copy_id = bc.id
                        JOIN book b ON bc.book_id = b.id
                        JOIN book_author ba ON b.id = ba.book_id
                        JOIN author a ON ba.author_id = a.id
                        WHERE l.user_id = :userId -- 假設1是當前使用者的ID
                            AND l.return_date IS NULL -- 只查詢未歸還的借閱記錄
                        GROUP BY l.id, b.id, b.title, b.image_url, bc.unique_code, l.loan_date, l.due_date, l.return_date
                        ORDER BY l.loan_date DESC ;""", nativeQuery = true)
    // 有用到聚合函數，就要記得 GROUP BY 剩餘的非聚合欄位，不然會報錯
    Set<LoanItemRespDto> findCurrentByUserId(@Param("userId") Long userId);

    /*@Query(value = """
            SELECT
                l.id,
                b.id AS bookId,
                b.title AS title,
                b.image_url AS imageUrl,
                bc.unique_code AS uniqueCode,
                l.loan_date AS loanDate,
                l.due_date AS dueDate,
                l.return_date AS returnDate,
                GROUP_CONCAT(DISTINCT a.name ORDER BY a.name SEPARATOR ',') AS authors
            FROM loan l
            JOIN book_copy bc ON l.book_copy_id = bc.id
            JOIN book b ON bc.book_id = b.id
            JOIN book_author ba ON b.id = ba.book_id
            JOIN author a ON ba.author_id = a.id
            WHERE l.user_id = :userId
              AND l.return_date IS NULL
            GROUP BY l.id, b.id, b.title, b.image_url, bc.unique_code, l.loan_date, l.due_date, l.return_date
            ORDER BY l.loan_date DESC
            """, nativeQuery = true)
    Set<LoanItemRespDto> findCurrentByUserId(@Param("userId") Long userId);*/


    // 個人借閱歷史，新增 loanId
    @Query(value = """
                        SELECT l.id, b.id, b.title, b.image_url, bc.unique_code, l.loan_date, l.due_date, l.return_date, GROUP_CONCAT(DISTINCT a.name ORDER BY a.name SEPARATOR ', ') AS author_name
                        FROM loan l
                        JOIN book_copy bc ON l.book_copy_id = bc.id
                        JOIN book b ON bc.book_id = b.id
                        JOIN book_author ba ON b.id = ba.book_id
                        JOIN author a ON ba.author_id = a.id
                        WHERE l.user_id = :userId -- 假設1是當前使用者的ID
                            AND l.return_date IS NOT NULL -- 只查詢已歸還的借閱記錄
                        GROUP BY l.id, b.id, b.title, b.image_url, bc.unique_code, l.loan_date, l.due_date, l.return_date
                        ORDER BY l.return_date DESC;""", nativeQuery = true)
    List<LoanItemRespDto> findHistoryByUserId(@Param("userId") Long userId);

    // 個人逾期未歸還 (假設借閱期為 30 天)，新增 loanId
    @Query(value = """
                        SELECT l.id, b.id, b.title, b.image_url, bc.unique_code, l.loan_date, l.due_date, l.return_date, GROUP_CONCAT(DISTINCT a.name ORDER BY a.name SEPARATOR ', ') AS author_name
                        FROM loan l
                        JOIN book_copy bc ON l.book_copy_id = bc.id
                        JOIN book b ON bc.book_id = b.id
                        JOIN book_author ba ON b.id = ba.book_id
                        JOIN author a ON ba.author_id = a.id
                        WHERE l.user_id = :userId -- 假設1是當前使用者的ID
                            AND l.return_date IS NULL -- 尚未歸還
                            AND l.loan_date < NOW() - INTERVAL 30 DAY -- 超過規定30天未歸還
                        GROUP BY l.id, b.id, b.title, b.image_url, bc.unique_code, l.loan_date, l.due_date, l.return_date
                        ORDER BY l.due_date Desc;""", nativeQuery = true) // 使用資料庫的日期函數
    List<LoanItemRespDto> findOverdueByUserId(@Param("userId") Long userId);

    // 收藏功能 (需要一個 FavoritesRepository 和對應的 DTO)
    // List<LoanItemRespDto> findFavoritesByUserId(@Param("userId") Long userId);

    // 借閱功能 insert

}
