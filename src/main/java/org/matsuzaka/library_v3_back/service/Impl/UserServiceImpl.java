package org.matsuzaka.library_v3_back.service.Impl;

import org.matsuzaka.library_v3_back.dto.userDTO.UserDetailRespDto;
import org.matsuzaka.library_v3_back.dto.userDTO.UserRegistrationRequest;
import org.matsuzaka.library_v3_back.dto.userDTO.UserUpdateRequest;
import org.matsuzaka.library_v3_back.exception.BusinessException;
import org.matsuzaka.library_v3_back.exception.ErrorCode;
import org.matsuzaka.library_v3_back.exception.ResourceNotFoundException;
import org.matsuzaka.library_v3_back.exception.ValidationException;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.entity.UserDetail;
import org.matsuzaka.library_v3_back.model.enums.Role;
import org.matsuzaka.library_v3_back.model.enums.UserStatus;
import org.matsuzaka.library_v3_back.model.mapper.UserMapper;
import org.matsuzaka.library_v3_back.model.repositoryDao.UserDetailRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.UserRepository;
import org.matsuzaka.library_v3_back.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository; // 注入 UserDetailRepository
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder; // 注入密碼編碼器

    public UserServiceImpl(UserRepository userRepository, UserDetailRepository userDetailRepository, UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userDetailRepository = userDetailRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /*
     * 可以，Spring Security 支援使用 Pbkdf2PasswordEncoder 來指定 SHA-256 作為雜湊演算法。你可以這樣替換：
     * private final Pbkdf2PasswordEncoder passwordEncoder; // 注入 SHA-256 密碼編碼器
     * this.passwordEncoder = new Pbkdf2PasswordEncoder("", 185000, 256, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
     * */

    @Override
    //findAllService
    public List<User> getAllUserService() {
        return userRepository.findAll();
    }

    // 個人帳號頁面
    @Override
    public UserDetailRespDto getUserDetailById(Long userId) {
        return userRepository.findUserDetailById(userId);
    }

    @Override
    public UserDetailRespDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "使用者ID: " + userId));
        return userMapper.toUserDetailRespDto(user);
    }

    @Override
    @Transactional
    public void registerUser(UserRegistrationRequest request) {
        // 1. 檢查帳號、電子郵件、手機號碼是否已存在
        if (userRepository.existsByAccount(request.getAccount())) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_EXISTS, "帳號: " + request.getAccount());
        }
        if (userDetailRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "電子郵件: " + request.getEmail());
        }
        if (userDetailRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, "手機號碼: " + request.getPhone());
        }

        // 2. 生成 card_id
        String newCardId = generateNextCardId();

        // 3. 創建 User 實體
        User user = new User();
        user.setAccount(request.getAccount());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // 雜湊密碼
        user.setRole(Role.ROLE_USER); // 預設為普通使用者
        user.setStatus(UserStatus.PENDING); // 預設為待開通
        user.setCardId(newCardId); // 將 card_id 設定在 User 實體上

        // 4. 創建 UserDetail 實體
        UserDetail userDetail = new UserDetail();
        userDetail.setName(request.getName());
        userDetail.setEmail(request.getEmail());
        userDetail.setPhone(request.getPhone());
        userDetail.setAddress(request.getAddress());
        //userDetail.setCardId(newCardId); // 設定生成的 card_id

        // 建立雙向關聯
        user.setUserDetail(userDetail);
        userDetail.setUser(user);

        // 5. 儲存 User (會級聯儲存 UserDetail)
        userRepository.save(user);
    }

    // 輔助方法：生成下一個 card_id
    private String generateNextCardId() {
        Long maxNumber = userRepository.findMaxCardIdNumber(); // 從 UserRepository 查詢最大數字
        if (maxNumber == null) {
            maxNumber = 0L; // 如果沒有任何 LIB 開頭的 card_id，從 0 開始
        }
        Long nextNumber = maxNumber + 1;
        // 格式化為 LIB001, LIB002 等
        return String.format("LIB%03d", nextNumber);
        // "LIB%03d" 是格式化字串，LIB 是固定字首，%03d 代表整數（d），且不足三位時前面補零（0），總共三位數（3）。
        // nextNumber 是要插入的數字，例如 1 會變成 001，2 會變成 002。
        // 最終結果會是 LIB001、LIB002 這種格式的卡號。
    }

    @Override
    @Transactional
    public User updateUserProfile(Long userId, UserUpdateRequest request) {
        // 檢查使用者是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "使用者ID: " + userId));

        // 檢查使用者詳細資料是否存在
        UserDetail userDetail = user.getUserDetail();
        if (userDetail == null) {
            throw new ResourceNotFoundException(ErrorCode.USER_DETAIL_NOT_FOUND, "使用者ID: " + userId);
        }

        // 更新使用者詳細資料
        if (request.getAccount() != null && !request.getAccount().equals(user.getAccount())) {
            Optional<User> existingUserByAccount = userRepository.findByAccount(request.getAccount());
            // 檢查帳號是否已存在
            if (existingUserByAccount.isPresent() && !Objects.equals(existingUserByAccount.get().getId(), userId)) {
                throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
            }
            user.setAccount(request.getAccount());
        }

        // 更新電子郵件
        if (request.getEmail() != null && !request.getEmail().equals(userDetail.getEmail())) {
            UserDetail existingUserByEmail = userDetailRepository.findByEmail(request.getEmail());
            // 檢查電子郵件是否已存在
            if (existingUserByEmail != null && !Objects.equals(existingUserByEmail.getUser().getId(), userId)) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            userDetail.setEmail(request.getEmail());
        }

        // 更新手機號碼
        if (request.getPhone() != null && !request.getPhone().equals(userDetail.getPhone())) {
            UserDetail existingUserByPhone = userDetailRepository.findByPhone(request.getPhone());
            // 檢查手機號碼是否已存在
            if (existingUserByPhone != null && !Objects.equals(existingUserByPhone.getUser().getId(), userId)) {
                throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
            }
            userDetail.setPhone(request.getPhone());
        }

        // 更新名字
        if (request.getName() != null) {
            userDetail.setName(request.getName());
        }

        // 更新地址
        if (request.getAddress() != null) {
            userDetail.setAddress(request.getAddress());
        }

        return userRepository.save(user);
    }

    /**
     * 更新密碼
     */
    /**
     * 驗證使用者舊密碼，並處理失敗嘗試次數限制。
     * @param userId 使用者 ID
     * @param oldPassword 使用者輸入的舊密碼
     * @return 如果舊密碼正確則返回 true，否則返回 false
     * @throws IllegalStateException 如果帳號被鎖定
     */

    // --- 記憶體內密碼嘗試次數限制相關屬性 (簡化範例，重啟應用程式會重置) ---
    private static final int MAX_ATTEMPTS = 3; // 最大嘗試次數
    private static final long LOCKOUT_DURATION_MINUTES = 2 * 60; // 鎖定時間 2 小時 (分鐘)

    // 儲存每個使用者 ID 的失敗嘗試次數
    private final Map<Long, Integer> failedPasswordAttempts = new ConcurrentHashMap<>();
    // 儲存每個使用者 ID 的鎖定解除時間
    private final Map<Long, LocalDateTime> lockoutTimes = new ConcurrentHashMap<>();
    // --- 結束記憶體內次數限制相關屬性 ---

    @Override
    public boolean verifyOldPassword(Long userId, String oldPassword) {
        // 檢查是否被鎖定
        if (isUserLocked(userId)) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED,
                "請於 " + lockoutTimes.get(userId).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + " 後再試");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "使用者ID: " + userId));

        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            // 密碼正確，重置失敗嘗試次數和鎖定時間
            failedPasswordAttempts.remove(userId);
            lockoutTimes.remove(userId);
            return true;
        } else {
            // 密碼錯誤，增加失敗嘗試次數
            failedPasswordAttempts.merge(userId, 1, Integer::sum);
            int attempts = failedPasswordAttempts.get(userId);
            System.out.println("User " + userId + " failed password attempts: " + attempts);

            if (attempts >= MAX_ATTEMPTS) {
                // 達到最大嘗試次數，鎖定帳號
                LocalDateTime lockoutUntil = LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES);
                lockoutTimes.put(userId, lockoutUntil);
                System.out.println("User " + userId + " locked until: " + lockoutUntil);
                throw new BusinessException(ErrorCode.PASSWORD_ATTEMPT_EXCEEDED,
                    "帳號已被鎖定 " + LOCKOUT_DURATION_MINUTES + " 分鐘");
            }
            throw new BusinessException(ErrorCode.OLD_PASSWORD_INCORRECT,
                "您還有 " + (MAX_ATTEMPTS - attempts) + " 次機會");
        }
    }

    /**
     * 修改使用者密碼。
     * @param userId 使用者 ID
     * @param oldPassword 使用者輸入的舊密碼 (再次驗證，確保安全性)
     * @param newPassword 新密碼
     * @throws IllegalStateException 如果帳號被鎖定
     * @throws IllegalArgumentException 如果舊密碼不正確或新密碼不符合要求
     */
    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 再次檢查是否被鎖定 (防止繞過 verifyOldPassword 直接呼叫 changePassword)
        if (isUserLocked(userId)) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED,
                "請於 " + lockoutTimes.get(userId).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + " 後再試");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "使用者ID: " + userId));

        // 再次驗證舊密碼
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            // 這裡可以選擇再次觸發失敗嘗試邏輯，但為了避免重複訊息，直接拋出錯誤
            // 或者可以將 verifyOldPassword 的邏輯整合進來
            throw new BusinessException(ErrorCode.OLD_PASSWORD_INCORRECT);
        }

        // 密碼強度檢查
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException(ErrorCode.PASSWORD_TOO_SHORT, "新密碼長度至少為6個字元");
        }

        // 更新密碼
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 密碼修改成功，重置失敗嘗試次數和鎖定時間
        failedPasswordAttempts.remove(userId);
        lockoutTimes.remove(userId);
        System.out.println("User " + userId + " password changed successfully. Failed attempts reset.");
    }

    // 輔助方法：檢查使用者是否被鎖定
    private boolean isUserLocked(Long userId) {
        if (lockoutTimes.containsKey(userId)) {
            LocalDateTime lockoutUntil = lockoutTimes.get(userId);
            if (LocalDateTime.now().isBefore(lockoutUntil)) {
                return true; // 仍在鎖定期間
            } else {
                // 鎖定時間已過，解除鎖定
                lockoutTimes.remove(userId);
                failedPasswordAttempts.remove(userId); // 同時重置嘗試次數
                System.out.println("User " + userId + " lockout expired and reset.");
                return false;
            }
        }
        return false; // 未被鎖定
    }

    /**
     * 會員搜尋（管理員用）
     */
    @Override
    public List<UserDetailRespDto> searchUsers(String cardId, String account, String name, String email, String phone) {
        // 如果所有參數都為空，返回所有使用者
        if (cardId == null && account == null && name == null && email == null && phone == null) {
            return userRepository.findAll().stream()
                    .map(userMapper::toUserDetailRespDto)
                    .toList();
        }

        // 使用 JPA Specification 進行動態查詢
        return userRepository.searchUsers(cardId, account, name, email, phone);
    }

    /**
     * 開通帳號
     */
    @Override
    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "使用者ID: " + userId));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "帳號已經是啟用狀態");
        }
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    /**
     * 停權帳號
     */
    @Override
    @Transactional
    public void suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "使用者ID: " + userId));

        user.setStatus(UserStatus.SUSPENDED);
        user.setSuspendedUntil(LocalDateTime.now().plusDays(30)); // 停權30天
        userRepository.save(user);
    }

    /**
     * 復權帳號
     */
    @Override
    @Transactional
    public void restoreUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "使用者ID: " + userId));

        user.setStatus(UserStatus.ACTIVE);
        user.setSuspendedUntil(null);
        user.setPenaltyPoints(0); // 復權時清零罰分
        userRepository.save(user);
    }
}
