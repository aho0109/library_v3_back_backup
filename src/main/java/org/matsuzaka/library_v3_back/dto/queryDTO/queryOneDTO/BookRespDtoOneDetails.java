package org.matsuzaka.library_v3_back.dto.queryDTO.queryOneDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.matsuzaka.library_v3_back.dto.loanDTO.BookCopyRespDto;
import org.matsuzaka.library_v3_back.model.entity.Author;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRespDtoOneDetails {
    private Integer id;
    private String title;
    private String seriesTitle; // 系列作標題，如果不是系列作則為 null
    private Long seriesId; // 系列 ID（新增用於編輯）
    private Boolean representative; // 是否為代表作（新增用於編輯）
    private String publisher; // 出版商名稱
    private Integer publishYear;
    private String isbn; 
    private String imageUrl;
    private String addedDate; // book上架日期

    private Set<Author> authors; // 作者名稱列表，用於顯示多個作者
    private Set<String> tags; // 標籤名稱列表，用於顯示多個標籤

    private Long mainCategoryId;
    private String mainCategoryTitle; // 主分類名稱

    private Long subCategoryId;
    private String subCategoryTitle;  // 子分類名稱

    private BigDecimal availableCopies; // 可借閱數量
    private Long totalCopies;     // 總副本數量
    private Set<BookCopyRespDto> bookCopies; // 新增：書籍副本列表
    
    // New fields for V3
    private BigDecimal averageRating;
    private Integer ratingCount;
}
