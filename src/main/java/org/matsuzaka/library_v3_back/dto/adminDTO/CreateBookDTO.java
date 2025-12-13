package org.matsuzaka.library_v3_back.dto.adminDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookDTO {

    private String title; // 書名，必填
    // private Long categoryId; // 主分類 ID，由 service 層處理，前端不需要傳入
    private Long publisherId; // 出版社 ID，必填
    private Short publishYear; // 出版年份，必填 null
    private BigDecimal price; // 價格，必填
    private String imageUrl;
    private String isbn;
    private Boolean representative; // 是否為代表作，service 處理，前端不需要傳入

    private Long categorySubId; // 子分類 ID，必填，只需要傳入最精確的子分類 ID

    // 系列 ID 是可選的
    private Long seriesId;

    // 使用輔助 DTO 來處理複雜的多對多關聯
    private ManyToManyInputDTO authors;
    private ManyToManyInputDTO tags;

}
