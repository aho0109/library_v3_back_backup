package org.matsuzaka.library_v3_back.service;

import org.matsuzaka.library_v3_back.dto.reservationDTO.ReservationResponseDto;

import java.util.List;

public interface ReservationService {
    void reserveBook(Long userId, Long bookId);
    void cancelReservation(Long userId, Long reservationId);
    List<ReservationResponseDto> getUserReservations(Long userId);
    
    // Internal use for LoanService
    boolean hasReservationsForCopy(Long bookCopyId);
    void handleReturn(Long bookCopyId); // Check queue and assign next
}

