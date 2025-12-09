package org.matsuzaka.library_v3_back.service.Impl;

import jakarta.persistence.EntityNotFoundException;
import org.matsuzaka.library_v3_back.dto.loanDTO.BorrowRespDto;
import org.matsuzaka.library_v3_back.dto.loanDTO.LoanItemRespDto;
import org.matsuzaka.library_v3_back.model.entity.BookCopy;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.entity.Loan;
import org.matsuzaka.library_v3_back.dto.loanDTO.ReturnResponseDto;
import org.matsuzaka.library_v3_back.model.repositoryDao.BookCopyRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.LoanRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.UserRepository;
import org.matsuzaka.library_v3_back.service.LoanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LoanServiceImpl implements LoanService {

    private final BookCopyRepository bookCopyRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository; // 需要 UserRepository 來獲取 User 實體

    public LoanServiceImpl(BookCopyRepository bookCopyRepository,
                           LoanRepository loanRepository,
                           UserRepository userRepository) {
        this.bookCopyRepository = bookCopyRepository;
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Set<LoanItemRespDto> getCurrentByUserId(Long userId) {
        LoanItemRespDto loanItemRespDto = new LoanItemRespDto();


        return loanRepository.findCurrentByUserId(userId);
    }

    @Override
    public List<LoanItemRespDto> getHistoryByUserId(Long userId) {
        return loanRepository.findHistoryByUserId(userId);
    }

    @Override
    public List<LoanItemRespDto> getOverdueByUserId(Long userId) {
        return loanRepository.findOverdueByUserId(userId);
    }

    /*@Override
    public List<LoanItemRespDto> getFavoritesByUserId(Long userId) {
        return loanRepository.findFavoritesByUserId(userId);
    }*/



    /* 借閱功能 */
    @Override
    @Transactional // 確保整個借閱操作是原子性的
    public BorrowRespDto borrowBook(Long bookId, Long userId) {
        // 1. 檢查使用者是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // 2. 查找 ID 最小的可借閱書籍副本
        Optional<BookCopy> availableCopyOptional = bookCopyRepository.findFirstAvailableCopyByBookId(bookId);

        if (availableCopyOptional.isEmpty()) {
            return new BorrowRespDto(false, "目前沒有可借閱的書籍副本。", null, null);
        }

        BookCopy borrowedCopy = availableCopyOptional.get();

        // 3. 更新書籍副本狀態為 'L' (On Loan)
        int updatedRows = bookCopyRepository.updateBookCopyStatusToOnLoan(borrowedCopy.getId());

        if (updatedRows == 0) {
            // 這種情況通常不應該發生，除非在查詢和更新之間有併發修改
            return new BorrowRespDto(false, "更新書籍副本狀態失敗，請重試。", null, null);
        }

        // 4. 創建新的借閱記錄
        Loan newLoan = new Loan();
        newLoan.setUser(user); // 設定借閱者
        newLoan.setBookCopy(borrowedCopy); // 設定借閱的副本
        newLoan.setLoanDate(LocalDateTime.now()); // 設定當前借出時間
        newLoan.setDueDate(LocalDate.now().plusDays(30));// 設定到期日期，假設為 30 天後
        // returnDate 保持為 null，表示尚未歸還
        newLoan.setStatus(Loan.LoanStatus.ON_LOAN); // 設定借閱狀態為 ON_LOAN

        Loan savedLoan = loanRepository.save(newLoan);

        return new BorrowRespDto(true, "書籍借閱成功！", borrowedCopy.getUniqueCode(), savedLoan.getId());
    }

    /* 歸還功能 */
    @Override
    @Transactional // 確保整個歸還操作是原子性的
    public ReturnResponseDto returnBook(Long loanId, Long userId) {
        // 1. 查找借閱記錄
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan record not found with ID: " + loanId));

        // 2. 檢查是否是借閱者本人或管理員歸還 (可選的權限檢查)
        // 這裡假設只有借閱者本人才能歸還。如果需要管理員權限，則需要更複雜的檢查。
        if (!(loan.getUser().getId() == userId)) {
            return new ReturnResponseDto(false, "您無權歸還這本書籍。", null);
        }

        // 3. 檢查書籍是否已經歸還
        if (loan.getReturnDate() != null) {
            return new ReturnResponseDto(false, "這本書籍已經歸還過了。", loan.getBookCopy().getUniqueCode());
        }

        BookCopy bookCopy = loan.getBookCopy();

        // 4. 更新書籍副本狀態為 'A' (Available)
        int updatedRows = bookCopyRepository.updateBookCopyStatusToAvailable(bookCopy.getId());

        if (updatedRows == 0) {
            return new ReturnResponseDto(false, "更新書籍副本狀態失敗，請重試。", bookCopy.getUniqueCode());
        }

        // 5. 更新借閱記錄的歸還日期
        loan.setReturnDate(LocalDateTime.now());
        loan.setStatus(Loan.LoanStatus.RETURNED);
        loanRepository.save(loan); // 保存更新後的借閱記錄

        return new ReturnResponseDto(true, "書籍歸還成功！", bookCopy.getUniqueCode());
    }

}
