package org.matsuzaka.library_v3_back.dto.reviewDTO;

import lombok.Data;

@Data
public class ReviewRequestDto {
    private Integer rating;
    private String reviewText;
}

