package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.notificationDTO.NotificationResponseDto;
import org.matsuzaka.library_v3_back.exception.BusinessException;
import org.matsuzaka.library_v3_back.exception.ErrorCode;
import org.matsuzaka.library_v3_back.exception.ResourceNotFoundException;
import org.matsuzaka.library_v3_back.model.entity.Notification;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.enums.NotificationType;
import org.matsuzaka.library_v3_back.model.enums.ReferenceType;
import org.matsuzaka.library_v3_back.model.repositoryDao.NotificationRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.UserRepository;
import org.matsuzaka.library_v3_back.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void sendTestNotification(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "使用者ID: " + userId));
        // 1. 儲存站內通知
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.WELCOME);
        notification.setTitle("測試通知");
        notification.setContent("恭喜你呀！這是一則測試通知！");
        notification.setRelatedId(null);
        notification.setReferenceId(null);
        notification.setReferenceType(ReferenceType.SYSTEM);
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }

    @Override
    public void sendNotification(User user, NotificationType type, String title, String content, 
                                Long relatedId, Long referenceId, ReferenceType referenceType) {
        // 1. 儲存站內通知
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle("【" + title + "】");
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setReferenceId(referenceId);
        notification.setReferenceType(referenceType);
        notification.setIsRead(false);
        
        notificationRepository.save(notification);
        
        // 2. 發送 Email（異步處理，避免阻塞主流程）
        // TODO: 實現 Email 發送功能
        // 可以使用 Spring Mail 或第三方服務（如 SendGrid, AWS SES）
        // sendEmail(user, type, title, content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, "通知ID: " + notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "無權限執行此操作");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unreadNotifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return (long) notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).size();
    }

    private NotificationResponseDto mapToDto(Notification n) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(n.getId());
        dto.setType(n.getType().name());
        dto.setTitle(n.getTitle());
        dto.setContent(n.getContent());
        dto.setRead(n.getIsRead());
        dto.setRelatedId(n.getRelatedId());
        dto.setReferenceId(n.getReferenceId());
        dto.setReferenceType(n.getReferenceType() != null ? n.getReferenceType().name() : null);
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}

