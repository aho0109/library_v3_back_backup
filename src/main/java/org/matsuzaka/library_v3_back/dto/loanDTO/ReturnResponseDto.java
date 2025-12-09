package org.matsuzaka.library_v3_back.dto.loanDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 歸還響應 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponseDto {
    private boolean success; // 操作是否成功
    private String message; // 返回訊息 (例如：成功歸還，或失敗原因)
    private String returnedBookUniqueCode; // 如果成功，返回歸還的副本唯一碼
}