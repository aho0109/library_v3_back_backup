package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.loanDTO.ReturnResponseDto;
import org.matsuzaka.library_v3_back.dto.queryDTO.BookCopyDTO;
import org.matsuzaka.library_v3_back.model.entity.BookCopy;
import org.matsuzaka.library_v3_back.model.repositoryDao.BookCopyRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.UserRepository;
import org.matsuzaka.library_v3_back.service.BookCopyService;
import org.matsuzaka.library_v3_back.service.LoanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 書籍副本服務實作
 */
@Service
@Transactional
public class BookCopyServiceImpl implements BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    private final LoanService loanService;

    public BookCopyServiceImpl(BookCopyRepository bookCopyRepository, UserRepository userRepository, LoanService loanService) {
        this.bookCopyRepository = bookCopyRepository;
        this.userRepository = userRepository;
        this.loanService = loanService;
    }

    /**
     * 獲取指定書籍的所有副本
     * 包含目前借閱者帳號
     * @param bookId 書籍ID
     * @return 副本列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookCopyDTO> getBookCopies(Long bookId) {
        List<BookCopy> copies = bookCopyRepository.findByBookId(bookId);

        // 批量查詢借閱詳細資訊以提升效能
        List<Long> copyIds = copies.stream()
                .map(BookCopy::getId)
                .collect(Collectors.toList());

        Map<Long, LoanDetails> loanDetailsMap = new HashMap<>();
        if (!copyIds.isEmpty()) {
            List<Object[]> loanResults = bookCopyRepository.findCurrentLoanDetailsByCopyIds(copyIds);
            for (Object[] result : loanResults) {
                Long copyId = (Long) result[0];
                String borrowerAccount = (String) result[1];
                LocalDateTime loanDate = (LocalDateTime) result[2];
                LocalDate dueDate = (LocalDate) result[3];
                LocalDateTime returnDate = (LocalDateTime) result[4];
                Long loanId = (Long) result[5];
                Long userId = (Long) result[6];

                loanDetailsMap.put(copyId, new LoanDetails(borrowerAccount, loanDate, dueDate, returnDate, loanId, userId));
            }
        }

        return copies.stream()
                .map(copy -> {
                    BookCopyDTO dto = BookCopyDTO.fromEntity(copy);

                    // 設定借閱詳細資訊
                    LoanDetails loanDetails = loanDetailsMap.get(copy.getId());
                    if (loanDetails != null) {
                        dto.setCurrentBorrowerAccount(loanDetails.getBorrowerAccount());
                        dto.setLoanDate(loanDetails.getLoanDate());
                        dto.setDueDate(loanDetails.getDueDate());
                        dto.setReturnDate(loanDetails.getReturnDate());
                        dto.setLoanId(loanDetails.getLoanId());
                        dto.setUserId(loanDetails.getUserId());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 內部類別來封裝借閱詳細資訊
    private static class LoanDetails {
        private final String borrowerAccount;
        private final LocalDateTime loanDate;
        private final LocalDate dueDate;
        private final LocalDateTime returnDate;
        private final Long loanId;
        private final Long userId;

        public LoanDetails(String borrowerAccount, LocalDateTime loanDate, LocalDate dueDate, LocalDateTime returnDate, Long loanId, Long userId) {
            this.borrowerAccount = borrowerAccount;
            this.loanDate = loanDate;
            this.dueDate = dueDate;
            this.returnDate = returnDate;
            this.loanId = loanId; // 這裡可以根據需要設定
            this.userId = userId; // 這裡可以根據需要設定
        }

        public String getBorrowerAccount() { return borrowerAccount; }
        public LocalDateTime getLoanDate() { return loanDate; }
        public LocalDate getDueDate() { return dueDate; }
        public LocalDateTime getReturnDate() { return returnDate; }
        public Long getLoanId() { return loanId; }
        public Long getUserId() { return userId; }
    }

    /**
     * 刪除書籍副本
     * @param copyId 副本ID
     */
    @Override
    public void deleteBookCopy(Long copyId) {
        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new RuntimeException("副本不存在"));

        // 檢查副本是否可以刪除（不能是已借出狀態）
        if (copy.getStatus() == BookCopy.BookCopyStatus.L) {
            throw new RuntimeException("無法刪除已借出的副本");
        }

        bookCopyRepository.delete(copy);
    }


    /**
     * 強制歸還副本
     * @param copyId 副本ID
     */
    @Override
    public ReturnResponseDto forceReturn(Long copyId, Long userId) {
        /*BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new RuntimeException("副本不存在"));

        if (copy.getStatus() != BookCopy.BookCopyStatus.L) {
            throw new RuntimeException("副本未處於借出狀態");
        }*/

        // 直接呼叫 LoanService 進行歸還操作
        return loanService.returnBook(copyId, userId);


    }

    /**
     * 強制借出副本
     * @param copyId 副本ID
     * @param borrowerAccount 借閱者帳號
     */
    @Override
    public void forceCheckout(Long copyId, String borrowerAccount) {
        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new RuntimeException("副本不存在"));

        if (copy.getStatus() != BookCopy.BookCopyStatus.A) {
            throw new RuntimeException("副本不可借閱");
        }

        // 直接呼叫 LoanService 進行借閱操作
        Long userId = userRepository.findByAccount(borrowerAccount)
                .orElseThrow(() -> new RuntimeException("借閱者不存在")).getId();
        loanService.borrowBook(copy.getBook().getId(), userId);

    }
}
