package org.matsuzaka.library_v3_back.service.Impl;

import jakarta.persistence.EntityNotFoundException;
import org.matsuzaka.library_v3_back.dto.loanDTO.BorrowRespDto;
import org.matsuzaka.library_v3_back.dto.loanDTO.LoanItemRespDto;
import org.matsuzaka.library_v3_back.dto.loanDTO.ReturnResponseDto;
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
        User user = userRepository.findByCardId(cardId)
                .orElseThrow(() -> new EntityNotFoundException("找不到使用者"));
        Long userId = user.getId();

        if (user.getStatus() == UserStatus.SUSPENDED) {
            // 檢查停權是否已過期
            if (user.getSuspendedUntil() != null && user.getSuspendedUntil().isAfter(LocalDateTime.now())) {
                return new BorrowRespDto(false, "使用者帳號已停權至 " + user.getSuspendedUntil(), null, null);
            } else {
                // 解除停權
                user.setStatus(UserStatus.ACTIVE);
                user.setSuspendedUntil(null);
                userRepository.save(user);
            }
        }
        
        if (user.getStatus() != UserStatus.ACTIVE) {
             return new BorrowRespDto(false, "使用者帳號未啟用", null, null);
        }

        // 檢查借閱額度：一般民眾 5 本，市民 10 本
        int limit = user.getRole() == Role.ROLE_CITIZEN ? 10 : 5;
        long currentLoans = loanRepository.countByUserIdAndStatus(userId, LoanStatus.ON_LOAN);
        if (currentLoans >= limit) {
            return new BorrowRespDto(false, "借閱數量已達上限", null, null);
        }

        BookCopy copy = bookCopyRepository.findByUniqueCode(uniqueCode)
                .orElseThrow(() -> new EntityNotFoundException("找不到書籍副本"));

        // 檢查副本狀態
        if (copy.getStatus() == BookCopyStatus.L) {
            return new BorrowRespDto(false, "此書已被借出", null, null);
        }

        Reservation matchedReservation = null;

        if (copy.getStatus() == BookCopyStatus.R) {
            // 檢查是否為此使用者的預約且狀態為 AVAILABLE
            List<Reservation> reservations = reservationRepository.findByUserIdAndBookCopyIdAndStatusIn(
                    userId, copy.getId(), List.of(ReservationStatus.AVAILABLE));
            if (reservations.isEmpty()) {
                return new BorrowRespDto(false, "此書已被其他使用者預約", null, null);
            }
            matchedReservation = reservations.get(0);
        } else if (copy.getStatus() == BookCopyStatus.A) {
             // 可借閱狀態，允許現場借閱
        } else {
             return new BorrowRespDto(false, "此書目前無法借閱 (狀態: " + copy.getStatus() + ")", null, null);
        }

        // 建立借閱記錄
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBookCopy(copy);
        loan.setLoanDate(LocalDateTime.now());
        loan.setDueDate(LocalDate.now().plusDays(30));
        loan.setStatus(LoanStatus.ON_LOAN);
        loan.setRenewCount(0);
        
        Loan savedLoan = loanRepository.save(loan);

        // 更新副本狀態為已借出
        copy.setStatus(BookCopyStatus.L);
        bookCopyRepository.save(copy);
        
        // 增加書籍的累計借閱次數
        Book book = copy.getBook();
        book.setTotalLoanCount(book.getTotalLoanCount() + 1);
        bookRepository.save(book);
        
        // 更新預約記錄（如果存在）
        if (matchedReservation != null) {
            matchedReservation.setStatus(ReservationStatus.PICKED_UP);
            matchedReservation.setPickupDate(LocalDateTime.now());
            reservationRepository.save(matchedReservation);
        }

        return new BorrowRespDto(true, "借閱成功", uniqueCode, savedLoan.getId());
    }

    @Override
    public ReturnResponseDto returnBook(String uniqueCode) {
        // 根據唯一碼找到副本
        BookCopy copy = bookCopyRepository.findByUniqueCode(uniqueCode)
                .orElseThrow(() -> new EntityNotFoundException("找不到書籍副本"));
        
        // 找到此副本的活躍借閱記錄
        Optional<Loan> loanOpt = loanRepository.findByBookCopyIdAndStatus(copy.getId(), LoanStatus.ON_LOAN);
        
        if (loanOpt.isEmpty()) {
             return new ReturnResponseDto(false, "此書無借出記錄", uniqueCode);
        }
        
        Loan loan = loanOpt.get();
        User user = loan.getUser();

        // 檢查是否逾期
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

        // 結束借閱記錄
        loan.setReturnDate(LocalDateTime.now());
        loan.setStatus(LoanStatus.RETURNED);
        loanRepository.save(loan);

        // 處理預約佇列（副本狀態更新邏輯在內部）
        reservationService.handleReturn(copy.getId());

        return new ReturnResponseDto(true, "歸還成功", uniqueCode);
    }

    @Override
    public void renewBook(Long loanId, Long userId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("找不到借閱記錄"));
        
        if (!loan.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("無權限執行此操作");
        }
        
        if (loan.getStatus() != LoanStatus.ON_LOAN) {
            throw new IllegalStateException("非借閱中狀態，無法續借");
        }
        
        if (loan.getRenewCount() >= 2) {
            throw new IllegalStateException("續借次數已達上限");
        }
        
        // 檢查是否有人預約
        if (reservationService.hasReservationsForCopy(loan.getBookCopy().getId())) {
             throw new IllegalStateException("此書已被預約，無法續借");
        }
        
        // 檢查續借視窗是否開啟（到期日前3天才可續借）
        // 例如：12/31 到期 -> 12/28 開放續借
        LocalDate renewStart = loan.getDueDate().minusDays(3);
        if (LocalDate.now().isBefore(renewStart)) {
             throw new IllegalStateException("續借功能僅在到期日前 3 天開放");
        }
        
        // 續借：到期日延後 10 天
        loan.setDueDate(loan.getDueDate().plusDays(10));
        loan.setRenewCount(loan.getRenewCount() + 1);
        loanRepository.save(loan);
    }
}
