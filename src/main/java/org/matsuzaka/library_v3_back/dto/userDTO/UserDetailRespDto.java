package org.matsuzaka.library_v3_back.dto.userDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.matsuzaka.library_v3_back.model.enums.Role;
import org.matsuzaka.library_v3_back.model.enums.UserStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    // 應應使用者更新個人檔案，需回傳新的 JWT
    private String jwtToken;

    // Constructor for JPA/JPQL projection
    // 更新：從 String 換成 UserStatus Role
    public UserDetailRespDto(Long id, String name, String cardId, String account, String email, String phone, String address, Integer penaltyPoints, UserStatus status, Role role, LocalDateTime suspendedUntil, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.cardId = cardId;
        this.account = account;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.penaltyPoints = penaltyPoints;
        this.status = (status != null) ? status.name() : null; // Convert Enum to String
        this.role = (role != null) ? role.name() : null;       // Convert Enum to String
        this.suspendedUntil = suspendedUntil;
        this.createdAt = createdAt;
    }
}
