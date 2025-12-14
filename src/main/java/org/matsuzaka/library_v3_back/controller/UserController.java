package org.matsuzaka.library_v3_back.controller;


import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.matsuzaka.library_v3_back.dto.notificationDTO.NotificationResponseDto;
import org.matsuzaka.library_v3_back.dto.userDTO.*;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.mapper.UserMapper;
import org.matsuzaka.library_v3_back.security.JwtUtil;
import org.matsuzaka.library_v3_back.security.UserDetailSecu;
import org.matsuzaka.library_v3_back.service.NotificationService;
import org.matsuzaka.library_v3_back.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users") // 使用 /api/users 作為使用者相關 API 的根路徑
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public UserController(UserService userService, NotificationService notificationService, JwtUtil jwtUtil, UserMapper userMapper) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    @GetMapping("/test/findAllUsers")
    public List<User> findAllUser() {
        return userService.getAllUserService();
    }


    // 註冊
    @PostMapping("/register") // 新增註冊 API 端點
    public ResponseEntity<RegistrationResponse> registerUser(@RequestBody UserRegistrationRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok(new RegistrationResponse(true, "註冊成功！"));
        } catch (IllegalArgumentException e) {
            // 如果帳號或電子郵件已存在，返回 409 Conflict
            // 這個特定的 IllegalArgumentException 仍然可以在這裡處理，
            // 因為它返回的是 CONFLICT 狀態，而 GlobalExceptionHandler 可能預設處理為 BAD_REQUEST。
            // 如果你希望所有 IllegalArgumentException 都返回 BAD_REQUEST，則可以移除此 try-catch。
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new RegistrationResponse(false, e.getMessage()));
        } catch (Exception e) {
            // 其他未知錯誤，可以讓 GlobalExceptionHandler 處理，或者在這裡返回通用錯誤
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RegistrationResponse(false, "註冊失敗，請稍後再試。"));
        }
    }



    // 以下為新版


    /**
     * 【核心修改 2：實現安全的 "me" 端點】
     * 建立一個專門用來獲取「當前登入使用者自己」個人檔案的端點。
     *
     * @param currentUser Spring Security 會透過 @AuthenticationPrincipal 註解，
     * 將 JwtAuthenticationFilter 驗證成功後的使用者物件 (UserDetailSecu) 自動注入進來。
     * 這個 currentUser 物件是從 JWT 中安全解析出來的，絕對可信。
     * @return 當前登入使用者的詳細個人檔案。
     */
    @GetMapping("/me/profile")
    public ResponseEntity<UserDetailRespDto> getCurrentUserProfile(@AuthenticationPrincipal UserDetailSecu currentUser) {
        // 從安全的 currentUser 物件中獲取使用者 ID，而不是從前端的任何請求參數中獲取。
        Long userId = currentUser.getUser().getId();
        UserDetailRespDto userProfile = userService.getUserDetailById(userId);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDetailRespDto> updateUserProfile(@AuthenticationPrincipal UserDetailSecu currentUser, @Valid @RequestBody UserUpdateRequest request) {
        Long userId = currentUser.getUser().getId();
        
        // 1. 更新使用者資料，並取得更新後的 User 實體
        User updatedUserEntity = userService.updateUserProfile(userId, request);
        
        // 2. 根據更新後的 User 實體，建立一個新的 UserDetailSecu 物件
        UserDetailSecu newUserDetails = new UserDetailSecu(updatedUserEntity);
        
        // 3. 產生新的 JWT
        String newToken = jwtUtil.generateToken(newUserDetails);
        
        // 4. 將 User 實體轉換為 DTO
        UserDetailRespDto responseDto = userMapper.toUserDetailRespDto(updatedUserEntity);
        
        // 5. 在 DTO 中設定新的 JWT
        responseDto.setJwtToken(newToken);
        
        // 6. 回傳包含新 JWT 的 DTO
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 【核心修改 3：將密碼驗證也改為安全的 "me" 端點】
     *
     * @param currentUser 當前登入的使用者，由 Spring Security 安全提供。
     * @param request 包含舊密碼的請求體。
     * @return 驗證結果。
     */
    @PostMapping("/me/change-password/verify-old")
    public ResponseEntity<ChangePasswordResponse> verifyOldPasswordNew(@AuthenticationPrincipal UserDetailSecu currentUser, @RequestBody ChangePasswordRequest request) {
        Long userId = currentUser.getUser().getId();
        // 讓 Service 層處理業務邏輯和可能的例外，Controller 保持簡潔。
        boolean isValid = userService.verifyOldPassword(userId, request.getOldPassword());
        return ResponseEntity.ok(new ChangePasswordResponse(true, "原密碼驗證成功NEW。"));
    }

    /**
     * 【核心修改 4：將修改密碼也改為安全的 "me" 端點】
     *
     * @param currentUser 當前登入的使用者。
     * @param request 包含舊密碼和新密碼的請求體。
     * @return 修改結果。
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(@AuthenticationPrincipal UserDetailSecu currentUser, @RequestBody ChangePasswordRequest request) {
        Long userId = currentUser.getUser().getId();
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(new ChangePasswordResponse(true, "密碼修改成功NEW。"));
    }

    /**
     * 獲取當前登入使用者的所有通知。
     * TODO: 似乎和 NotificationController 重複，待整理。
     * @param currentUser 當前登入的使用者，由 Spring Security 安全提供。
     * @return 使用者的通知列表。
     */
    @GetMapping("/me/notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(@AuthenticationPrincipal UserDetailSecu currentUser) {
        return ResponseEntity.ok(notificationService.getUserNotifications(currentUser.getUser().getId()));
    }

    /**
     * 標記當前使用者的單筆通知為已讀。
     * TODO: 似乎和 NotificationController 重複，待整理。
     * @param id
     * @param currentUser
     * @return
     */
    @PutMapping("/me/notifications/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markNotificationRead(@PathVariable Long id, @AuthenticationPrincipal UserDetailSecu currentUser) {
        notificationService.markAsRead(id, currentUser.getUser().getId());
        return ResponseEntity.ok().build();
    }

    /*
      註冊不需更改，因為註冊本來就不需要身份驗證。
     */
}
