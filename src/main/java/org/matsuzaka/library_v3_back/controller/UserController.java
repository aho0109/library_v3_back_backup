package org.matsuzaka.library_v3_back.controller;


import jakarta.persistence.EntityNotFoundException;
import org.matsuzaka.library_v3_back.dto.userDTO.*;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.security.UserDetailSecu;
import org.matsuzaka.library_v3_back.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users") // 使用 /api/users 作為使用者相關 API 的根路徑
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/test/findAllUsers")
    public List<User> findAllUser() {
        return userService.getAllUserService();
    }

    // 個人帳號頁面
    @GetMapping("/{userId}/profile") // 獲取使用者個人檔案
    public ResponseEntity<UserDetailRespDto> getUserProfile(@PathVariable Long userId) {
        // 這裡不需要 try-catch，EntityNotFoundException 會被 GlobalExceptionHandler 捕獲
        UserDetailRespDto userProfile = userService.getUserDetailById(userId);
        return ResponseEntity.ok(userProfile);
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

    // 更新密碼
    /**
     * 驗證舊密碼的端點。
     * 這裡只接收舊密碼，不包含新密碼，用於前端的第一步驗證。
     */
    @PostMapping("/update/{userId}/change-password/verify-old")
    public ResponseEntity<ChangePasswordResponse> verifyOldPassword(@PathVariable Long userId, @RequestBody ChangePasswordRequest request) {
        try {
            boolean isValid = userService.verifyOldPassword(userId, request.getOldPassword());
            if (isValid) {
                return ResponseEntity.ok(new ChangePasswordResponse(true, "原密碼驗證成功。"));
            } else {
                // 理論上 verifyOldPassword 會拋出異常，這裡不會執行到
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ChangePasswordResponse(false, "原密碼驗證失敗。"));
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ChangePasswordResponse(false, "使用者不存在。"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            // IllegalArgumentException 處理 "原密碼輸入錯誤"
            // IllegalStateException 處理 "帳號被鎖定"
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ChangePasswordResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ChangePasswordResponse(false, "驗證原密碼時發生錯誤哈哈。"));
        }
    }
    /**
     * 移除 try-catch 區塊，讓例外直接拋出，由 GlobalExceptionHandler 捕獲
     */
    @PostMapping("/update/{userId}/change-password/verify-old-maybe")
    public ResponseEntity<ChangePasswordResponse> verifyOldPasswordMaybe(@PathVariable Long userId, @RequestBody ChangePasswordRequest request) {
        // 移除 try-catch 區塊，讓例外直接拋出，由 GlobalExceptionHandler 捕獲
        boolean isValid = userService.verifyOldPassword(userId, request.getOldPassword());
        // 如果驗證成功，返回成功響應
        return ResponseEntity.ok(new ChangePasswordResponse(true, "原密碼驗證成功。"));
    }

    /**
     * 修改密碼的端點。
     * 接收舊密碼和新密碼。
     */
    @PostMapping("/update/{userId}/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(@PathVariable Long userId, @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(new ChangePasswordResponse(true, "密碼修改成功。"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ChangePasswordResponse(false, "使用者不存在。"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            // IllegalArgumentException 處理 "原密碼不正確" 或 "新密碼不符合要求"
            // IllegalStateException 處理 "帳號被鎖定"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ChangePasswordResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ChangePasswordResponse(false, "修改密碼時發生錯誤。"));
        }
    }
    /**
     * 例外將由 GlobalExceptionHandler 處理。
     */
    @PostMapping("/update/{userId}/change-password-maybe")
    public ResponseEntity<ChangePasswordResponse> changePasswordMaybe(@PathVariable Long userId, @RequestBody ChangePasswordRequest request) {
        // 移除 try-catch 區塊，讓例外直接拋出，由 GlobalExceptionHandler 捕獲
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        // 如果修改成功，返回成功響應
        return ResponseEntity.ok(new ChangePasswordResponse(true, "密碼修改成功。"));
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

    /*
      註冊不需更改，因為註冊本來就不需要身份驗證。
     */
}
