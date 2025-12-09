package org.matsuzaka.library_v3_back.dto.userDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    private String account;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String address;
    // cardId 將在後端自動生成，不從前端接收
    // role 也將在後端設置為預設值
}
