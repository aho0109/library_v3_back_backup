package org.matsuzaka.library_v3_back.dto.queryDTO;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 通用的分頁回應 DTO。
 * 用於封裝分頁結果，提供一個穩定的、不受 Spring 內部實現影響的 JSON 結構。
 *
 * @param <T> DTO 內容的泛型類型
 */
@Data
public class PageResponseDTO<T> {

    // 頁面內容列表
    private List<T> content;

    // 總記錄數
    private long totalElements;

    // 總頁數
    private int totalPages;

    // 當前頁碼 (從0開始)
    private int currentPage;

    // 頁面大小 (每頁記錄數)
    private int pageSize;

    // 是否為最後一頁
    private boolean last;

    // 是否為第一頁
    private boolean first;

    /**
     * 靜態工廠方法，將 Spring Data 的 Page 物件轉換為自定義的 PageResponseDTO。
     * 這是Service或Controller層常用的轉換方式。
     *
     * @param page Spring Data 的 Page 物件
     * @param <T> 泛型類型
     * @return 轉換後的 PageResponseDTO 物件
     */
    public static <T> PageResponseDTO<T> fromPage(Page<T> page) {
        PageResponseDTO<T> dto = new PageResponseDTO<>();
        dto.setContent(page.getContent());
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        dto.setCurrentPage(page.getNumber());
        dto.setPageSize(page.getSize());
        dto.setLast(page.isLast());
        dto.setFirst(page.isFirst());
        return dto;
    }
}