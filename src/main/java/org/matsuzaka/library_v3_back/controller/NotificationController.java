package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.notificationDTO.NotificationResponseDto;
import org.matsuzaka.library_v3_back.security.UserDetailSecu;
import org.matsuzaka.library_v3_back.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知功能 Controller
 * 
 * 功能：
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
    public ResponseEntity<?> sendTestNotification(@PathVariable Long userId) {
        notificationService.sendTestNotification(userId);
        return ResponseEntity.ok("測試通知發送成功");
    }

    /**
     * 獲取當前使用者的所有通知
     * 按建立時間降序排列（最新的在最上面）
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        return ResponseEntity.ok(notificationService.getUserNotifications(currentUser.getUser().getId()));
    }

    /**
     * 標記通知為已讀
     */
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId,
                                        @AuthenticationPrincipal UserDetailSecu currentUser) {
        try {
            notificationService.markAsRead(notificationId, currentUser.getUser().getId());
            return ResponseEntity.ok("已標記為已讀");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 獲取未讀通知數量
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserDetailSecu currentUser) {
        return ResponseEntity.ok(notificationService.getUnreadCount(currentUser.getUser().getId()));
    }

    /**
     * 標記所有通知為已讀
     */
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal UserDetailSecu currentUser) {
        try {
            notificationService.markAllAsRead(currentUser.getUser().getId());
            return ResponseEntity.ok("已全部標記為已讀");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

