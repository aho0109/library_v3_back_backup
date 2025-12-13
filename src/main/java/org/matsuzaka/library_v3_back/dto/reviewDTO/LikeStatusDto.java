package org.matsuzaka.library_v3_back.dto.reviewDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeStatusDto {
    private boolean liked;  // 改為 liked，避免 Lombok 處理 isLiked 時出現問題
}

