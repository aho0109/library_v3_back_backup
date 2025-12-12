package org.matsuzaka.library_v3_back.dto.userDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailRespDto {
    private Long id;        // 使用者ID (通常是 Long)
    private String name;    // 姓名
    private String cardId;  // 借書證ID
    private String account; // 帳號
    private String email;   // 電子郵件
    private String phone;   // 電話
    private String address; // 地址
    
    // New fields for V3
    private Integer penaltyPoints;
    private String status; // PENDING, ACTIVE, SUSPENDED
    private String role; // ROLE_USER, ROLE_CITIZEN, ROLE_ADMIN
    private LocalDateTime suspendedUntil;
    private LocalDateTime createdAt;
}
