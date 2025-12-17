package org.matsuzaka.library_v3_back.dto.queryDTO;

import lombok.Data;
import org.matsuzaka.library_v3_back.model.entity.BookCopy;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 書籍副本 DTO
 * 用於管理員頁面顯示副本詳細資訊
 */
@Data
public class BookCopyDTO {

    private Long id;                    // 副本ID
    private String uniqueCode;          // 副本唯一碼
    private String status;              // 副本狀態 (A/L/R)
    private String statusDisplay;       // 狀態顯示文字
    private String currentBorrowerAccount; // 目前借閱者帳號
    private Long bookId;                // 所屬書籍ID
    private LocalDate stockedDate;      // 進貨日期

    // 新增借閱詳細資訊
    private LocalDateTime loanDate;     // 借閱日期
    private LocalDate dueDate;          // 到期日期
    private LocalDateTime returnDate;   // 歸還日期
    private Long loanId;          // 借閱記錄ID
    private Long userId;          // 使用者ID (目前借閱者)

    /**
     * 從實體轉換為 DTO
     */
    public static BookCopyDTO fromEntity(BookCopy bookCopy) {
        BookCopyDTO dto = new BookCopyDTO();
        dto.setId(bookCopy.getId());
        dto.setUniqueCode(bookCopy.getUniqueCode());
        dto.setStatus(bookCopy.getStatus().name());
        dto.setBookId(bookCopy.getBook().getId());
        dto.setStockedDate(bookCopy.getStockedDate());

        // 設定狀態顯示文字
        switch (bookCopy.getStatus()) {
            case A:
                dto.setStatusDisplay("在館");
                break;
            case L:
                dto.setStatusDisplay("已借出");
                break;
            case P:
                dto.setStatusDisplay("處理中");
                break;
            case R:
                dto.setStatusDisplay("被預約");
                break;
            case U:
                dto.setStatusDisplay("已下架");
                break;
        }

        // 目前借閱者和借閱詳細資訊將在服務層設定
        dto.setCurrentBorrowerAccount(null);
        dto.setLoanDate(null);
        dto.setDueDate(null);
        dto.setReturnDate(null);

        dto.setLoanId(null);
        dto.setUserId(null);

        return dto;
    }
}
