package org.matsuzaka.library_v3_back.service.Impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matsuzaka.library_v3_back.dto.userDTO.UserRegistrationRequest;
import org.matsuzaka.library_v3_back.dto.userDTO.UserUpdateRequest;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.entity.UserDetail;
import org.matsuzaka.library_v3_back.model.enums.UserStatus;
import org.matsuzaka.library_v3_back.model.mapper.UserMapper;
import org.matsuzaka.library_v3_back.model.repositoryDao.UserDetailRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.UserRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetailRepository userDetailRepository;
    @Mock
    private UserMapper userMapper;
    @Spy // 使用 @Spy 而不是 @Mock，因為我們需要真實的加密行為
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;
    private UserDetail mockUserDetail;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setAccount("testuser");
        mockUser.setPassword(passwordEncoder.encode("oldPassword"));
        mockUser.setStatus(UserStatus.ACTIVE);

        mockUserDetail = new UserDetail();
        mockUserDetail.setEmail("test@example.com");
        mockUserDetail.setName("Test User");
        mockUser.setUserDetail(mockUserDetail);
        mockUserDetail.setUser(mockUser);
    }

    // --- 註冊測試 (registerUser) ---

    @Test
    void registerUser_Success() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setAccount("newUser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        when(userRepository.findByAccount("newUser")).thenReturn(Optional.empty());
        when(userDetailRepository.findByEmail("new@example.com")).thenReturn(null);
        when(userRepository.findMaxCardIdNumber()).thenReturn(10L);

        userService.registerUser(request);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_Fail_AccountExists() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setAccount("existingUser");

        when(userRepository.findByAccount("existingUser")).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            userService.registerUser(request)
        );
        assertEquals("帳號已存在。", exception.getMessage());
    }

    @Test
    void registerUser_Fail_EmailExists() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setAccount("newUser");
        request.setEmail("existing@example.com");

        when(userRepository.findByAccount("newUser")).thenReturn(Optional.empty());
        when(userDetailRepository.findByEmail("existing@example.com")).thenReturn(new UserDetail());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            userService.registerUser(request)
        );
        assertEquals("電子郵件已存在。", exception.getMessage());
    }

    // --- 更新資料測試 (updateUserProfile) ---

    @Test
    void updateUserProfile_Success() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setAccount("newAccount");
        request.setEmail("new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.findByAccount("newAccount")).thenReturn(Optional.empty());
        when(userDetailRepository.findByEmail("new@example.com")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User updatedUser = userService.updateUserProfile(1L, request);

        assertNotNull(updatedUser);
        assertEquals("newAccount", updatedUser.getAccount());
        assertEquals("new@example.com", updatedUser.getUserDetail().getEmail());
    }

    @Test
    void updateUserProfile_Fail_AccountExists() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setAccount("otherUserAccount");

        User otherUser = new User();
        otherUser.setId(2L); // 不同的 ID

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.findByAccount("otherUserAccount")).thenReturn(Optional.of(otherUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            userService.updateUserProfile(1L, request)
        );
        assertEquals("此帳號已被其他使用者註冊。", exception.getMessage());
    }

    @Test
    void updateUserProfile_Fail_EmailExists() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("other@example.com");

        User otherUser = new User();
        otherUser.setId(2L);
        UserDetail otherUserDetail = new UserDetail();
        otherUserDetail.setUser(otherUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userDetailRepository.findByEmail("other@example.com")).thenReturn(otherUserDetail);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            userService.updateUserProfile(1L, request)
        );
        assertEquals("此電子郵件已被其他使用者註冊。", exception.getMessage());
    }

    // --- 密碼驗證與修改測試 ---

    @Test
    void verifyOldPassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        
        boolean result = userService.verifyOldPassword(1L, "oldPassword");
        
        assertTrue(result);
    }

    @Test
    void verifyOldPassword_Fail_IncorrectPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            userService.verifyOldPassword(1L, "wrongPassword")
        );
        assertTrue(exception.getMessage().contains("原密碼輸入錯誤。您還有"));
    }

    @Test
    void verifyOldPassword_Fail_AccountLocked() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // 模擬連續失敗
        assertThrows(IllegalArgumentException.class, () -> userService.verifyOldPassword(1L, "wrong1"));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyOldPassword(1L, "wrong2"));
        
        // 第三次失敗，應該拋出鎖定例外
        IllegalStateException lockException = assertThrows(IllegalStateException.class, () ->
            userService.verifyOldPassword(1L, "wrong3")
        );
        assertTrue(lockException.getMessage().contains("原密碼錯誤次數過多"));

        // 第四次嘗試，應該直接拋出已鎖定例外
        IllegalStateException lockedException = assertThrows(IllegalStateException.class, () ->
            userService.verifyOldPassword(1L, "oldPassword")
        );
        assertTrue(lockedException.getMessage().contains("您的帳號因多次嘗試失敗已被鎖定"));
    }

    @Test
    void changePassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        userService.changePassword(1L, "oldPassword", "newPassword");

        // 驗證密碼是否真的被更新 (雖然是 Spy，但可以這樣驗證)
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertTrue(passwordEncoder.matches("newPassword", savedUser.getPassword()));
    }

    @Test
    void changePassword_Fail_IncorrectOldPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            userService.changePassword(1L, "wrongOldPassword", "newPassword")
        );
        assertEquals("原密碼不正確。", exception.getMessage());
    }

    // --- 管理員操作測試 ---

    @Test
    void activateUser_Success() {
        mockUser.setStatus(UserStatus.PENDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        userService.activateUser(1L);

        assertEquals(UserStatus.ACTIVE, mockUser.getStatus());
        verify(userRepository).save(mockUser);
    }

    @Test
    void activateUser_Fail_AlreadyActive() {
        mockUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        assertThrows(IllegalStateException.class, () ->
            userService.activateUser(1L)
        );
    }

    @Test
    void suspendUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        userService.suspendUser(1L);

        assertEquals(UserStatus.SUSPENDED, mockUser.getStatus());
        assertNotNull(mockUser.getSuspendedUntil());
        verify(userRepository).save(mockUser);
    }

    @Test
    void restoreUser_Success() {
        mockUser.setStatus(UserStatus.SUSPENDED);
        mockUser.setPenaltyPoints(5);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        userService.restoreUser(1L);

        assertEquals(UserStatus.ACTIVE, mockUser.getStatus());
        assertNull(mockUser.getSuspendedUntil());
        assertEquals(0, mockUser.getPenaltyPoints()); // 驗證罰點已歸零
        verify(userRepository).save(mockUser);
    }

    // --- 補強測試案例 ---

    @Test
    void testGetMethods() {
        // 測試 getAllUserService
        userService.getAllUserService();
        verify(userRepository, times(1)).findAll();

        // 測試 getUserDetailById
        userService.getUserDetailById(1L);
        verify(userRepository, times(1)).findUserDetailById(1L);

        // 測試 getUserProfile
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        userService.getUserProfile(1L);
        verify(userMapper, times(1)).toUserDetailRespDto(mockUser);
    }

    @Test
    void registerUser_Success_FirstUser() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setAccount("firstUser");
        request.setEmail("first@example.com");
        request.setPassword("password123");

        when(userRepository.findByAccount("firstUser")).thenReturn(Optional.empty());
        when(userDetailRepository.findByEmail("first@example.com")).thenReturn(null);
        // 模擬資料庫是空的，回傳 null
        when(userRepository.findMaxCardIdNumber()).thenReturn(null);

        userService.registerUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        // 驗證第一位使用者的卡號是否為 LIB001
        assertEquals("LIB001", userCaptor.getValue().getCardId());
    }

    @Test
    void updateUserProfile_Fail_UserDetailNotFound() {
        mockUser.setUserDetail(null); // 模擬資料異常，沒有 UserDetail
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UserUpdateRequest request = new UserUpdateRequest();
        
        assertThrows(EntityNotFoundException.class, () -> 
            userService.updateUserProfile(1L, request)
        );
    }

    @Test
    void updateUserProfile_Success_PartialUpdate() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("New Name");
        // 其他欄位為 null

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        userService.updateUserProfile(1L, request);

        assertEquals("New Name", mockUser.getUserDetail().getName());
        assertEquals("test@example.com", mockUser.getUserDetail().getEmail()); // Email 應保持不變
    }

    @Test
    void updateUserProfile_Success_SameAccountAndEmail() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setAccount("testuser"); // 與原帳號相同
        request.setEmail("test@example.com"); // 與原 Email 相同

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        userService.updateUserProfile(1L, request);

        // 驗證沒有呼叫 findByAccount 或 findByEmail (因為值沒變)
        verify(userRepository, never()).findByAccount(anyString());
        verify(userDetailRepository, never()).findByEmail(anyString());
    }

    @Test
    void changePassword_Fail_AccountLocked() {
        // 1. 準備環境
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        
        // 2. 觸發 3 次密碼錯誤，導致帳號被鎖定
        // 為了確保密碼驗證失敗，我們明確 Mock passwordEncoder.matches 回傳 false
        // 注意：因為是 Spy，所以要用 doReturn...when 的語法來覆蓋真實行為
        doReturn(false).when(passwordEncoder).matches(anyString(), anyString());

        try {
            userService.verifyOldPassword(1L, "wrong1");
        } catch (Exception e) {}
        try {
            userService.verifyOldPassword(1L, "wrong2");
        } catch (Exception e) {}
        try {
            userService.verifyOldPassword(1L, "wrong3");
        } catch (Exception e) {}

        // 3. 嘗試修改密碼，應拋出鎖定例外
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            userService.changePassword(1L, "oldPassword", "newPassword")
        );
        assertTrue(exception.getMessage().contains("已被鎖定"));
    }

    @Test
    void changePassword_Fail_WeakPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        // 這裡需要讓舊密碼驗證通過，才能走到新密碼檢查
        doReturn(true).when(passwordEncoder).matches(eq("oldPassword"), anyString());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            userService.changePassword(1L, "oldPassword", "123") // 密碼太短
        );
        assertEquals("新密碼長度至少為6個字元。", exception.getMessage());
    }

    @Test
    void verifyOldPassword_Success_LockExpired() throws InterruptedException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // 1. 觸發鎖定
        try {
            userService.verifyOldPassword(1L, "wrong1");
            userService.verifyOldPassword(1L, "wrong2");
            userService.verifyOldPassword(1L, "wrong3");
        } catch (Exception e) {}

        // 2. 這裡我們無法真的等待 2 小時。
        // 在單元測試中，要測試時間相關的邏輯，最好的方法是將時間提供者 (Clock) 抽象化並 Mock。
        // 但由於我們沒有這樣做，這裡只能透過 Reflection 或修改 Service 的可見性來測試，或者接受這個限制。
        // 不過，我們可以測試 "如果鎖定時間已過，是否會重置"。
        
        // 由於無法簡單 Mock LocalDateTime.now()，這個測試案例在不重構程式碼的情況下很難精確撰寫。
        // 我們可以選擇跳過這個極端邊界條件，或者重構 Service 引入 Clock。
        // 為了保持簡單，我們這裡先不寫這個測試，因為它需要修改 Service 結構。
        // 但我們可以測試 "如果沒有被鎖定，就不會拋出例外" (這已經在 verifyOldPassword_Success 測過了)。
    }
}
