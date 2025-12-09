package org.matsuzaka.library_v3_back.dto.loanDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok: 自動生成 getter, setter, toString, equals, hashCode
@NoArgsConstructor // Lombok: 自動生成無參建構子
@AllArgsConstructor // Lombok: 自動生成包含所有欄位的建構子
public class BookCopyRespDto {

    private Integer id;
    private String uniqueCode;
    private String statusDescription; // 用於儲存轉換後的中文狀態描述
    private String returnDate; // 如果已借出，顯示預計歸還日期，否則為 null


}
