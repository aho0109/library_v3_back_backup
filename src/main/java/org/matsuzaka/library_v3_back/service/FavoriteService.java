package org.matsuzaka.library_v3_back.service;

import org.matsuzaka.library_v3_back.dto.favoriteDTO.FavoriteDTO;
import org.matsuzaka.library_v3_back.dto.favoriteDTO.FavoriteStatusDTO;

import java.util.List;

public interface FavoriteService {
    
    /**
     * 新增收藏
     */
    void addFavorite(Long userId, Long bookId);
    
    /**
     * 取消收藏
     */
    void removeFavorite(Long userId, Long bookId);
    
    /**
     * 切換收藏狀態（有就刪除，沒有就新增）
     */
    FavoriteStatusDTO toggleFavorite(Long userId, Long bookId);
    
    /**
     * 查詢使用者的所有收藏
     */
    List<FavoriteDTO> getUserFavorites(Long userId);
    
    /**
     * 檢查使用者是否已收藏該書籍
     */
    boolean isFavorited(Long userId, Long bookId);
    
    /**
     * 查詢使用者收藏的書籍 ID 列表
     */
    List<Long> getUserFavoriteBookIds(Long userId);
}

