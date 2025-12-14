package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.dto.loanDTO.LoanItemRespDto;
import org.matsuzaka.library_v3_back.model.entity.Loan;
import org.matsuzaka.library_v3_back.model.enums.LoanStatus;
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
    
    // Check borrowing limit
    long countByUserIdAndStatus(Long userId, LoanStatus status);
    
    // Find active loan for copy
    Optional<Loan> findByBookCopyIdAndStatus(Long bookCopyId, LoanStatus status);

    // 找出所有未歸還且已逾期的借閱記錄 (用於排程任務)
    @Query("SELECT l FROM Loan l WHERE l.status = 'ON_LOAN' AND l.dueDate < :date")
    List<Loan> findByStatusAndDueDateBefore(@Param("status") LoanStatus status, @Param("date") LocalDate date);

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
    // 修正定義，應該是尚未歸還的
    @Query(value = """
                        SELECT l.id, b.id, b.title, b.image_url, bc.unique_code, DATE(l.loan_date), l.due_date, DATE(l.return_date), GROUP_CONCAT(DISTINCT a.name ORDER BY a.name SEPARATOR ', ') AS author_name, l.status
                        FROM loan l
                        JOIN book_copy bc ON l.book_copy_id = bc.id
                        JOIN book b ON bc.book_id = b.id
                        JOIN book_author ba ON b.id = ba.book_id
                        JOIN author a ON ba.author_id = a.id
                        WHERE l.user_id = :userId 
                            AND l.return_date IS NULL 
                        GROUP BY l.id, b.id, b.title, b.image_url, bc.unique_code, l.loan_date, l.due_date, l.return_date
                        ORDER BY l.loan_date DESC ;""", nativeQuery = true)
    Set<LoanItemRespDto> findCurrentByUserId(@Param("userId") Long userId);


    // 個人借閱歷史，新增 loanId
    // 修正定義，應該是要已經歸還的 RETURNED
    @Query(value = """
                        SELECT l.id, b.id, b.title, b.image_url, bc.unique_code, DATE(l.loan_date), l.due_date, DATE(l.return_date), GROUP_CONCAT(DISTINCT a.name ORDER BY a.name SEPARATOR ', ') AS author_name, l.status
                        FROM loan l
                        JOIN book_copy bc ON l.book_copy_id = bc.id
                        JOIN book b ON bc.book_id = b.id
                        JOIN book_author ba ON b.id = ba.book_id
                        JOIN author a ON ba.author_id = a.id
                        WHERE l.user_id = :userId 
                            AND l.status = 'RETURNED'
                        GROUP BY l.id, b.id, b.title, b.image_url, bc.unique_code, l.loan_date, l.due_date, l.return_date
                        ORDER BY l.return_date DESC;""", nativeQuery = true)
    List<LoanItemRespDto> findHistoryByUserId(@Param("userId") Long userId);

    // 個人逾期未歸還 (使用 due_date 判斷)，新增 loanId
    @Query(value = """
                        SELECT l.id, b.id, b.title, b.image_url, bc.unique_code, DATE(l.loan_date), l.due_date, DATE(l.return_date), GROUP_CONCAT(DISTINCT a.name ORDER BY a.name SEPARATOR ', ') AS author_name, l.status
                        FROM loan l
                        JOIN book_copy bc ON l.book_copy_id = bc.id
                        JOIN book b ON bc.book_id = b.id
                        JOIN book_author ba ON b.id = ba.book_id
                        JOIN author a ON ba.author_id = a.id
                        WHERE l.user_id = :userId 
                            AND l.status = 'OVERDUE'
                            AND l.due_date < CURRENT_DATE()
                        GROUP BY l.id, b.id, b.title, b.image_url, bc.unique_code, l.loan_date, l.due_date, l.return_date
                        ORDER BY l.due_date Desc;""", nativeQuery = true) 
    List<LoanItemRespDto> findOverdueByUserId(@Param("userId") Long userId);


    // 找到指定 book_copy 的「目前借閱紀錄」(未還)，回傳最近一筆
    Optional<Loan> findFirstByBookCopyIdAndReturnDateIsNullOrderByLoanDateDesc(Long bookCopyId);

}

