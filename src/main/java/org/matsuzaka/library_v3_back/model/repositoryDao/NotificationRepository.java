package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * 查詢使用者的所有通知，按建立時間降序
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 查詢使用者的未讀通知
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    /**
     * 查詢使用者的已讀/未讀通知
     */
    List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);
    
    /**
     * 計算使用者的已讀/未讀通知數量
     */
    Long countByUserIdAndIsRead(Long userId, Boolean isRead);
}

