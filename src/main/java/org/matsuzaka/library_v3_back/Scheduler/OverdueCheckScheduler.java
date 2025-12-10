package org.matsuzaka.library_v3_back.Scheduler;

import org.matsuzaka.library_v3_back.model.entity.Loan;
import org.matsuzaka.library_v3_back.model.entity.Reservation;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.enums.*;
import org.matsuzaka.library_v3_back.model.repositoryDao.LoanRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.ReservationRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.UserRepository;
import org.matsuzaka.library_v3_back.service.NotificationService;
import org.matsuzaka.library_v3_back.service.ReservationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OverdueCheckScheduler {

    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ReservationService reservationService;
    private final NotificationService notificationService;

    public OverdueCheckScheduler(LoanRepository loanRepository,
                                 ReservationRepository reservationRepository,
                                 UserRepository userRepository,
                                 ReservationService reservationService,
                                 NotificationService notificationService) {
        this.loanRepository = loanRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
    }

    // 每天凌晨 0 點執行
    @Scheduled(cron = "0 0 0 * * ?")
    // @PostConstruct // 測試時可取消註解，正式環境請註解
    @Transactional
    public void dailyCheck() {
        checkOverdueLoans();
        checkExpiredReservations();
    }

    private void checkOverdueLoans() {
        System.out.println("執行逾期檢查：" + LocalDateTime.now());
        
        // 找出所有未歸還的借閱記錄
        List<Loan> overdueLoans = loanRepository.findByReturnDateIsNull();
        LocalDate today = LocalDate.now();

        for (Loan loan : overdueLoans) {
            if (loan.getDueDate().isBefore(today)) {
                // 逾期：每日累計違規點數
                User user = loan.getUser();
                
                // 增加違規點數（一天一點）
                user.setPenaltyPoints(user.getPenaltyPoints() + 1);
                
                // 檢查是否達停權標準
                if (user.getPenaltyPoints() >= 10) {
                    user.setPenaltyPoints(0); // 歸零點數
                    user.setStatus(UserStatus.SUSPENDED);
                    
                    LocalDateTime baseTime = (user.getSuspendedUntil() != null && user.getSuspendedUntil().isAfter(LocalDateTime.now())) 
                            ? user.getSuspendedUntil() 
                            : LocalDateTime.now();
                    user.setSuspendedUntil(baseTime.plusDays(30));
                    
                    notificationService.sendNotification(user, NotificationType.PENALTY, "帳號停權通知", 
                            "您因書籍《" + loan.getBookCopy().getBook().getTitle() + "》逾期，累積違規點數達 10 點，帳號停權 30 天。", 
                            loan.getId(), null, ReferenceType.PENALTY);
                } else {
                    notificationService.sendNotification(user, NotificationType.LOAN_DUE, "逾期提醒", 
                            "書籍《" + loan.getBookCopy().getBook().getTitle() + "》逾期，新增 1 點違規點數。目前累積：" + user.getPenaltyPoints() + " 點", 
                            loan.getId(), null, ReferenceType.LOAN);
                }
                
                userRepository.save(user);
                
                // 更新借閱狀態為逾期
                if (loan.getStatus() != LoanStatus.OVERDUE) {
                    loan.setStatus(LoanStatus.OVERDUE);
                    loanRepository.save(loan);
                }
            }
        }
    }

    private void checkExpiredReservations() {
        System.out.println("執行預約過期檢查");
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndExpirationDateBefore(ReservationStatus.AVAILABLE, LocalDate.now());
        
        for (Reservation r : expiredReservations) {
            r.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(r);
            
            notificationService.sendNotification(r.getUser(), NotificationType.RESERVE_EXPIRING, "【預約過期】",
                    "您預約的《" + r.getBookCopy().getBook().getTitle() + "》未於期限內取書，已取消。",
                    r.getId(), null, ReferenceType.RESERVATION);
            
            // 遞補給下一位預約者
            reservationService.handleReturn(r.getBookCopy().getId());
        }
    }
}

