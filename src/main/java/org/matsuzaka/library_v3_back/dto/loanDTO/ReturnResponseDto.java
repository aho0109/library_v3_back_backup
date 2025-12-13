package org.matsuzaka.library_v3_back.dto.loanDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 歸還響應 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponseDto {
    private boolean success; // 操作是否成功
    private String message; // 返回訊息 (例如：成功歸還，或失敗原因)
    private String returnedBookUniqueCode; // 如果成功，返回歸還的副本唯一碼
    
    // 新增：借閱者資訊
    private String borrowerCardId; // 借閱者卡號
    private String borrowerName; // 借閱者姓名
    private String borrowerRole; // 借閱者角色
    private String borrowerStatus; // 借閱者狀態
    private Integer borrowerPenaltyPoints; // 借閱者罰分
    private Integer currentLoanCount; // 當前借閱數量
    private Integer maxLoanCount; // 最大借閱數量
    
    // 新增：本次歸還的書籍資訊
    private String bookTitle; // 書名
    private LocalDateTime loanDate; // 借閱日期
    private LocalDate dueDate; // 到期日期
}
