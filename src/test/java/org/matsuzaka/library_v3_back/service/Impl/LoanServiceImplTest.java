package org.matsuzaka.library_v3_back.service.Impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matsuzaka.library_v3_back.dto.loanDTO.BorrowRespDto;
import org.matsuzaka.library_v3_back.dto.loanDTO.RenewResponseDto;
import org.matsuzaka.library_v3_back.dto.loanDTO.ReturnResponseDto;
import org.matsuzaka.library_v3_back.model.entity.*;
import org.matsuzaka.library_v3_back.model.enums.*;
import org.matsuzaka.library_v3_back.model.repositoryDao.*;
import org.matsuzaka.library_v3_back.service.NotificationService;
import org.matsuzaka.library_v3_back.service.ReservationService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private BookCopyRepository bookCopyRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationService reservationService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LoanServiceImpl loanService;

    private User mockUser;
    private Book mockBook;
    private BookCopy mockCopy;
    private Loan mockLoan;

    @BeforeEach
    void setUp() {
        // 初始化共用的 Mock 物件
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setCardId("CARD-001");
        mockUser.setStatus(UserStatus.ACTIVE);
        mockUser.setRole(Role.ROLE_USER);
        mockUser.setPenaltyPoints(0);
        UserDetail userDetail = new UserDetail();
        userDetail.setName("Test User");
        mockUser.setUserDetail(userDetail);

        mockBook = new Book();
        mockBook.setId(10L);
        mockBook.setTitle("測試書籍");
        mockBook.setTotalLoanCount(0);

        mockCopy = new BookCopy();
        mockCopy.setId(100L);
        mockCopy.setUniqueCode("CODE-123");
        mockCopy.setStatus(BookCopyStatus.A);
        mockCopy.setBook(mockBook);

        mockLoan = new Loan();
        mockLoan.setId(500L);
        mockLoan.setUser(mockUser);
        mockLoan.setBookCopy(mockCopy);
        mockLoan.setLoanDate(LocalDateTime.now().minusDays(10));
        mockLoan.setDueDate(LocalDate.now().plusDays(20));
        mockLoan.setStatus(LoanStatus.ON_LOAN);
        mockLoan.setRenewCount(0);
    }

    // --- 借書測試 (borrowBook) ---

    @Test
    void borrowBook_Success() {
        when(userRepository.findByCardId("CARD-001")).thenReturn(Optional.of(mockUser));
        when(loanRepository.countByUserIdAndStatus(1L, LoanStatus.ON_LOAN)).thenReturn(0L);
        when(bookCopyRepository.findByUniqueCode("CODE-123")).thenReturn(Optional.of(mockCopy));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            loan.setId(999L); // Simulate DB ID generation
            return loan;
        });

        BorrowRespDto response = loanService.borrowBook("CODE-123", "CARD-001");

        assertTrue(response.isSuccess());
        assertEquals("借閱成功", response.getMessage());
//        assertEquals("CODE-123", response.getUniqueCode());
        verify(loanRepository).save(any(Loan.class));
        verify(bookCopyRepository).save(mockCopy);
        assertEquals(BookCopyStatus.L, mockCopy.getStatus());
    }

    @Test
    void borrowBook_Fail_UserNotFound() {
        when(userRepository.findByCardId("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> 
            loanService.borrowBook("CODE-123", "UNKNOWN")
        );
    }

    @Test
    void borrowBook_Fail_UserSuspended() {
        mockUser.setStatus(UserStatus.SUSPENDED);
        mockUser.setSuspendedUntil(LocalDateTime.now().plusDays(10));
        when(userRepository.findByCardId("CARD-001")).thenReturn(Optional.of(mockUser));

        BorrowRespDto response = loanService.borrowBook("CODE-123", "CARD-001");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("已停權"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrowBook_Success_SuspensionExpired() {
        // 停權但已過期 -> 應自動復權並借書成功
        mockUser.setStatus(UserStatus.SUSPENDED);
        mockUser.setSuspendedUntil(LocalDateTime.now().minusDays(1)); // 昨天到期
        
        when(userRepository.findByCardId("CARD-001")).thenReturn(Optional.of(mockUser));
        when(loanRepository.countByUserIdAndStatus(1L, LoanStatus.ON_LOAN)).thenReturn(0L);
        when(bookCopyRepository.findByUniqueCode("CODE-123")).thenReturn(Optional.of(mockCopy));
        when(loanRepository.save(any(Loan.class))).thenReturn(mockLoan);

        BorrowRespDto response = loanService.borrowBook("CODE-123", "CARD-001");

        assertTrue(response.isSuccess());
        assertEquals(UserStatus.ACTIVE, mockUser.getStatus()); // 驗證狀態已更新
        assertNull(mockUser.getSuspendedUntil());
        verify(userRepository).save(mockUser); // 驗證有儲存使用者狀態
    }

    @Test
    void borrowBook_Fail_LimitReached() {
        mockUser.setRole(Role.ROLE_USER); // Limit 5
        when(userRepository.findByCardId("CARD-001")).thenReturn(Optional.of(mockUser));
        when(loanRepository.countByUserIdAndStatus(1L, LoanStatus.ON_LOAN)).thenReturn(5L);

        BorrowRespDto response = loanService.borrowBook("CODE-123", "CARD-001");

        assertFalse(response.isSuccess());
        assertEquals("借閱數量已達上限", response.getMessage());
    }

    @Test
    void borrowBook_Fail_CopyNotFound() {
        when(userRepository.findByCardId("CARD-001")).thenReturn(Optional.of(mockUser));
        when(loanRepository.countByUserIdAndStatus(1L, LoanStatus.ON_LOAN)).thenReturn(0L);
        when(bookCopyRepository.findByUniqueCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> 
            loanService.borrowBook("UNKNOWN", "CARD-001")
        );
    }

    @Test
    void borrowBook_Fail_CopyOnLoan() {
        mockCopy.setStatus(BookCopyStatus.L);
        when(userRepository.findByCardId("CARD-001")).thenReturn(Optional.of(mockUser));
        when(loanRepository.countByUserIdAndStatus(1L, LoanStatus.ON_LOAN)).thenReturn(0L);
        when(bookCopyRepository.findByUniqueCode("CODE-123")).thenReturn(Optional.of(mockCopy));

        BorrowRespDto response = loanService.borrowBook("CODE-123", "CARD-001");

        assertFalse(response.isSuccess());
        assertEquals("此書已被借出", response.getMessage());
    }

    @Test
    void borrowBook_Fail_ReservedByOther() {
        mockCopy.setStatus(BookCopyStatus.R);
        when(userRepository.findByCardId("CARD-001")).thenReturn(Optional.of(mockUser));
        when(loanRepository.countByUserIdAndStatus(1L, LoanStatus.ON_LOAN)).thenReturn(0L);
        when(bookCopyRepository.findByUniqueCode("CODE-123")).thenReturn(Optional.of(mockCopy));
        // 模擬找不到此使用者的預約
        when(reservationRepository.findByUserIdAndBookCopyIdAndStatusIn(anyLong(), anyLong(), anyList()))
                .thenReturn(Collections.emptyList());

        BorrowRespDto response = loanService.borrowBook("CODE-123", "CARD-001");

        assertFalse(response.isSuccess());
        assertEquals("此書已被其他使用者預約", response.getMessage());
    }

    @Test
    void borrowBook_Success_ReservedBySelf() {
        mockCopy.setStatus(BookCopyStatus.R);
        Reservation mockReservation = new Reservation();
        mockReservation.setId(88L);
        mockReservation.setStatus(ReservationStatus.AVAILABLE);

        when(userRepository.findByCardId("CARD-001")).thenReturn(Optional.of(mockUser));
        when(loanRepository.countByUserIdAndStatus(1L, LoanStatus.ON_LOAN)).thenReturn(0L);
        when(bookCopyRepository.findByUniqueCode("CODE-123")).thenReturn(Optional.of(mockCopy));
        // 模擬找到此使用者的預約
        when(reservationRepository.findByUserIdAndBookCopyIdAndStatusIn(eq(1L), eq(100L), anyList()))
                .thenReturn(List.of(mockReservation));
        when(loanRepository.save(any(Loan.class))).thenReturn(mockLoan);

        BorrowRespDto response = loanService.borrowBook("CODE-123", "CARD-001");

        assertTrue(response.isSuccess());
        assertEquals(ReservationStatus.PICKED_UP, mockReservation.getStatus()); // 驗證預約狀態更新
        verify(reservationRepository).save(mockReservation);
    }

    // --- 還書測試 (returnBook) ---

    @Test
    void returnBook_Success_NoOverdue() {
        when(bookCopyRepository.findByUniqueCode("CODE-123")).thenReturn(Optional.of(mockCopy));
        when(loanRepository.findByBookCopyIdAndStatus(100L, LoanStatus.ON_LOAN)).thenReturn(Optional.of(mockLoan));
        when(loanRepository.countByUserIdAndStatus(1L, LoanStatus.ON_LOAN)).thenReturn(0L);

        ReturnResponseDto response = loanService.returnBook("CODE-123");

        assertTrue(response.isSuccess());
        assertEquals("歸還成功", response.getMessage());
        assertEquals(LoanStatus.RETURNED, mockLoan.getStatus());
        assertNotNull(mockLoan.getReturnDate());
        verify(reservationService).handleReturn(100L); // 驗證有呼叫預約處理
    }

    @Test
    void returnBook_Success_WithOverdue() {
        // 設定逾期 5 天
        mockLoan.setDueDate(LocalDate.now().minusDays(5));
        
        when(bookCopyRepository.findByUniqueCode("CODE-123")).thenReturn(Optional.of(mockCopy));
        when(loanRepository.findByBookCopyIdAndStatus(100L, LoanStatus.ON_LOAN)).thenReturn(Optional.of(mockLoan));
        when(loanRepository.countByUserIdAndStatus(1L, LoanStatus.ON_LOAN)).thenReturn(0L);

        ReturnResponseDto response = loanService.returnBook("CODE-123");

        assertTrue(response.isSuccess());
        assertEquals(5, mockUser.getPenaltyPoints()); // 驗證罰點增加
        verify(notificationService).sendNotification(eq(mockUser), eq(NotificationType.PENALTY), anyString(), anyString(), anyLong(), any(), any());
    }

    @Test
    void returnBook_Success_OverdueCausesSuspension() {
        // 設定逾期 10 天 (達到停權門檻)
        mockLoan.setDueDate(LocalDate.now().minusDays(10));
        
        when(bookCopyRepository.findByUniqueCode("CODE-123")).thenReturn(Optional.of(mockCopy));
        when(loanRepository.findByBookCopyIdAndStatus(100L, LoanStatus.ON_LOAN)).thenReturn(Optional.of(mockLoan));

        loanService.returnBook("CODE-123");

        assertEquals(0, mockUser.getPenaltyPoints()); // 罰點歸零
        assertEquals(UserStatus.SUSPENDED, mockUser.getStatus()); // 狀態變為停權
        assertNotNull(mockUser.getSuspendedUntil()); // 設定停權期限
    }

    @Test
    void returnBook_Fail_NoActiveLoan() {
        when(bookCopyRepository.findByUniqueCode("CODE-123")).thenReturn(Optional.of(mockCopy));
        when(loanRepository.findByBookCopyIdAndStatus(100L, LoanStatus.ON_LOAN)).thenReturn(Optional.empty());

        ReturnResponseDto response = loanService.returnBook("CODE-123");

        assertFalse(response.isSuccess());
        assertEquals("此書無借出記錄", response.getMessage());
    }

    // --- 續借測試 (renewBook) ---

    @Test
    void renewBook_Success() {
        // 設定符合續借條件：本人、借閱中、未達上限、無人預約、在續借期內
        mockLoan.setDueDate(LocalDate.now().plusDays(1)); // 明天到期 (在 3 天內)
        
        when(loanRepository.findById(500L)).thenReturn(Optional.of(mockLoan));
        when(reservationService.hasReservationsForCopy(100L)).thenReturn(false);

        RenewResponseDto response = loanService.renewBook(500L, 1L);

        assertTrue(response.isSuccess());
        assertEquals(1, mockLoan.getRenewCount());
        // 驗證到期日延後 (原 DueDate + 10 天)
        // 注意：這裡邏輯是 loan.getDueDate().plusDays(10)，所以是 明天+10天
        assertEquals(LocalDate.now().plusDays(11), mockLoan.getDueDate());
    }

    @Test
    void renewBook_Fail_NotOwner() {
        when(loanRepository.findById(500L)).thenReturn(Optional.of(mockLoan));
        
        assertThrows(IllegalArgumentException.class, () -> 
            loanService.renewBook(500L, 999L) // 傳入錯誤的 userId
        );
    }

    @Test
    void renewBook_Fail_MaxRenewReached() {
        mockLoan.setRenewCount(2);
        when(loanRepository.findById(500L)).thenReturn(Optional.of(mockLoan));
        
        assertThrows(IllegalStateException.class, () -> 
            loanService.renewBook(500L, 1L)
        );
    }

    @Test
    void renewBook_Fail_HasReservations() {
        when(loanRepository.findById(500L)).thenReturn(Optional.of(mockLoan));
        when(reservationService.hasReservationsForCopy(100L)).thenReturn(true); // 有人預約
        
        assertThrows(IllegalStateException.class, () -> 
            loanService.renewBook(500L, 1L)
        );
    }

    @Test
    void renewBook_Fail_TooEarly() {
        mockLoan.setDueDate(LocalDate.now().plusDays(10)); // 10 天後到期 (不在 3 天內)
        when(loanRepository.findById(500L)).thenReturn(Optional.of(mockLoan));
        when(reservationService.hasReservationsForCopy(100L)).thenReturn(false);
        
        assertThrows(IllegalStateException.class, () -> 
            loanService.renewBook(500L, 1L)
        );
    }
}
