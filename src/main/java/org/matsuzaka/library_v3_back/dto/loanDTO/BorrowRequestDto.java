package org.matsuzaka.library_v3_back.dto.loanDTO;

import lombok.Data;

@Data
public class BorrowRequestDto {
    private String uniqueCode;
    private Long userId;
}
