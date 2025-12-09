package org.matsuzaka.library_v3_back.service;


import org.matsuzaka.library_v3_back.dto.queryDTO.BookListItemDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.queryOneDTO.BookRespDtoOneDetails;

import java.util.List;
import java.util.Optional;

public interface BookServiceBasic {

    /* 首頁，各種預設熱門查詢 */

    /**
     * 查詢借閱前五名。
     * @return 所有書籍列表
     */
    List<BookListItemDTO> getTop5Loan(Long categoryId);

    /**
     * 查詢最新前五名。
     * @param categoryId 分類ID
     * @return 最新上架前五名的書籍列表
     */
    List<BookListItemDTO> getTop5New(Long categoryId);


    /**
     * 根據書籍ID查詢詳細資訊(for 讀者端)。
     * 使用 JOIN FETCH 來避免 N+1 問題，確保在查詢書籍時，同時載入相關的作者、出版社、系列、分類子項、分類、書籍副本和標籤等關聯實體。
     * 參與到的table有：book, author, publisher, series, category, categorySub, bookCopy, tag
     */
    Optional<BookRespDtoOneDetails> getOneByIdWithDetails(Long id);

}
