package org.matsuzaka.library_v3_back.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 多個借閱記錄屬於一個使用者
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 多個借閱記錄屬於一個實體書副本
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_copy_id", nullable = false) // 欄位名與資料庫一致
    private BookCopy bookCopy; // 屬性名為 bookCopy

    @CreationTimestamp
    @Column(name = "loan_date", updatable = false)
    private LocalDateTime loanDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate; // 只需日期

    @Column(name = "return_date")
    private LocalDateTime returnDate; // 可為 NULL

    @Enumerated(EnumType.STRING) // 將 ENUM 映射為字串
    @Column(name = "status", nullable = false, length = 10)
    private LoanStatus status; // 使用 Java Enum

    // 定義一個 Java Enum 來對應 ENUM 類型
    public enum LoanStatus {
        ON_LOAN,
        RETURNED,
        OVERDUE
    }
}
