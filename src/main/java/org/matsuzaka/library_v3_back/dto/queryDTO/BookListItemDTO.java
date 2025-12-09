package org.matsuzaka.library_v3_back.dto.queryDTO;

import lombok.Data;
import org.matsuzaka.library_v3_back.model.entity.Author;
import org.matsuzaka.library_v3_back.model.entity.Book;
import org.matsuzaka.library_v3_back.model.entity.Tag;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 書籍列表項目的 DTO。
 * 用於在書籍搜尋結果頁面中顯示單本書籍的簡要資訊。
 * 避免直接暴露 JPA Entity，並將複雜的關聯物件扁平化為簡單的屬性。
 */
@Data
public class BookListItemDTO {

    // 書籍的唯一識別ID
    private Long id;

    // 書籍標題
    private String title;

    // 書籍封面圖片的URL
    private String imageUrl;

    // 書籍是否為系列作，true 表示為系列作，false 表示為單行本
    // 注意：這裡的命名規則是 isXxx，Lombok 會自動生成 isXxx() 的 getter 方法
    // 前端改，或是這裏寫 @JsonProperty("isSeries") 也可以
    private boolean isSeries;

    // 系列作ID，如果不是系列作則為 null
    private Long seriesId;

    // 系列作標題，如果不是系列作則為 null
    private String seriesTitle;

    // 作者名稱列表，用於顯示多個作者
    private List<String> authors;

    // 出版社名稱
    private String publisherName;

    // 標籤名稱列表，用於顯示多個標籤
    private List<String> tags;

    // 是否可借閱，僅針對單行本顯示
    private boolean availableForLoan;

    // 顯示內容類型，"可借閱"、"不可借閱"或"點擊查看"
    private String displayType;

    // 管理員專用欄位 - 副本統計資訊
    private Integer totalCopies;      // 總副本數
    private Integer availableCopies;  // 可借閱副本數
    private String isbn;              // ISBN（管理員查看需要）
    private Short publishYear;        // 出版年份（管理員查看需要）
    private Boolean representative;   // 是否為代表作（管理員查看需要）
    private String mainCategoryTitle; // 主分類名稱（管理員查看需要）
    private String subCategoryTitle;  // 子分類名稱（管理員查看需要）

    /**
     * 靜態方法，用於從 JPA Book Entity 轉換為 BookListItemDTO。
     * 支持舊版本接口，不指定seriesDisplay時默認為顯示所有書籍模式
     *
     * @param book Entity 層的 Book 物件
     * @param isAvailable 書籍是否可借���
     * @return 轉換後的 BookListItemDTO 物件
     */
    public static BookListItemDTO fromEntity(Book book, boolean isAvailable) {
        // 默認為顯示所有書��模式 (seriesDisplay = 0)
        return fromEntity(book, isAvailable, 0);
    }

    /**
     * 靜態方法，用於從 JPA Book Entity 轉換為 BookListItemDTO。
     * 根據系列顯示模式和書籍類型決定正確的顯示內容。
     *
     * @param book Entity 層的 Book 物件
     * @param isAvailable 書籍是否可借閱
     * @param seriesDisplay 系列顯示模式，1表示僅顯示代表作，0表示顯示所有書籍
     * @return 轉換後的 BookListItemDTO 物件
     */
    public static BookListItemDTO fromEntity(Book book, boolean isAvailable, Integer seriesDisplay) {
        BookListItemDTO dto = new BookListItemDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setImageUrl(book.getImageUrl());

        // 判斷是否為系列作，並設定對應的 seriesTitle
        boolean isSeries = book.getSeries() != null;
        //dto.setSeries(isSeries);
        dto.isSeries = isSeries;
        // 結論：後端 isXxx 開頭的屬性在json序列化時會被去掉 is 前綴，變成 xxx
        // 媽的害我搞一整天debug，跟空氣搏鬥

        if (isSeries) {
            dto.setSeriesTitle(book.getSeries().getTitle());
            // 設定系列ID
            dto.setSeriesId(book.getSeries().getId());

            // 系列顯示模式為1（顯示系列代表作）時，系列作顯示"點擊查看"
            if (seriesDisplay != null && seriesDisplay == 1) {
                dto.setDisplayType("DTO點擊查看");
            } else {
                // 顯示所有書籍模式下，系列作也根據可借閱狀態顯示
                dto.setDisplayType(isAvailable ? "DTO單集可借閱" : "DTO單集不可借閱");
            }
            System.out.println("安安 isSeries 的結果" + isSeries);
            System.out.println(dto.seriesTitle);
            System.out.println(dto.displayType);
        } else {
            // 單行本根據可借閱狀態設置顯示類型
            dto.setDisplayType(isAvailable ? "可借閱" : "不可借閱");
        }

        // 使用 Stream API 提取作者名稱
        if (book.getAuthors() != null) {
            dto.setAuthors(book.getAuthors().stream()
                    .map(Author::getName)
                    .collect(Collectors.toList()));
        }

        // 提取出版社名稱
        if (book.getPublisher() != null) {
            dto.setPublisherName(book.getPublisher().getPubName());
        }

        // 使用 Stream API 提取標籤名稱
        if (book.getTags() != null) {
            dto.setTags(book.getTags().stream()
                    .map(Tag::getTitle)
                    .collect(Collectors.toList()));
        }

        // 設定可借閱狀態
        dto.setAvailableForLoan(isAvailable);

        // 設定基本資訊（從 Book 實體直接獲取）
        dto.setIsbn(book.getIsbn());
        dto.setPublishYear(book.getPublishYear());
        dto.setRepresentative(book.getRepresentative());

        // 設定分類資訊
        if (book.getCategorySub() != null) {
            dto.setSubCategoryTitle(book.getCategorySub().getCategorySubTitle());
            if (book.getCategorySub().getCategory() != null) {
                dto.setMainCategoryTitle(book.getCategorySub().getCategory().getCategoryTitle());
            }
        }

        return dto;
    }
}

// Lombok 生成方法的規則
// 1. @Data 註解會生成以下方法：
//      所有字段的 getter 方法
//      所有非 final 字段的 setter 方法
//      equals()、hashCode() 和 toString() 方法
//      一個無參構造函數
// 2. 布爾屬性的特殊命名規則：
//      對於名為 isSeries 的布爾屬性，Lombok 會生成：
//      名為 isSeries() 的 getter 方法（而不是 getIsSeries()）
//      名為 setSeries(boolean) 的 setter 方法（移除 "is" 前綴）
// 3. JSON 序列化的行為：
//      當使用 Jackson（Spring Boot 的默認 JSON 庫）進行序列化時：
//      預設會使用 Java Bean 規範處理屬性
//      對於 isSeries 屬性，序列化後 JSON 中的屬性名為 series（去掉 "is" 前綴）
//      這是因為 Jackson 會識別 isSeries() 方法為布爾屬性的標準 getter，並將屬性名轉換為 series
