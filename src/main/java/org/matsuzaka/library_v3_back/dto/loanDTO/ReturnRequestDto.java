package org.matsuzaka.library_v3_back.dto.loanDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 歸還請求 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestDto {
    private Long loanId; // 要歸還的借閱記錄 ID
    private Long userId; // 歸還者的使用者 ID (用於權限檢查)
}
