package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.queryDTO.BookListItemDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.BookSearchResponseDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.queryOneDTO.BookRespDtoOneDetails;
import org.matsuzaka.library_v3_back.dto.reviewDTO.LikeStatusDto;
import org.matsuzaka.library_v3_back.dto.reviewDTO.ReviewRequestDto;
import org.matsuzaka.library_v3_back.dto.reviewDTO.ReviewResponseDto;
import org.matsuzaka.library_v3_back.security.UserDetailSecu;
import org.matsuzaka.library_v3_back.service.BookService;
import org.matsuzaka.library_v3_back.service.BookServiceBasic;
import org.matsuzaka.library_v3_back.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;
    private final BookServiceBasic bookServiceBasic;
    private final ReviewService reviewService;

    public BookController(BookService bookService,
                          BookServiceBasic bookServiceBasic,
                          ReviewService reviewService) {
        this.bookService = bookService;
        this.bookServiceBasic = bookServiceBasic;
        this.reviewService = reviewService;
    }

    /**
     * GET /api/books
     * 書籍搜尋與多重篩選 API。
     * 
     * @param keyword          搜尋關鍵字
     * @param mainCategoryId   主分類ID
     * @param subCategoryId    子分類ID
     * @param seriesDisplay    系列作顯示模式 (1:顯示系列作, 0:只顯示單行本)
     * @param authorId         作者ID
     * @param publisherId      出版社ID
     * @param tagIds           標籤ID列表
     * @param seriesId         系列ID
     * @param pageable         分頁資訊，由 Spring 自動解析 URL 參數
     *                         - page: 頁碼（從0開始），例如 ?page=0
     *                         - size: 每頁數量，例如 ?size=20
     *                         - sort: 排序欄位和方向，例如：
     *                           ?sort=addedDate,desc (上架日期新到舊)
     *                           ?sort=addedDate,asc (上架日期舊到新)
     *                           ?sort=totalLoanCount,desc (熱門借閱)
     *                           ?sort=totalLoanCount,asc (冷門借閱)
     *                           ?sort=title,asc (書名A-Z)
     * @return 包含書籍列表和分頁資訊的 HTTP 200 OK 回應
     */
    @GetMapping
    public ResponseEntity<BookSearchResponseDTO> searchBooksAndStats(
                                                                      @RequestParam(required = false) String keyword,
                                                                      @RequestParam(required = false) Long mainCategoryId,
                                                                      @RequestParam(required = false) Long subCategoryId,
                                                                      @RequestParam(required = false) Integer seriesDisplay,
                                                                      @RequestParam(required = false) Long authorId,
                                                                      @RequestParam(required = false) Long publisherId,
                                                                      @RequestParam(name = "tags", required = false) List<Long> tagIds,
                                                                      @RequestParam(required = false) Long seriesId,
                                                                      // 預設按上架日期降序排列（新書優先）
                                                                      @PageableDefault(size = 10, sort = "addedDate", direction = Sort.Direction.DESC) Pageable pageable) {

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

    // Review Endpoints

    /**
     * 新增書籍評論
     * @param bookId
     * @param currentUser
     * @param request
     * @return
     */
    @PostMapping("/{bookId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponseDto> addReview(@PathVariable Long bookId,
                                                       @AuthenticationPrincipal UserDetailSecu currentUser,
                                                       @RequestBody ReviewRequestDto request) {
        return ResponseEntity.ok(reviewService.addReview(currentUser.getUser().getId(), bookId, request));
    }

    /**
     * 編輯書籍評論
     * @param bookId
     * @param reviewId
     * @param currentUser
     * @param request
     * @return
     */
    @PutMapping("/{bookId}/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponseDto> updateReview(@PathVariable Long bookId,
                                                          @PathVariable Long reviewId,
                                                          @AuthenticationPrincipal UserDetailSecu currentUser,
                                                          @RequestBody ReviewRequestDto request) {
        return ResponseEntity.ok(reviewService.updateReview(currentUser.getUser().getId(), reviewId, request));
    }

    /**
     * 刪除書籍評論
     * @param bookId
     * @param reviewId
     * @param currentUser
     * @return
     */
    @DeleteMapping("/{bookId}/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteReview(@PathVariable Long bookId,
                                          @PathVariable Long reviewId,
                                          @AuthenticationPrincipal UserDetailSecu currentUser) {
        reviewService.deleteReview(currentUser.getUser().getId(), reviewId);
        return ResponseEntity.ok("評論刪除成功");
    }

    /**
     * 取得書籍評論列表
     * @param bookId
     * @param currentUser
     * @param pageable
     * @return
     */
    @GetMapping("/{bookId}/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getReviews(@PathVariable Long bookId,
                                                              @AuthenticationPrincipal UserDetailSecu currentUser, // Optional
                                                              Pageable pageable) {
        Long userId = currentUser != null ? currentUser.getUser().getId() : null;
        return ResponseEntity.ok(reviewService.getReviewsByBookId(bookId, userId, pageable));
    }

    /**
     * 書籍評論按讚/取消讚（Toggle）
     * @param bookId
     * @param reviewId
     * @param currentUser
     * @return
     */
    @PostMapping("/{bookId}/reviews/{reviewId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LikeStatusDto> toggleLikeReview(@PathVariable Long bookId,
                                                           @PathVariable Long reviewId,
                                                           @AuthenticationPrincipal UserDetailSecu currentUser) {
        boolean liked = reviewService.toggleLikeReview(currentUser.getUser().getId(), reviewId);
        return ResponseEntity.ok(new LikeStatusDto(liked));
    }

    @DeleteMapping("/{bookId}/reviews/{reviewId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> unlikeReview(@PathVariable Long bookId,
                                          @PathVariable Long reviewId,
                                          @AuthenticationPrincipal UserDetailSecu currentUser) {
        reviewService.unlikeReview(currentUser.getUser().getId(), reviewId);
        return ResponseEntity.ok().build();
    }
}
