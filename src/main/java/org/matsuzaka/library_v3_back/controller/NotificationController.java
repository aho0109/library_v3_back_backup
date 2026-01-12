package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
import org.matsuzaka.library_v3_back.dto.notificationDTO.NotificationResponseDto;
import org.matsuzaka.library_v3_back.security.UserDetailSecu;
import org.matsuzaka.library_v3_back.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知功能 Controller
 * 1. 讀取使用者的所有通知（站內信）
 * 2. 標記通知為已讀
 * 3. 取得未讀通知數量
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 發送測試通知
     */
    @PostMapping("/test/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendTestNotification(@PathVariable Long userId) {
        notificationService.sendTestNotification(userId);
        return ResponseEntity.ok(ApiResponse.success("測試通知發送成功"));
    }

    /**
     * 獲取當前使用者的所有通知
     * 按建立時間降序排列（最新的在最上面）
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getMyNotifications(
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        List<NotificationResponseDto> notifications = notificationService.getUserNotifications(currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * 標記通知為已讀
     */
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        notificationService.markAsRead(notificationId, currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("通知已標記為已讀"));
    }

    /**
     * 標記所有通知為已讀
     */
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        notificationService.markAllAsRead(currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("所有通知已標記為已讀"));
    }

    /**
     * 取得未讀通知數量
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        long count = notificationService.getUnreadCount(currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

//    /**
//     * 刪除通知
//     */
//    @DeleteMapping("/{notificationId}")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<ApiResponse<Void>> deleteNotification(
//            @PathVariable Long notificationId,
//            @AuthenticationPrincipal UserDetailSecu currentUser) {
//        notificationService.deleteNotification(notificationId, currentUser.getUser().getId());
//        return ResponseEntity.ok(ApiResponse.success("通知刪除成功"));
//    }
}
