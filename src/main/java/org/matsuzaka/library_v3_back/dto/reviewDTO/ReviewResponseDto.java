package org.matsuzaka.library_v3_back.dto.reviewDTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponseDto {
    private Long id;
    private Long userId;
    private String userName;
    private Integer rating;
    private String reviewText;
    private Integer likesCount;
    private LocalDateTime createdAt;
    private boolean likedByCurrentUser;
}

