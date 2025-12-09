package org.matsuzaka.library_v3_back.dto.queryDTO;

import lombok.Data;

import java.util.List;

/**
 * 分面搜尋統計資料的 DTO。
 * 用於將各篩選條件下的數量統計結果傳輸給前端，以便動態生成篩選面板。
 * 這是用於左側篩選面板的統計資料模型。它將包含多個子 DTO。
 */

@Data
public class FacetedSearchStatsDTO {
    private List<StatItemDTO> categories; // 主分類統計
    private List<StatItemDTO> subCategories; // 子分類統計
    private List<StatItemDTO> series; // 系列統計
    private List<StatItemDTO> authors; // 作者統計
    private List<StatItemDTO> publishers; // 出版社統計
    private List<StatItemDTO> tags; // 標籤統計
}