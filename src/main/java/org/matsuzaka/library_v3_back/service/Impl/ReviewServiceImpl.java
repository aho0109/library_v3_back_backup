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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
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

    /**
     * 新增書籍評論
     * @param userId
     * @param bookId
     * @param request
     * @return
     */
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

    /**
     * 編輯書籍評論
     * @param userId
     * @param reviewId
     * @param request
     * @return
     */
    @Override
    public ReviewResponseDto updateReview(Long userId, Long reviewId, ReviewRequestDto request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("找不到評論"));

        // 確認是評論者本人
        if (!Objects.equals(review.getUser().getId(), userId)) {
            throw new IllegalStateException("您只能編輯自己的評論");
        }

        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        review = reviewRepository.save(review);

        // 更新書籍平均評分
        updateBookRating(review.getBook());

        // 檢查當前使用者是否按過讚
        boolean liked = reviewLikeRepository.existsByUserIdAndReviewId(userId, reviewId);
        return mapToDto(review, liked);
    }

    /**
     * 刪除書籍評論
     * @param userId
     * @param reviewId
     */
    @Override
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("找不到評論"));

        // 確認是評論者本人
        if (!Objects.equals(review.getUser().getId(), userId)) {
            throw new IllegalStateException("您只能刪除自己的評論");
        }

        Book book = review.getBook();
        reviewRepository.delete(review);

        // 更新書籍平均評分
        updateBookRating(book);
    }

    private void updateBookRating(Book book) {
        // 載入該書籍所有評論（可透過 JPQL 優化）
        List<Review> reviews = reviewRepository.findByBookId(book.getId());
        
        if (reviews.isEmpty()) {
            book.setAverageRating(BigDecimal.ZERO);
            book.setRatingCount(0);
        } else {
            double avgRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            // 轉換為 BigDecimal 並設定精度（保留一位小數）
            book.setAverageRating(BigDecimal.valueOf(avgRating)
                    .setScale(1, RoundingMode.HALF_UP));
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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("找不到使用者"));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("找不到評論"));

        // 已經按過讚
        if (reviewLikeRepository.existsByUserIdAndReviewId(userId, reviewId)) {
            /* TODO: 把客製化訊息回傳給前端 */
            System.out.println("您已按過讚");
            System.out.println("使用者 " + userId + " 已經按過讚評論 " + reviewId);
            return;
        }

        // 按自己的讚無效
        if (Objects.equals(review.getUser().getId(), userId)) {
            System.out.println("您不能按自己的讚");
            throw new IllegalStateException("您不能按自己的讚");
        }


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

    /**
     * Toggle 按讚（按過就取消，沒按過就新增）
     * @param userId
     * @param reviewId
     * @return 回傳當前狀態（true=已按讚，false=已取消）
     */
    @Override
    public boolean toggleLikeReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("找不到評論"));

        // 按自己的讚無效
        if (Objects.equals(review.getUser().getId(), userId)) {
            throw new IllegalStateException("您不能按自己的讚");
        }

        // 檢查是否已按過讚
        Optional<ReviewLike> existingLike = reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId);

        if (existingLike.isPresent()) {
            // 已按讚 -> 取消讚
            reviewLikeRepository.delete(existingLike.get());
            review.setLikesCount(Math.max(0, review.getLikesCount() - 1));
            reviewRepository.save(review);
            return false; // 已取消讚
        } else {
            // 未按讚 -> 新增讚
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("找不到使用者"));

            ReviewLike like = new ReviewLike();
            like.setUser(user);
            like.setReview(review);
            reviewLikeRepository.save(like);

            review.setLikesCount(review.getLikesCount() + 1);
            reviewRepository.save(review);
            return true; // 已按讚
        }
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

