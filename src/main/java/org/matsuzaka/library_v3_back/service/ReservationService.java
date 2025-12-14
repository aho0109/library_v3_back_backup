package org.matsuzaka.library_v3_back.service;

import org.matsuzaka.library_v3_back.dto.reservationDTO.ReservationResponseDto;

import java.util.List;

public interface ReservationService {
    /**
     * 預約特定的書籍副本
     * 只有當副本狀態為 L（已借出）時才能預約
     * 
     * @param userId 使用者ID
     * @param bookCopyId 書籍副本ID
     */
    void reserveBookCopy(Long userId, Long bookCopyId);
    
    /**
     * 取消預約
     * 
     * @param userId 使用者ID
     * @param reservationId 預約記錄ID
     */
    void cancelReservation(Long userId, Long reservationId);
    
    /**
     * 獲取使用者的預約列表
     * 
     * @param userId 使用者ID
     * @return 預約列表
     */
    List<ReservationResponseDto> getUserReservations(Long userId);

    List<ReservationResponseDto> getUserReservationsAdmin(Long userId);

    // 內部方法供 LoanService 使用
    boolean hasReservationsForCopy(Long bookCopyId);
    void handleReturn(Long bookCopyId);
}

