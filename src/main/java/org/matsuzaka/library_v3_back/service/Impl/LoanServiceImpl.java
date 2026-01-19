package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.loanDTO.BorrowRespDto;
import org.matsuzaka.library_v3_back.dto.loanDTO.LoanItemRespDto;
import org.matsuzaka.library_v3_back.dto.loanDTO.RenewResponseDto;
import org.matsuzaka.library_v3_back.dto.loanDTO.ReturnResponseDto;
import org.matsuzaka.library_v3_back.exception.BusinessException;
import org.matsuzaka.library_v3_back.exception.ErrorCode;
import org.matsuzaka.library_v3_back.exception.ResourceNotFoundException;
import org.matsuzaka.library_v3_back.model.entity.*;
import org.matsuzaka.library_v3_back.model.enums.*;
import org.matsuzaka.library_v3_back.model.repositoryDao.*;
import org.matsuzaka.library_v3_back.service.LoanService;
import org.matsuzaka.library_v3_back.service.NotificationService;
import org.matsuzaka.library_v3_back.service.ReservationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    private final BookCopyRepository bookCopyRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final NotificationService notificationService;

    public LoanServiceImpl(BookCopyRepository bookCopyRepository,
                           LoanRepository loanRepository,
                           UserRepository userRepository,
                           BookRepository bookRepository,
                           ReservationRepository reservationRepository,
                           @Lazy ReservationService reservationService,
                           NotificationService notificationService) {
        this.bookCopyRepository = bookCopyRepository;
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
    }

    @Override
    public Set<LoanItemRespDto> getCurrentByUserId(Long userId) {
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

    /**
     * 管理員處理借閱。
     * @param uniqueCode 書籍副本唯一碼
     * @param cardId 使用者卡號
     * @return 借閱結果 DTO
     */
    @Override
    public BorrowRespDto borrowBook(String uniqueCode, String cardId) {
        // 1. 驗證使用者存在
        User user = userRepository.findByCardId(cardId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "卡號: " + cardId));
        Long userId = user.getId();

        // 2. 檢查停權狀態
        if (user.getStatus() == UserStatus.SUSPENDED) {
            // 檢查停權是否已過期
            if (user.getSuspendedUntil() != null && user.getSuspendedUntil().isAfter(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.USER_SUSPENDED,
                    "停權至: " + user.getSuspendedUntil());
            } else {
                // 解除停權
                user.setStatus(UserStatus.ACTIVE);
                user.setSuspendedUntil(null);
                userRepository.save(user);
            }
        }

        // 3. 檢查帳號啟用狀態
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_NOT_ACTIVATED);
        }

        // 4. 檢查借閱額度（民眾 5，市民和管理員 10）
        int limit = user.getRole() == Role.ROLE_CITIZEN ? 10 : 5;
        long currentLoans = loanRepository.countByUserIdAndStatus(userId, LoanStatus.ON_LOAN);
        if (currentLoans >= limit) {
            throw new BusinessException(ErrorCode.BORROW_LIMIT_EXCEEDED);
        }

        // 5. 驗證書籍副本存在
        BookCopy copy = bookCopyRepository.findByUniqueCode(uniqueCode)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_COPY_NOT_FOUND,
                    "副本編號: " + uniqueCode));

        // 6. 檢查副本狀態
        if (copy.getStatus() == BookCopyStatus.L) {
            throw new BusinessException(ErrorCode.BOOK_ALREADY_BORROWED);
        }

        Reservation matchedReservation = null;

        if (copy.getStatus() == BookCopyStatus.R) {
            // 檢查是否為此使用者的預約且狀態為 AVAILABLE
            List<Reservation> reservations = reservationRepository.findByUserIdAndBookCopyIdAndStatusIn(
                    userId, copy.getId(), List.of(ReservationStatus.AVAILABLE));
            if (reservations.isEmpty()) {
                throw new BusinessException(ErrorCode.BOOK_RESERVED_BY_OTHERS);
            }
            matchedReservation = reservations.get(0);
        } else if (copy.getStatus() == BookCopyStatus.A) {
            // 可借閱狀態，允許現場借閱
            // 這裏啥都不執行，等同結束（完成）這段 if 判斷，接下去跑借閱流程
        } else {
            throw new BusinessException(ErrorCode.BOOK_COPY_UNAVAILABLE,
                "狀態: " + copy.getStatus());
        }

        // 7. 建立借閱記錄
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBookCopy(copy);
        loan.setLoanDate(LocalDateTime.now());
        loan.setDueDate(LocalDate.now().plusDays(30));
        loan.setStatus(LoanStatus.ON_LOAN);
        loan.setRenewCount(0);

        Loan savedLoan = loanRepository.save(loan);

        // 8. 更新副本狀態為已借出
        copy.setStatus(BookCopyStatus.L);
        bookCopyRepository.save(copy);

        // 9. 增加書籍的累計借閱次數
        Book book = copy.getBook();
        book.setTotalLoanCount(book.getTotalLoanCount() + 1);
        bookRepository.save(book);

        // 10. 更新預約記錄（如果存在）
        if (matchedReservation != null) {
            matchedReservation.setStatus(ReservationStatus.PICKED_UP);
            matchedReservation.setPickupDate(LocalDateTime.now());
            reservationRepository.save(matchedReservation);
        }

        // 11. 成功時只返回成功的結果
        return new BorrowRespDto(true, "借閱成功", uniqueCode, savedLoan.getId(), copy.getBook().getTitle());
    }

    @Override
    public ReturnResponseDto returnBook(String uniqueCode) {
        // 1. 驗證書籍副本存在
        BookCopy copy = bookCopyRepository.findByUniqueCode(uniqueCode)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_COPY_NOT_FOUND, "副本編號: " + uniqueCode));

        // 2. 查找此副本的活躍借閱記錄
        Optional<Loan> loanOpt = loanRepository.findByBookCopyIdAndStatus(copy.getId(), LoanStatus.ON_LOAN);

        if (loanOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.BOOK_NOT_BORROWED);
        }

        Loan loan = loanOpt.get();
        User user = loan.getUser();

        // 3. 檢查是否逾期並處理罰分
        long overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
        if (overdueDays > 0) {
            int points = (int) overdueDays; // 一天一點
            user.setPenaltyPoints(user.getPenaltyPoints() + points);

            if (user.getPenaltyPoints() >= 10) {
                user.setPenaltyPoints(0);
                user.setStatus(UserStatus.SUSPENDED);

                LocalDateTime baseTime = (user.getSuspendedUntil() != null && user.getSuspendedUntil().isAfter(LocalDateTime.now()))
                        ? user.getSuspendedUntil()
                        : LocalDateTime.now();
                user.setSuspendedUntil(baseTime.plusDays(30));

                notificationService.sendNotification(user, NotificationType.PENALTY, "帳號停權通知",
                        "您因累積違規點數達 10 點，帳號將停權 30 天至 " + user.getSuspendedUntil(),
                        loan.getId(), null, ReferenceType.PENALTY);
            } else {
                 notificationService.sendNotification(user, NotificationType.PENALTY, "逾期違規通知",
                        "您本次逾期產生 " + points + " 點違規點數，目前累積點數：" + user.getPenaltyPoints(),
                        loan.getId(), null, ReferenceType.PENALTY);
            }
            userRepository.save(user);
        }

        // 4. 結束借閱記錄
        loan.setReturnDate(LocalDateTime.now());
        loan.setStatus(LoanStatus.RETURNED);
        loanRepository.save(loan);

        // 5. 處理預約佇列（副本狀態更新邏輯在內部）
        reservationService.handleReturn(copy.getId());

        // 6. 計算歸還後的借閱數量
        long currentLoanCount = loanRepository.countByUserIdAndStatus(user.getId(), LoanStatus.ON_LOAN);
        int maxLoanCount = user.getRole() == Role.ROLE_CITIZEN ? 10 : 5;

        // 7. 構建完整的響應（成功）
        ReturnResponseDto response = new ReturnResponseDto();
        response.setSuccess(true);
        response.setMessage("歸還成功");
        response.setReturnedBookUniqueCode(uniqueCode);
        response.setBorrowerCardId(user.getCardId());
        response.setBorrowerName(user.getUserDetail() != null ? user.getUserDetail().getName() : "未知");
        response.setBorrowerRole(user.getRole().name());
        response.setBorrowerStatus(user.getStatus().name());
        response.setBorrowerPenaltyPoints(user.getPenaltyPoints());
        response.setCurrentLoanCount((int) currentLoanCount);
        response.setMaxLoanCount(maxLoanCount);
        response.setBookTitle(copy.getBook().getTitle());
        response.setLoanDate(loan.getLoanDate());
        response.setDueDate(loan.getDueDate());

        return response;
    }

    /**
     * 續借書籍。
     * @param loanId
     * @param userId
     */
    @Override
    public RenewResponseDto renewBook(Long loanId, Long userId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.LOAN_NOT_FOUND, "借閱記錄ID: " + loanId));

        if (!loan.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_LOAN_OPERATION);
        }
        
        // 檢查使用者狀態
        User user = loan.getUser();

        // 檢查停權狀態（與借書邏輯一致）
        if (user.getStatus() == UserStatus.SUSPENDED) {
            // 檢查停權是否已過期
            if (user.getSuspendedUntil() != null && user.getSuspendedUntil().isAfter(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.USER_SUSPENDED,
                    "停權至: " + user.getSuspendedUntil());
            } else {
                // 解除停權
                user.setStatus(UserStatus.ACTIVE);
                user.setSuspendedUntil(null);
                userRepository.save(user);
            }
        }

        // 檢查帳號啟用狀態
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_NOT_ACTIVATED);
        }

        if (loan.getStatus() != LoanStatus.ON_LOAN) {
            throw new BusinessException(ErrorCode.NOT_ON_LOAN_STATUS);
        }
        
        if (loan.getRenewCount() >= 2) {
            throw new BusinessException(ErrorCode.RENEW_LIMIT_EXCEEDED);
        }
        
        // 檢查是否有人預約
        if (reservationService.hasReservationsForCopy(loan.getBookCopy().getId())) {
             throw new BusinessException(ErrorCode.BOOK_RESERVED_CANNOT_RENEW);
        }
        
        // 檢查續借視窗是否開啟（到期日前3天才可續借）
        // 例如：12/31 到期 -> 12/28 開放續借
        LocalDate renewStart = loan.getDueDate().minusDays(3);
        if (LocalDate.now().isBefore(renewStart)) {
             throw new BusinessException(ErrorCode.RENEW_WINDOW_NOT_OPEN, "續借功能僅在到期日前 3 天開放");
        }
        
        // 續借：到期日延後 10 天
        loan.setDueDate(loan.getDueDate().plusDays(10));
        loan.setRenewCount(loan.getRenewCount() + 1);
        loanRepository.save(loan);

        // 回傳最新到期日期
        return new RenewResponseDto(true, "續借成功", loan.getDueDate());
    }
}
