package org.matsuzaka.library_v3_back.dto.userDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理員借還書時查詢使用者的簡化資訊 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBriefDTO {
    private Long id;
    private String cardId;
    private String name;
    private String account;
    private String role;
    private String status;
    private Integer penaltyPoints;
    private Integer currentLoansCount; // 目前借閱數量
    private Integer maxLoansAllowed;   // 最大借閱額度
}

