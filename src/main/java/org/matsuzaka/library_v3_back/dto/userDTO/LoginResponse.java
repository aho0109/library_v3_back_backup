package org.matsuzaka.library_v3_back.dto.userDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 用於返回登入結果的 DTO (包含 JWT)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    /*private String jwt; // 包含簽發給前端的 JWT 字串
    private Long userId;     // 使用者 ID
    private String role;     // 使用者角色*/
    /**
     * 登入回應 DTO 被大幅簡化。
     * 它現在只包含一個欄位：jwtToken。
     * 這是因為所有必要的身份資訊 (userId, role, username) 都已經安全地嵌入在 token 內部了。
     * 前端不再需要、也不應該接收任何獨立的身份資訊。
     */
    private String jwtToken;

}
