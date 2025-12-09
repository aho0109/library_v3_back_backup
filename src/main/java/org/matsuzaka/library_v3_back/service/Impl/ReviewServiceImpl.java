package org.matsuzaka.library_v3_back.service.Impl;

import jakarta.persistence.EntityNotFoundException;
import org.matsuzaka.library_v3_back.dto.reviewDTO.ReviewRequestDto;
import org.matsuzaka.library_v3_back.dto.reviewDTO.ReviewResponseDto;
import org.matsuzaka.library_v3_back.model.entity.Book;
import org.matsuzaka.library_v3_back.model.entity.Review;
import org.matsuzaka.library_v3_back.model.entity.ReviewLike;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.repositoryDao.BookRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.ReviewLikeRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.ReviewRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.UserRepository;
import org.matsuzaka.library_v3_back.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ReviewLikeRepository reviewLikeRepository,
                             BookRepository bookRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ReviewResponseDto addReview(Long userId, Long bookId, ReviewRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("找不到使用者"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("找不到書籍"));

        Optional<Review> existingReview = reviewRepository.findByUserIdAndBookId(userId, bookId);
        if (existingReview.isPresent()) {
            throw new IllegalStateException("您已評論過此書籍");
        }

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        review.setLikesCount(0);

        review = reviewRepository.save(review);

        // 更新書籍平均評分
        updateBookRating(book);

        return mapToDto(review, false);
    }

    private void updateBookRating(Book book) {
        // 載入該書籍所有評論（可透過 JPQL 優化）
        java.util.List<Review> reviews = reviewRepository.findByBookId(book.getId());
        
        if (reviews.isEmpty()) {
            book.setAverageRating(0.0);
            book.setRatingCount(0);
        } else {
            double avgRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            book.setAverageRating(avgRating);
            book.setRatingCount(reviews.size());
        }
        bookRepository.save(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviewsByBookId(Long bookId, Long currentUserId, Pageable pageable) {
        return reviewRepository.findByBookId(bookId, pageable)
                .map(review -> {
                    boolean liked = false;
                    if (currentUserId != null) {
                        liked = reviewLikeRepository.existsByUserIdAndReviewId(currentUserId, review.getId());
                    }
                    return mapToDto(review, liked);
                });
    }

    @Override
    public void likeReview(Long userId, Long reviewId) {
        if (reviewLikeRepository.existsByUserIdAndReviewId(userId, reviewId)) {
            return; // 已經按過讚
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("找不到使用者"));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("找不到評論"));

        ReviewLike like = new ReviewLike();
        like.setUser(user);
        like.setReview(review);
        reviewLikeRepository.save(like);

        review.setLikesCount(review.getLikesCount() + 1);
        reviewRepository.save(review);
    }

    @Override
    public void unlikeReview(Long userId, Long reviewId) {
        ReviewLike like = reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId)
                .orElseThrow(() -> new EntityNotFoundException("找不到按讚記錄"));
        
        reviewLikeRepository.delete(like);

        Review review = like.getReview();
        review.setLikesCount(Math.max(0, review.getLikesCount() - 1));
        reviewRepository.save(review);
    }

    private ReviewResponseDto mapToDto(Review review, boolean likedByCurrentUser) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setId(review.getId());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getUserDetail() != null ? review.getUser().getUserDetail().getName() : review.getUser().getAccount());
        dto.setRating(review.getRating());
        dto.setReviewText(review.getReviewText());
        dto.setLikesCount(review.getLikesCount());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setLikedByCurrentUser(likedByCurrentUser);
        return dto;
    }
}

