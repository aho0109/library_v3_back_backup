package org.matsuzaka.library_v3_back.dto.queryDTO.queryOneDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.matsuzaka.library_v3_back.dto.loanDTO.BookCopyRespDto;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRespDtoOneDetails {
    private Integer id;
    private String title;
    private String seriesTitle; // 系列作標題，如果不是系列作則為 null
    private String publisher; // 出版商名稱
    private Integer publishYear;
    private BigDecimal price; // 新增：價格
    private String isbn; // 新增：ISBN (假設你的 Book 實體有這個欄位)
    //private String description; // 新增：書籍描述 (假設你的 Book 實體有這個欄位)
    private String imageUrl; // 新增：書籍封面圖片 URL (如果你的 Book 實體有這個欄位)

    private Set<String> authors; // 作者名稱列表，用於顯示多個作者
    private Set<String> tags; // 標籤名稱列表，用於顯示多個標籤

    private String mainCategoryTitle; // 主分類名稱
    private String subCategoryTitle;  // 子分類名稱

    private BigDecimal availableCopies; // 可借閱數量
    private Long totalCopies;     // 總副本數量
    private Set<BookCopyRespDto> bookCopies; // 新增：書籍副本列表




}


