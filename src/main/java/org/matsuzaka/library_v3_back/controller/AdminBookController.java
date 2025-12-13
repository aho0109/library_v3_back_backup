package org.matsuzaka.library_v3_back.controller;


import org.matsuzaka.library_v3_back.dto.adminDTO.CreateBookCopyDTO;
import org.matsuzaka.library_v3_back.dto.adminDTO.CreateBookDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.BookSearchParamsDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.BookSearchResponseDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.queryOneDTO.BookRespDtoOneDetails;
import org.matsuzaka.library_v3_back.service.BookService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理員書籍管理控制器
 *
 * 職責說明:
 * 1. 提供管理員專用的書籍查詢和管理功能
 * 2. 支援更豐富的搜尋條件，包括模糊搜尋
 * 3. 提供書籍副本數量統計
 * 4. 處理管理員操作的權限控制
 */
@RestController
@RequestMapping("/api/admin/books")
@CrossOrigin(origins = "*")
public class AdminBookController {

    private final BookService bookService;

    public AdminBookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 管理員書籍搜尋與多重篩選 API
     *
     * 支援比一般使用者更多的搜尋條件：
     * - 作者關鍵字模糊搜尋
     * - 出版商關鍵字模糊搜尋
     * - 書名關鍵字模糊搜尋
     * - 出版年份精確搜尋
     * - ISBN 模糊搜尋
     *
     * @param keyword 一般搜尋關鍵字
     * @param mainCategoryId 主分類ID
     * @param subCategoryId 子分類ID
     * @param seriesDisplay 系列作顯示模式
     * @param authorId 作者ID
     * @param publisherId 出版社ID
     * @param tagIds 標籤ID列表
     * @param seriesId 系列ID
     * @param authorKeyword 作者關鍵字（模糊搜尋）
     * @param publisherKeyword 出版商關鍵字（模糊搜尋）
     * @param bookTitleKeyword 書名關鍵字（模糊搜尋）
     * @param publishYear 出版年份
     * @param isbn ISBN（模糊搜尋）
     * @param pageable 分頁資訊
     * @return 包含書籍列表、分頁資訊和統計資料的回應
     */
    @GetMapping
    public ResponseEntity<BookSearchResponseDTO> searchBooksForAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long mainCategoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) Integer seriesDisplay,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long publisherId,
            @RequestParam(name = "tags", required = false) List<Long> tagIds,
            @RequestParam(required = false) Long seriesId,
            // 管理員專用搜尋參數
            @RequestParam(required = false) String authorKeyword,
            @RequestParam(required = false) String publisherKeyword,
            @RequestParam(required = false) String bookTitleKeyword,
            @RequestParam(required = false) Short publishYear,
            @RequestParam(required = false) String isbn,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        // 構建管理員專用的搜尋參數
        BookSearchParamsDTO params = BookSearchParamsDTO.from(
                keyword, mainCategoryId, subCategoryId, seriesDisplay,
                authorId, publisherId, tagIds, seriesId,
                authorKeyword, publisherKeyword, bookTitleKeyword,
                publishYear, isbn
        );

        // 執行搜尋並返回結果
        BookSearchResponseDTO response = bookService.searchAndFilterBooks(params, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 建立一本新書，包含所有複雜的關聯和業務邏輯
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BookRespDtoOneDetails> createBook(@RequestBody CreateBookDTO createBookDTO) {
        BookRespDtoOneDetails createdBookDto = bookService.createBook(createBookDTO);
        return new ResponseEntity<>(createdBookDto, HttpStatus.CREATED);
    }

    /**
     * 更新書籍資訊
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BookRespDtoOneDetails> updateBook(
            @PathVariable Long id,
            @RequestBody CreateBookDTO updateBookDTO) {
        BookRespDtoOneDetails updatedBook = bookService.updateBook(id, updateBookDTO);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * 刪除書籍
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok("書籍刪除成功");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 根據 ID 查詢書籍詳細資訊
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BookRespDtoOneDetails> getBookById(@PathVariable Long id) {
        BookRespDtoOneDetails book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    /**
     * 為指定書籍新增副本
     */
    @PostMapping("/{bookId}/copies")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> addBookCopy(
            @PathVariable Long bookId,
            @RequestBody CreateBookCopyDTO copyDTO) {
        bookService.addBookCopy(bookId, copyDTO);
        return new ResponseEntity<>("副本新增成功", HttpStatus.CREATED);
    }
}
