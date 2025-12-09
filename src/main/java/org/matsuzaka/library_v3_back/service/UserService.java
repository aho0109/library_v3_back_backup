package org.matsuzaka.library_v3_back.service;



import org.matsuzaka.library_v3_back.dto.userDTO.UserDetailRespDto;
import org.matsuzaka.library_v3_back.dto.userDTO.UserRegistrationRequest;
import org.matsuzaka.library_v3_back.model.entity.User;

import java.util.List;

public interface UserService {
    List<User> getAllUserService();

    // 個人帳號頁面
    UserDetailRespDto getUserDetailById(Long userId);

    // 註冊
    UserDetailRespDto getUserProfile(Long userId);
    void registerUser(UserRegistrationRequest request); // 新增註冊方法

    // 更新密碼
    boolean verifyOldPassword(Long userId, String oldPassword); // 新增：驗證舊密碼
    void changePassword(Long userId, String oldPassword, String newPassword); // 新增：修改密碼

}
