package org.matsuzaka.library_v3_back.dto.queryDTO;

import lombok.Data;

/**
 * 書籍搜尋回應的複合 DTO。用來封裝書籍列表和分面搜尋的統計資料，確保它們在單一 API 回應中傳輸。
 * 用於在單一 API 請求中，同時傳輸分頁後的書籍列表與分面篩選的統計資料。
 * 這能減少網路請求次數，提升使用者體驗。
 */
@Data
public class BookSearchResponseDTO {

    // 包含書籍清單和分頁資訊的物件
    private PageResponseDTO<BookListItemDTO> bookPage;

    // 包含各類別、作者等統計數量的物件
    private FacetedSearchStatsDTO stats;
}

