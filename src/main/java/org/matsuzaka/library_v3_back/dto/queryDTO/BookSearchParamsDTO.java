package org.matsuzaka.library_v3_back.dto.queryDTO;

import lombok.Getter;

import java.util.List;

/**
 * 書籍查詢參數封裝類
 * 職責說明:
 * 1. 封裝所有與書籍查詢相關的參數
 * 2. 減少方法間的參數傳遞複雜度
 * 3. 提高代碼可讀性和可維護性
 *
 * 設計理念:
 * - 參數物件模式(Parameter Object Pattern)的應用
 * - 減少長參數列表(Long Parameter List)的程式碼異味
 * - 便於未來擴展查詢參數而不影響方法簽名
 */
@Getter
public class BookSearchParamsDTO {
    // Getters
    private String keyword;           // 搜尋關鍵字
    private Long mainCategoryId;      // 主分類ID
    private Long subCategoryId;       // 子分類ID
    private Integer seriesDisplay;    // 系列顯示模式
    private Long authorId;            // 作者ID
    private Long publisherId;         // 出版社ID
    private List<Long> tagIds;        // 標籤ID列表
    private Long seriesId;            // 系列ID

    // 新增管理員專用搜尋參數
    private String authorKeyword;     // 作者關鍵字（模糊搜尋）
    private String publisherKeyword;  // 出版商關鍵字（模糊搜尋）
    private String bookTitleKeyword;  // 書名關鍵字（模糊搜尋）
    private Short publishYear;        // 出版年份
    private String isbn;              // ISBN精確搜尋

    // 私有建構子，阻止直接實例化
    private BookSearchParamsDTO() {}

    /**
     * 建構者類，用於流暢地建立查詢參數對象
     */
    public static class Builder {
        private final BookSearchParamsDTO params;

        public Builder() {
            this.params = new BookSearchParamsDTO();
        }

        public Builder keyword(String keyword) {
            params.keyword = keyword;
            return this;
        }

        public Builder mainCategoryId(Long mainCategoryId) {
            params.mainCategoryId = mainCategoryId;
            return this;
        }

        public Builder subCategoryId(Long subCategoryId) {
            params.subCategoryId = subCategoryId;
            return this;
        }

        public Builder seriesDisplay(Integer seriesDisplay) {
            params.seriesDisplay = seriesDisplay;
            return this;
        }

        public Builder authorId(Long authorId) {
            params.authorId = authorId;
            return this;
        }

        public Builder publisherId(Long publisherId) {
            params.publisherId = publisherId;
            return this;
        }

        public Builder tagIds(List<Long> tagIds) {
            params.tagIds = tagIds;
            return this;
        }

        public Builder seriesId(Long seriesId) {
            params.seriesId = seriesId;
            return this;
        }

        // 新增管理員專用搜尋參數的建構子
        public Builder authorKeyword(String authorKeyword) {
            params.authorKeyword = authorKeyword;
            return this;
        }

        public Builder publisherKeyword(String publisherKeyword) {
            params.publisherKeyword = publisherKeyword;
            return this;
        }

        public Builder bookTitleKeyword(String bookTitleKeyword) {
            params.bookTitleKeyword = bookTitleKeyword;
            return this;
        }

        public Builder publishYear(Short publishYear) {
            params.publishYear = publishYear;
            return this;
        }

        public Builder isbn(String isbn) {
            params.isbn = isbn;
            return this;
        }

        public BookSearchParamsDTO build() {
            return params;
        }
    }

    /**
     * 從原始參數快速創建查詢參數對象的工廠方法
     */
    public static BookSearchParamsDTO from(
            String keyword, Long mainCategoryId, Long subCategoryId,
            Integer seriesDisplay, Long authorId, Long publisherId,
            List<Long> tagIds, Long seriesId,
            String authorKeyword, String publisherKeyword, String bookTitleKeyword,
            Short publishYear, String isbn) {

        return new Builder()
                .keyword(keyword)
                .mainCategoryId(mainCategoryId)
                .subCategoryId(subCategoryId)
                .seriesDisplay(seriesDisplay)
                .authorId(authorId)
                .publisherId(publisherId)
                .tagIds(tagIds)
                .seriesId(seriesId)  // 添加 seriesId 设置
                .authorKeyword(authorKeyword)
                .publisherKeyword(publisherKeyword)
                .bookTitleKeyword(bookTitleKeyword)
                .publishYear(publishYear)
                .isbn(isbn)
                .build();
    }
}
