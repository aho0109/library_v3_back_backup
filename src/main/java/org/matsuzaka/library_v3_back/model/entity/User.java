package org.matsuzaka.library_v3_back.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.matsuzaka.library_v3_back.model.enums.Role;
import org.matsuzaka.library_v3_back.model.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user") // 注意：user 可能是 SQL 關鍵字，最好明確指定 table name
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_id", nullable = false, unique = true)
    private String cardId;

    @Column(name = "account", nullable = false, unique = true)
    private String account;

    @Column(name = "password", nullable = false)
    private String password; // 實際應用中應儲存雜湊值

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.ROLE_USER; // Default ROLE_USER

    @Column(name = "penalty_points", nullable = false)
    private Integer penaltyPoints = 0; // Default 0

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.PENDING; // Default PENDING

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil; // 帳號凍結截止時間

    // 一個使用者有多個借閱記錄
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Loan> loans;

    // 一個使用者有多個預約記錄
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    // 一個使用者有多個收藏
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Favorite> favorites;

    // 一個使用者有多個評論
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews;

    // 一個使用者有多個評論按讚記錄
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReviewLike> reviewLikes;

    // 一個使用者有多個通知
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Notification> notifications;

    // 與 UserDetail 的一對一關係
    // mappedBy 指向 UserDetail 中引用 User 的屬性名
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn // 表示 user_id 是共享主鍵
    private UserDetail userDetail;

    // Helper method to link UserDetail
    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
        if (userDetail != null) {
            userDetail.setUser(this);
        }
    }
}