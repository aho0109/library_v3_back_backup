package org.matsuzaka.library_v3_back.service;

import org.matsuzaka.library_v3_back.dto.adminDTO.CreateBookDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.BookSearchParamsDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.BookSearchResponseDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.queryOneDTO.BookRespDtoOneDetails;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {

    /**
     * 搜尋與篩選書籍，並同時獲取分面統計資料。
     * 將原本的 searchAndFilterBooks 和 getFacetedSearchStats 方法合併，以減少 API 請求次數，提升效能和使用者體驗。
     * @param keyword          搜尋關鍵字
     * @param mainCategoryId   主分類ID
     * @param subCategoryId    子分類ID
     * @param seriesDisplay    系列作顯示模式
     * @param authorId         作者ID
     * @param publisherId      出版社ID
     * @param tagIds           標籤ID列表
     * @param seriesId         系列ID
     * @param pageable         分頁和排序資訊
     * @return 包含書籍列表和分面統計資料的複合DTO
     */
    BookSearchResponseDTO searchAndFilterAndGetStats(String keyword, Long mainCategoryId, Long subCategoryId, Integer seriesDisplay,
                                                     Long authorId, Long publisherId, List<Long> tagIds, Long seriesId,
                                                     String authorKeyword,
                                                     String publisherKeyword,
                                                     String bookTitleKeyword,
                                                     Short publishYear,
                                                     String isbn,
                                                     Pageable pageable);


    BookSearchResponseDTO searchAndFilterBooks(BookSearchParamsDTO params, Pageable pageable);


    /**
     * 建立一本新書，包含所有複雜的關聯和業務邏輯
     * @Transactional 確保所有資料庫操作要麼全部成功，要麼全部失敗，保證資料一致性
     */
    BookRespDtoOneDetails createBook(CreateBookDTO dto);

    /**
     * 更新書籍資訊
     */
    BookRespDtoOneDetails updateBook(Long id, CreateBookDTO dto);

    /**
     * 刪除書籍
     */
    void deleteBook(Long id);

    /**
     * 根據 ID 查詢書籍詳細資訊（管理員用）
     */
    BookRespDtoOneDetails getBookById(Long id);

    }
