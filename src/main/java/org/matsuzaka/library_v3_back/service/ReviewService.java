package org.matsuzaka.library_v3_back.service;

import org.matsuzaka.library_v3_back.dto.reviewDTO.ReviewRequestDto;
import org.matsuzaka.library_v3_back.dto.reviewDTO.ReviewResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponseDto addReview(Long userId, Long bookId, ReviewRequestDto request);
    ReviewResponseDto updateReview(Long userId, Long reviewId, ReviewRequestDto request);
    void deleteReview(Long userId, Long reviewId);
    Page<ReviewResponseDto> getReviewsByBookId(Long bookId, Long currentUserId, Pageable pageable);
    void likeReview(Long userId, Long reviewId);
    void unlikeReview(Long userId, Long reviewId);
    boolean toggleLikeReview(Long userId, Long reviewId);
}

