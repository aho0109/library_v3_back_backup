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
    
    // 依據 userId、bookCopyId 和多個 status 找出預約紀錄
    List<Reservation> findByUserIdAndBookCopyIdAndStatusIn(Long userId, Long bookCopyId, List<ReservationStatus> statuses);

    // 依據 status 和 expirationDate 找出過期的預約紀錄
    List<Reservation> findByStatusAndExpirationDateBefore(ReservationStatus status, LocalDate date);

    // 依據 userId 和多個 status 找出預約紀錄(for USER)
    List<Reservation> findByUserIdAndStatusIn(Long userId, List<ReservationStatus> statuses);

    // 管理員用
    List<Reservation> findByUserId(Long userId);

    // 依據 bookCopyId 和 status 找出預約，並依 queuePosition 升冪排序
    List<Reservation> findByBookCopyIdAndStatusOrderByQueuePositionAsc(Long bookCopyId, ReservationStatus status);
    
    // 依據 bookCopyId 找出最大的 queuePosition
    Optional<Reservation> findFirstByBookCopyIdAndStatusOrderByQueuePositionDesc(Long bookCopyId, ReservationStatus status);
}
