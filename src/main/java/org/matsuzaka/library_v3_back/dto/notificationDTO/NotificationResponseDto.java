package org.matsuzaka.library_v3_back.dto.notificationDTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponseDto {
    private Long id;
    private String type;
    private String title;
    private String content;
    private boolean isRead;
    private Long relatedId;
    private Long referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
}

