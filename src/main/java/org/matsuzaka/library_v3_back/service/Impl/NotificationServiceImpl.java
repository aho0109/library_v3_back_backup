package org.matsuzaka.library_v3_back.service.Impl;

import jakarta.persistence.EntityNotFoundException;
import org.matsuzaka.library_v3_back.dto.notificationDTO.NotificationResponseDto;
import org.matsuzaka.library_v3_back.model.entity.Notification;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.enums.NotificationType;
import org.matsuzaka.library_v3_back.model.enums.ReferenceType;
import org.matsuzaka.library_v3_back.model.repositoryDao.NotificationRepository;
import org.matsuzaka.library_v3_back.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void sendNotification(User user, NotificationType type, String title, String content, Long relatedId, Long referenceId, ReferenceType referenceType) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setReferenceId(referenceId);
        notification.setReferenceType(referenceType);
        notification.setIsRead(false);
        
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("找不到通知記錄"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
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

