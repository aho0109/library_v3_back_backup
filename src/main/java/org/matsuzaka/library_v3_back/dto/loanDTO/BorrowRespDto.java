package org.matsuzaka.library_v3_back.dto.loanDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 借閱響應 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRespDto {
    private boolean success; // 操作是否成功
    private String message; // 返回訊息 (例如：成功借閱，或失敗原因)
    private String borrowedBookUniqueCode; // 如果成功，返回借閱的副本唯一碼
    private Long loanId; // 如果成功，返回借閱記錄ID
}