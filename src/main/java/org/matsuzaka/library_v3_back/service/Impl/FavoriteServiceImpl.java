package org.matsuzaka.library_v3_back.service.Impl;

import jakarta.transaction.Transactional;
import org.matsuzaka.library_v3_back.dto.favoriteDTO.FavoriteDTO;
import org.matsuzaka.library_v3_back.dto.favoriteDTO.FavoriteStatusDTO;
import org.matsuzaka.library_v3_back.exception.BusinessException;
import org.matsuzaka.library_v3_back.exception.ErrorCode;
import org.matsuzaka.library_v3_back.exception.ResourceNotFoundException;
import org.matsuzaka.library_v3_back.model.entity.Author;
import org.matsuzaka.library_v3_back.model.entity.Book;
import org.matsuzaka.library_v3_back.model.entity.Favorite;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.repositoryDao.BookRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.FavoriteRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.UserRepository;
import org.matsuzaka.library_v3_back.service.FavoriteService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    
    public FavoriteServiceImpl(FavoriteRepository favoriteRepository, 
                               UserRepository userRepository, 
                               BookRepository bookRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    // TODO:確認是否 API 已棄用
    @Override
    @Transactional
    public void addFavorite(Long userId, Long bookId) {
        // 檢查是否已收藏
        if (favoriteRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new BusinessException(ErrorCode.ALREADY_FAVORITED);
        }
        
        // 查詢使用者和書籍
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "使用者ID: " + userId));
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_NOT_FOUND, "圖書ID: " + bookId));

        // 建立收藏
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setBook(book);
        favoriteRepository.save(favorite);
    }

    // TODO:確認是否 API 已棄用
    @Override
    @Transactional
    public void removeFavorite(Long userId, Long bookId) {
        // 查詢收藏記錄
        Favorite favorite = favoriteRepository.findByUserIdAndBookId(userId, bookId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FAVORITE_NOT_FOUND));

        // 刪除收藏
        favoriteRepository.delete(favorite);
    }
    
    @Override
    @Transactional
    public FavoriteStatusDTO toggleFavorite(Long userId, Long bookId) {
        Optional<Favorite> existingFavorite = favoriteRepository.findByUserIdAndBookId(userId, bookId);
        
        if (existingFavorite.isPresent()) {
            // 已收藏，則取消收藏
            favoriteRepository.delete(existingFavorite.get());
            return new FavoriteStatusDTO(false);
        } else {
            // 未收藏，則新增收藏
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "使用者ID: " + userId));
            Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_NOT_FOUND, "圖書ID: " + bookId));

            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setBook(book);
            favoriteRepository.save(favorite);
            
            return new FavoriteStatusDTO(true);
        }
    }
    
    @Override
    public List<FavoriteDTO> getUserFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return favorites.stream().map(favorite -> {
            Book book = favorite.getBook();
            
            // 組合作者名稱
            String authorNames = book.getAuthors() != null && !book.getAuthors().isEmpty()
                ? book.getAuthors().stream()
                    .map(Author::getName)
                    .collect(Collectors.joining("、"))
                : "未知";
            
            FavoriteDTO dto = new FavoriteDTO();
            dto.setId(favorite.getId());
            dto.setBookId(book.getId());
            dto.setTitle(book.getTitle());
            dto.setAuthor(authorNames);
            dto.setImageUrl(book.getImageUrl());
            dto.setPublisherName(book.getPublisher() != null ? book.getPublisher().getPubName(): null);
            dto.setAddedDate(book.getAddedDate());
            dto.setCreatedAt(favorite.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public boolean isFavorited(Long userId, Long bookId) {
        return favoriteRepository.existsByUserIdAndBookId(userId, bookId);
    }
    
    @Override
    public List<Long> getUserFavoriteBookIds(Long userId) {
        return favoriteRepository.findBookIdsByUserId(userId);
    }
}

