package org.matsuzaka.library_v3_back.dto.reservationDTO;

import lombok.Data;

@Data
public class ReservationRequestDto {
    private Long bookCopyId; // 直接預約特定的副本
}

