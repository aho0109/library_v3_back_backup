package org.matsuzaka.library_v3_back.Scheduler;

import jakarta.annotation.PostConstruct;
import org.matsuzaka.library_v3_back.model.entity.Loan;
import org.matsuzaka.library_v3_back.model.repositoryDao.LoanRepository;
import org.matsuzaka.library_v3_back.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OverdueCheckScheduler {

    private final LoanRepository loanRepository;
    private final UserService userService; // 注入 UserService 來處理用戶凍結

    public OverdueCheckScheduler(LoanRepository loanRepository, UserService userService) {
        this.loanRepository = loanRepository;
        this.userService = userService;
    }

    // 每天凌晨 0 點執行
    @Scheduled(cron = "0 0 0 * * ?")
    @PostConstruct // 應用啟動時就也會執行一次
    // 或者 for testing: @Scheduled(fixedRate = 30000) // 每 30 秒執行一次 (測試用)
    public void checkAndHandleOverdueLoans() {
        System.out.println("Running overdue loan check at " + LocalDateTime.now());

        // 找到所有 ON_LOAN 狀態，且dueDate早於今天的借閱記錄
        // 注意：這裡使用 LocalDate.now() 會是當天的日期，時間部分為 00:00:00
        List<Loan> overdueLoans = loanRepository.findByStatusAndDueDateBefore(Loan.LoanStatus.ON_LOAN, LocalDate.now());

        for (Loan loan : overdueLoans) {
            // 更新借閱狀態為 OVERDUE
            loan.setStatus(Loan.LoanStatus.OVERDUE);
            loanRepository.save(loan); // 保存更新

            /*// 凍結相關使用者
            userService.suspendUserForTest(loan.getUser().getId()); // 假設 Loan 實體有指向 User 的關聯
            System.out.println("Loan " + loan.getId() + " is overdue. User " + loan.getUser().getId() + " is suspended.");*/
        }
    }
}
