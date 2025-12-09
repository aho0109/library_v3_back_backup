package org.matsuzaka.library_v3_back.dto.loanDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

// 續借響應 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenewResponseDto {

    private boolean success;
    private String message;
    private LocalDate newDueDate;
}
