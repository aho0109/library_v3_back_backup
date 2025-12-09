package org.matsuzaka.library_v3_back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


// 單純用於首頁顯示各種前五，借閱前五，最新前五
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookTop5DTO {

    // 書籍的唯一識別ID
    private Long id;

    // 書籍標題
    private String title;

    // 書籍封面圖片的URL
    private String imageUrl;

    // 作者名稱列表，用於顯示多個作者
    private List<String> authors;

    // 出版社名稱
    private String publisherName;

}
