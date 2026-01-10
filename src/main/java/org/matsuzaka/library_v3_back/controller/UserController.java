package org.matsuzaka.library_v3_back.controller;

import jakarta.validation.Valid;
import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
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
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        userService.registerUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("註冊成功！請等待管理員開通帳號"));
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
    public ResponseEntity<ApiResponse<UserDetailRespDto>> getCurrentUserProfile(@AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId();
        UserDetailRespDto userProfile = userService.getUserDetailById(userId);
        return ResponseEntity.ok(ApiResponse.success(userProfile));
    }

    @PutMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDetailRespDto>> updateUserProfile(
            @AuthenticationPrincipal UserDetailSecu currentUser,
            @Valid @RequestBody UserUpdateRequest request) {
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
        return ResponseEntity.ok(ApiResponse.success("個人資料更新成功", responseDto));
    }

    /**
     * 【核心修改 3：將密碼驗證也改為安全的 "me" 端點】
     *
     * @param currentUser 當前登入的使用者，由 Spring Security 安全提供。
     * @param request 包含舊密碼的請求體。
     * @return 驗證結果。
     */
    @PostMapping("/me/change-password/verify-old")
    public ResponseEntity<ApiResponse<Void>> verifyOldPasswordNew(
            @AuthenticationPrincipal UserDetailSecu currentUser,
            @RequestBody ChangePasswordRequest request) {
        Long userId = currentUser.getUser().getId();
        userService.verifyOldPassword(userId, request.getOldPassword());
        return ResponseEntity.ok(ApiResponse.success("原密碼驗證成功"));
    }

    /**
     * 【核心修改 4：將修改密碼也改為安全的 "me" 端點】
     *
     * @param currentUser 當前登入的使用者。
     * @param request 包含舊密碼和新密碼的請求體。
     * @return 修改結果。
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetailSecu currentUser,
            @RequestBody ChangePasswordRequest request) {
        Long userId = currentUser.getUser().getId();
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("密碼修改成功"));
    }

    /**
     * 獲取當前登入使用者的所有通知。
     * TODO: 似乎和 NotificationController 重複，待整理。
     * @param currentUser 當前登入的使用者，由 Spring Security 安全提供。
     * @return 使用者的通知列表。
     */
    @GetMapping("/me/notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getMyNotifications(
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        List<NotificationResponseDto> notifications = notificationService.getUserNotifications(currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(notifications));
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
    public ResponseEntity<ApiResponse<Void>> markNotificationRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        notificationService.markAsRead(id, currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("已讀取通知"));
    }

    /*
      註冊不需更改，因為註冊本來就不需要身份驗證。
     */
}
