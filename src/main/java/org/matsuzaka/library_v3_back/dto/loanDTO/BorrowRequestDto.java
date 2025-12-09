package org.matsuzaka.library_v3_back.dto.loanDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 借閱請求 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequestDto {
    private Long bookId; // 要借閱的書籍ID
    private Long userId; // 借閱者的使用者ID
}