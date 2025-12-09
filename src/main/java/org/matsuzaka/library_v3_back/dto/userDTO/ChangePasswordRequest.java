package org.matsuzaka.library_v3_back.dto.userDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest { // 接收修改密碼的請求
    private String oldPassword;
    private String newPassword;
    // 這裡不需要 confirmNewPassword，因為前端會先驗證
}
