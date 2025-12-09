package org.matsuzaka.library_v3_back.service.Impl;

import jakarta.persistence.EntityNotFoundException;
import org.matsuzaka.library_v3_back.dto.reservationDTO.ReservationResponseDto;
import org.matsuzaka.library_v3_back.model.entity.*;
import org.matsuzaka.library_v3_back.model.enums.*;
import org.matsuzaka.library_v3_back.model.repositoryDao.*;
import org.matsuzaka.library_v3_back.service.NotificationService;
import org.matsuzaka.library_v3_back.service.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  BookCopyRepository bookCopyRepository, 
                                  UserRepository userRepository,
                                  NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void reserveBookCopy(Long userId, Long bookCopyId) {
        // 1. 驗證使用者
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("找不到使用者"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("使用者帳號未啟用，無法預約");
        }

        // 2. 檢查預約額度：一般民眾 5 本，市民 10 本
        int limit = user.getRole() == Role.ROLE_CITIZEN ? 10 : 5;
        List<Reservation> userReservations = reservationRepository.findByUserIdAndStatusIn(userId, 
                List.of(ReservationStatus.PENDING, ReservationStatus.AVAILABLE));
        if (userReservations.size() >= limit) {
            throw new IllegalStateException("預約數量已達上限（" + limit + " 本）");
        }

        // 3. 驗證書籍副本
        BookCopy bookCopy = bookCopyRepository.findById(bookCopyId)
                .orElseThrow(() -> new EntityNotFoundException("找不到此書籍副本"));

        // 4. 檢查副本狀態：只有 L（已借出）的副本才能預約
        if (bookCopy.getStatus() != BookCopyStatus.L) {
            throw new IllegalStateException("此書籍副本目前無法預約（狀態：" + bookCopy.getStatus() + "）。只有已借出的書籍可以預約。");
        }

        // 5. 檢查是否已預約此副本
        boolean alreadyReserved = userReservations.stream()
                .anyMatch(r -> r.getBookCopy().getId().equals(bookCopyId));
        if (alreadyReserved) {
            throw new IllegalStateException("您已預約此書籍副本");
        }

        // 6. 計算排隊位置
        List<Reservation> existingQueue = reservationRepository.findByBookCopyIdAndStatusOrderByQueuePositionAsc(
                bookCopyId, ReservationStatus.PENDING);
        
        int queuePosition = existingQueue.isEmpty() ? 1 : 
                existingQueue.get(existingQueue.size() - 1).getQueuePosition() + 1;

        // 7. 創建預約記錄
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBookCopy(bookCopy);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setQueuePosition(queuePosition);
        reservation.setExpirationDate(LocalDate.now().plusDays(7)); // 預設值，實際在通知時更新
        
        reservationRepository.save(reservation);

        // 8. 發送通知
        notificationService.sendNotification(user, NotificationType.RESERVE_SUCCESS, 
                "預約成功", 
                "您已成功預約《" + bookCopy.getBook().getTitle() + "》（副本編號：" + bookCopy.getUniqueCode() + "），" +
                "目前排隊位置：第 " + queuePosition + " 位。書籍歸還後將依序通知取書。", 
                null, reservation.getId(), ReferenceType.RESERVATION);
    }


    @Override
    public void cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("找不到預約記錄"));
        
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("無權限執行此操作");
        }
        
        if (reservation.getStatus() == ReservationStatus.CANCELLED || 
            reservation.getStatus() == ReservationStatus.PICKED_UP || 
            reservation.getStatus() == ReservationStatus.EXPIRED) {
             throw new IllegalStateException("無法取消已完成或已取消的預約");
        }
        
        ReservationStatus oldStatus = reservation.getStatus();
        Long bookCopyId = reservation.getBookCopy().getId();
        int cancelledQueuePosition = reservation.getQueuePosition();
        
        // 取消預約
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        
        if (oldStatus == ReservationStatus.PENDING) {
            // 如果取消的是 PENDING（排隊中）的預約，需要更新後面所有人的排隊位置
            List<Reservation> laterReservations = reservationRepository
                    .findByBookCopyIdAndStatusOrderByQueuePositionAsc(bookCopyId, ReservationStatus.PENDING);
            
            // 更新所有排在後面的人的 queuePosition（往前遞補）
            for (Reservation r : laterReservations) {
                if (r.getQueuePosition() > cancelledQueuePosition) {
                    r.setQueuePosition(r.getQueuePosition() - 1);
                    reservationRepository.save(r);
                    
                    // 發送通知告知排隊位置更新
                    notificationService.sendNotification(r.getUser(), NotificationType.RESERVE_SUCCESS, 
                            "預約排隊順位更新", 
                            "您預約的《" + r.getBookCopy().getBook().getTitle() + "》排隊順位已更新為第 " + r.getQueuePosition() + " 位", 
                            null, r.getId(), ReferenceType.RESERVATION);
                }
            }
        } else if (oldStatus == ReservationStatus.AVAILABLE) {
            // 如果取消的是 AVAILABLE（可取書）狀態的預約，需要通知下一位或將副本改為可借
            handleReturn(bookCopyId);
        }
    }

    @Override
    public List<ReservationResponseDto> getUserReservations(Long userId) {
        return reservationRepository.findByUserIdAndStatusIn(userId, 
                List.of(ReservationStatus.PENDING, ReservationStatus.AVAILABLE))
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public boolean hasReservationsForCopy(Long bookCopyId) {
        return !reservationRepository.findByBookCopyIdAndStatusOrderByQueuePositionAsc(bookCopyId, ReservationStatus.PENDING).isEmpty();
    }

    @Override
    public void handleReturn(Long bookCopyId) {
        // 查找下一位等待中的預約
        List<Reservation> queue = reservationRepository.findByBookCopyIdAndStatusOrderByQueuePositionAsc(bookCopyId, ReservationStatus.PENDING);
        BookCopy copy = bookCopyRepository.findById(bookCopyId).orElseThrow();

        if (queue.isEmpty()) {
            // 如果沒有人預約，將書籍副本狀態設為 "A" (可借閱)
            copy.setStatus(BookCopyStatus.A);
        } else {
            // 如果有人預約，取出佇列中的第一位
            Reservation nextReservation = queue.getFirst();
            
            // 更新預約狀態為 "AVAILABLE" (可取書)
            nextReservation.setStatus(ReservationStatus.AVAILABLE);
            // 設定通知日期為現在
            nextReservation.setNotifyDate(LocalDateTime.now());
            // 設定取書截止日期為通知後 7 天
            nextReservation.setExpirationDate(LocalDate.now().plusDays(7));
            // 重置排隊順位 (可選，但在 AVAILABLE 狀態下此欄位較無意義)
            nextReservation.setQueuePosition(0);
            reservationRepository.save(nextReservation);
            
            // 將書籍副本狀態設為 "R" (已預約/保留中)
            copy.setStatus(BookCopyStatus.R); 
            
            // 發送通知給下一位預約者
            notificationService.sendNotification(nextReservation.getUser(), NotificationType.RESERVE_AVAILABLE,
                    "預約書籍到館通知", "您預約的書籍《" + copy.getBook().getTitle() + "》已到館，請於 " + nextReservation.getExpirationDate() + " 前取書。",
                    nextReservation.getId(), nextReservation.getId(), ReferenceType.RESERVATION);
        }
        bookCopyRepository.save(copy);
    }
    
    private ReservationResponseDto mapToDto(Reservation r) {
        ReservationResponseDto dto = new ReservationResponseDto();
        dto.setId(r.getId());
        dto.setBookId(r.getBookCopy().getBook().getId());
        dto.setBookTitle(r.getBookCopy().getBook().getTitle());
        dto.setImageUrl(r.getBookCopy().getBook().getImageUrl());
        dto.setBookCopyId(r.getBookCopy().getId());
        dto.setStatus(r.getStatus().name());
        dto.setQueuePosition(r.getQueuePosition());
        dto.setReserveDate(r.getReserveDate());
        dto.setExpirationDate(r.getExpirationDate());
        return dto;
    }
}

