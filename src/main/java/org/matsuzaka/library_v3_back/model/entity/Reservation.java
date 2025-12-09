package org.matsuzaka.library_v3_back.model.entity;

import org.matsuzaka.library_v3_back.model.enums.ReservationStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 多個預約記錄屬於一個使用者
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 多個預約記錄屬於一個實體書副本
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_copy_id", nullable = false)
    private BookCopy bookCopy;

    @Column(name = "queue_position", nullable = false)
    private Integer queuePosition;

    @CreationTimestamp
    @Column(name = "reserve_date", updatable = false)
    private LocalDateTime reserveDate;

    @Column(name = "notify_date")
    private LocalDateTime notifyDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "pickup_date")
    private LocalDateTime pickupDate; // 可為 NULL

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;
}
