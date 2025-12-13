package org.matsuzaka.library_v3_back.dto.adminDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.matsuzaka.library_v3_back.model.enums.BookCopyStatus;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookCopyDTO {
    private String uniqueCode; // 可選，如果為空則自動生成
    private LocalDate stockedDate; // 進貨日期
    private BookCopyStatus status; // 狀態，預設為 A (Available)
}

