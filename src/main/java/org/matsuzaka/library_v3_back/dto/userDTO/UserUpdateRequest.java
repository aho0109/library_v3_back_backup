package org.matsuzaka.library_v3_back.dto.userDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Size(min = 4, max = 20, message = "帳號長度必須介於 4 到 20 個字元")
    private String account;

    @Size(min = 1, max = 50, message = "姓名不能為空")
    private String name;

    @Email(message = "請輸入有效的電子郵件地址")
    private String email;

    private String phone;

    private String address;
}
