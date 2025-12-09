package org.matsuzaka.library_v3_back.dto.queryDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 統計項目的 DTO。
 * 這是通用模型，用於表示任一篩選條件下的 ID、名稱和符合的書籍數量。
 * 這是 FacetedSearchStatsDTO 中每一個統計項目的通用資料模型。
 */
@Data
@AllArgsConstructor
public class StatItemDTO {
    private Long id;       // 篩選條件的ID，例如 category_id 或 author_id
    private String name;   // 篩選條件的名稱，例如 "漫畫" 或 "作者甲"
    private Long count;    // 符合該條件的書籍數量
}