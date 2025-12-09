package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.model.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 找出使用者是否有對某本書的未過期預約
    Optional<Reservation> findByUserIdAndBookCopyIdAndExpirationDateAfter(Long userId, Long bookCopyId, LocalDate date);

    // 找出所有已過期但未取書的預約 (用於排程任務)
    List<Reservation> findByReservationStatusTitleAndExpirationDateBefore(String statusTitle, LocalDate date);

    // 找出使用者未完成的預約
    List<Reservation> findByUserIdAndReservationStatusTitleIn(Long userId, List<String> statusTitles);
}
