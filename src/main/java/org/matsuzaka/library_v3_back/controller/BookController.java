package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.queryDTO.BookListItemDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.BookSearchResponseDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.queryOneDTO.BookRespDtoOneDetails;
import org.matsuzaka.library_v3_back.service.BookService;
import org.matsuzaka.library_v3_back.service.BookServiceBasic;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;
    private final BookServiceBasic bookServiceBasic;

    public BookController(BookService bookService,
                          BookServiceBasic bookServiceBasic) {
        this.bookService = bookService;
        this.bookServiceBasic = bookServiceBasic;
    }

    /**
     * GET /api/books
     * 書籍搜尋與多重篩選 API。
     * * @param keyword          搜尋關鍵字
     * @param mainCategoryId   主分類ID
     * @param subCategoryId    子分類ID
     * @param seriesDisplay    系列作顯示模式 (1:顯示系列作, 0:只顯示單行本)
     * @param authorId         作者ID
     * @param publisherId      出版社ID
     * @param tagIds           標籤ID列表
     * @param seriesId         系列ID
     * @param pageable         分頁資訊，由 Spring 自動解析 URL 參數 (e.g., ?page=0&size=10&sort=title,asc)
     * @return 包含書籍列表和分頁資訊的 HTTP 200 OK 回應
     */
    @GetMapping
    public ResponseEntity<BookSearchResponseDTO> searchBooksAndStats( // Page 改成 PageResponseDTO<，因spring boot建議
                                                                      @RequestParam(required = false) String keyword,
                                                                      @RequestParam(required = false) Long mainCategoryId,
                                                                      @RequestParam(required = false) Long subCategoryId,
                                                                      @RequestParam(required = false/*, defaultValue = "1"*/) Integer seriesDisplay,
                                                                      @RequestParam(required = false) Long authorId,
                                                                      @RequestParam(required = false) Long publisherId,
                                                                      @RequestParam(name = "tags", required = false) List<Long> tagIds,
                                                                      @RequestParam(required = false) Long seriesId,
                                                                      //@PageableDefault: 這是 Spring Data 的一個強大功能。
                                                                      // 它會自動從 URL 參數（例如 ?page=0&size=10&sort=title,asc）中解析出分頁和排序資訊，並將其封裝成 Pageable 物件，極大地簡化了分頁的處理。
                                                                      // 這裡的 pageable 參數會自動處理分頁和排序，默認每頁20條，按標題升序排列
                                                                      @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        // 將所有參數傳遞給服務層，執行業務邏輯
        BookSearchResponseDTO response = bookService.searchAndFilterAndGetStats(
                keyword, mainCategoryId, subCategoryId, seriesDisplay, authorId, publisherId, tagIds, seriesId,
                null, // authorKeyword
                null, // publisherKeyword
                null, // bookTitleKeyword
                null, // publishYear
                null, // isbn
                pageable);

        // 返回包含分頁資料的 HTTP OK 回應
        //return ResponseEntity.ok(books);
//        PageResponseDTO<BookListItemDTO> response = PageResponseDTO.fromPage(booksPage);
        return ResponseEntity.ok(response);
    }


    /* 首頁，各種預設熱門查詢 */

    /**
     * 查詢借閱前五名。
     * @return 所有書籍列表
     */
    @GetMapping({"/home/top5loan", "/home/top5loan/{categoryId}"})
    public List<BookListItemDTO> getTop5Loan(@PathVariable(value = "categoryId", required = false) Long categoryId) {
        return bookServiceBasic.getTop5Loan(categoryId);
    }

    /**
     * 查詢最新前五名。
     * @param categoryId 分類ID
     * @return 最新上架前五名的書籍列表
     */
    @GetMapping({"/home/top5new", "/home/top5new/{categoryId}"})
    public List<BookListItemDTO> getTop5New(@PathVariable(value = "categoryId", required = false) Long categoryId) {
        return bookServiceBasic.getTop5New(categoryId);
    }


    /**
     * 根據書籍ID查詢詳細資訊(for 讀者端)。
     * 使用 JOIN FETCH 來避免 N+1 問題，確保在查詢書籍時，同時載入相關的作者、出版社、系列、分類子項、分類、書籍副本和標籤等關聯實體。
     * 參與到的table有：book, author, publisher, series, category, categorySub, bookCopy, tag
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookRespDtoOneDetails> getOneByIdWithDetails(@PathVariable("id") Long id) {
        return bookServiceBasic.getOneByIdWithDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
