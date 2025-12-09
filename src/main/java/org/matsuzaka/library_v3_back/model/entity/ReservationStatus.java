package org.matsuzaka.library_v3_back.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "reservation_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Byte id; // TINYINT 對應 Byte

    @Column(name = "title", nullable = false, unique = true, length = 50)
    private String title;

    // 一個狀態可以有多個預約記錄
    @OneToMany(mappedBy = "reservationStatus", fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    // 建議：也可以直接用 Java Enum 而非單獨的表來管理這種固定狀態
    // 但如果未來狀態會動態增加或有其他屬性，用表更好
}