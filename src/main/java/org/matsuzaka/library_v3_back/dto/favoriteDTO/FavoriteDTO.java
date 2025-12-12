package org.matsuzaka.library_v3_back.dto.favoriteDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteDTO {
    private Long id;
    private Long bookId;
    private String title;
    private String author;
    private String imageUrl;
    private String publisherName;
    private LocalDate addedDate;
    private LocalDateTime createdAt; // 收藏日期
}

