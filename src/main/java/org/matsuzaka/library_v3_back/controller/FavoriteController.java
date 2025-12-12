package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.favoriteDTO.FavoriteDTO;
import org.matsuzaka.library_v3_back.dto.favoriteDTO.FavoriteStatusDTO;
import org.matsuzaka.library_v3_back.security.UserDetailSecu;
import org.matsuzaka.library_v3_back.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {
    
    private final FavoriteService favoriteService;
    
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }
    
    /**
     * 獲取當前使用者的所有收藏
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FavoriteDTO>> getMyFavorites(
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId();
        List<FavoriteDTO> favorites = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(favorites);
    }
    
    /**
     * 切換收藏狀態（有就刪除，沒有就新增）
     */
    @PostMapping("/toggle/{bookId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FavoriteStatusDTO> toggleFavorite(
            @PathVariable Long bookId,
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId();
        FavoriteStatusDTO status = favoriteService.toggleFavorite(userId, bookId);
        return ResponseEntity.ok(status);
    }
    
    /**
     * 檢查是否已收藏某書籍
     */
    @GetMapping("/check/{bookId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FavoriteStatusDTO> checkFavorite(
            @PathVariable Long bookId,
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId();
        boolean isFavorited = favoriteService.isFavorited(userId, bookId);
        return ResponseEntity.ok(new FavoriteStatusDTO(isFavorited));
    }
    
    /**
     * 獲取當前使用者收藏的所有書籍 ID
     */
    @GetMapping("/book-ids")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Long>> getFavoriteBookIds(
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId();
        List<Long> bookIds = favoriteService.getUserFavoriteBookIds(userId);
        return ResponseEntity.ok(bookIds);
    }
    
    /**
     * 新增收藏
     */
    @PostMapping("/{bookId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> addFavorite(
            @PathVariable Long bookId,
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId();
        favoriteService.addFavorite(userId, bookId);
        return ResponseEntity.ok("收藏成功");
    }
    
    /**
     * 取消收藏
     */
    @DeleteMapping("/{bookId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> removeFavorite(
            @PathVariable Long bookId,
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId();
        favoriteService.removeFavorite(userId, bookId);
        return ResponseEntity.ok("已取消收藏");
    }
}

