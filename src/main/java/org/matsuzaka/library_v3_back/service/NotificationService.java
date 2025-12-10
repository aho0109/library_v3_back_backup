package org.matsuzaka.library_v3_back.service;

import org.matsuzaka.library_v3_back.dto.notificationDTO.NotificationResponseDto;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.enums.NotificationType;
import org.matsuzaka.library_v3_back.model.enums.ReferenceType;

import java.util.List;

public interface NotificationService {


    /**
     * 發送測試通知
     */
    void sendTestNotification(Long userId);

    /**
     * 發送通知（站內信 + Email）
     */
    void sendNotification(User user, NotificationType type, String title, String content, 
                         Long relatedId, Long referenceId, ReferenceType referenceType);
    
    /**
     * 獲取使用者的所有通知
     */
    List<NotificationResponseDto> getUserNotifications(Long userId);
    
    /**
     * 標記單筆通知為已讀
     */
    void markAsRead(Long notificationId, Long userId);
    
    /**
     * 標記使用者所有通知為已讀
     */
    void markAllAsRead(Long userId);
    
    /**
     * 獲取使用者的未讀通知數量
     */
    Long getUnreadCount(Long userId);
}
