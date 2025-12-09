package org.matsuzaka.library_v3_back.service;

import org.matsuzaka.library_v3_back.dto.notificationDTO.NotificationResponseDto;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.enums.NotificationType;
import org.matsuzaka.library_v3_back.model.enums.ReferenceType;

import java.util.List;

public interface NotificationService {
    void sendNotification(User user, NotificationType type, String title, String content, Long relatedId, Long referenceId, ReferenceType referenceType);
    List<NotificationResponseDto> getUserNotifications(Long userId);
    void markAsRead(Long notificationId);
}
