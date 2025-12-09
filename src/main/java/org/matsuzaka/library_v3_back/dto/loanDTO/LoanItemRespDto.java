package org.matsuzaka.library_v3_back.dto.loanDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanItemRespDto {

    // 我原本只給書籍ID，但在處理借閱紀錄時，應該以loanId作為驅動
    // 每一筆 loans 表中的記錄 (loanId) 都代表一個獨立的借閱事件，它記錄了「哪位使用者在何時借了哪本特定的書籍副本」。
    // 歸還一本書時，你歸還的是這筆特定的借閱記錄所代表的書籍副本。
    // loanId 是唯一識別這筆借閱記錄的鍵，因此直接使用 loanId 來進行歸還操作是最精確和安全的。

    // 移植時 Integer 忘了改成 Long，前端顯示詭異的 403 錯誤，後來加了 ExceptionHandler 才能正確反應錯誤類型 0819

    private Long loanId; // 借閱記錄ID
    private Long id; // 書籍ID
    private String title; // 書名
    private String imageUrl; // 建議也包含圖片URL，方便顯示

    private String uniqueCode; // 書籍副本的唯一碼
    private LocalDate loanDate; // 借閱日期
    private LocalDate dueDate; // 預計歸還日期
    private LocalDate returnDate; // 歸還日期 (如果已歸還，否則為 null)

    private String authors; // 作者名稱列表，用於顯示多個作者


}
