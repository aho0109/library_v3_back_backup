package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.model.entity.Reservation;
import org.matsuzaka.library_v3_back.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // Find active reservation for user and book copy
    List<Reservation> findByUserIdAndBookCopyIdAndStatusIn(Long userId, Long bookCopyId, List<ReservationStatus> statuses);

    // Find expired reservations
    List<Reservation> findByStatusAndExpirationDateBefore(ReservationStatus status, LocalDate date);

    // Find user's reservations by status
    List<Reservation> findByUserIdAndStatusIn(Long userId, List<ReservationStatus> statuses);

    // Find user's reservations 管理員用
    List<Reservation> findByUserId(Long userId);

    // Find reservations for a book copy sorted by queue position (for PENDING)
    List<Reservation> findByBookCopyIdAndStatusOrderByQueuePositionAsc(Long bookCopyId, ReservationStatus status);
    
    // Find max queue position for a book copy
    Optional<Reservation> findFirstByBookCopyIdAndStatusOrderByQueuePositionDesc(Long bookCopyId, ReservationStatus status);
}
