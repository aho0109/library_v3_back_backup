package org.matsuzaka.library_v3_back.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.matsuzaka.library_v3_back.model.enums.BookCopyStatus;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "book_copy")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 多個實體副本屬於一本書籍
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "unique_code", nullable = false, unique = true, length = 100)
    private String uniqueCode;

    @Enumerated(EnumType.STRING) // 將 ENUM 映射為字串
    @Column(name = "status", nullable = false, length = 10)
    private BookCopyStatus status; // 使用 Java Enum

    @Column(name = "location")
    private String location = "新書上架區"; // Default location

    @Column(name = "stocked_date", nullable = false)
    private LocalDate stockedDate;

    // 一個實體副本可以有多個借閱記錄
    @OneToMany(mappedBy = "bookCopy", fetch = FetchType.LAZY)
    private List<Loan> loans;

    // 一個實體副本可以有多個預約記錄
    @OneToMany(mappedBy = "bookCopy", fetch = FetchType.LAZY)
    private List<Reservation> reservations;

}
