package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
import org.matsuzaka.library_v3_back.dto.queryDTO.BookCopyDTO;
import org.matsuzaka.library_v3_back.service.BookCopyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理員書籍副本管理控制器
 *
 * 職責說明:
 * 1. 提供管理員專用的書籍副本查詢和管理功能
 * 2. 支援副本的增刪改查操作
 * 3. 提供強制借出和歸還功能
 */
@RestController
@RequestMapping("/api/admin") // url 基本上延續 AdminBookController
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminBookCopyController {

    private final BookCopyService bookCopyService;

    public AdminBookCopyController(BookCopyService bookCopyService) {
        this.bookCopyService = bookCopyService;
    }

    /**
     * 查詢指定書籍的所有副本
     * @param bookId 書籍ID
     */
    @GetMapping("/books/{bookId}/copies")
    public ResponseEntity<ApiResponse<List<BookCopyDTO>>> getBookCopies(@PathVariable Long bookId) {
        List<BookCopyDTO> copies = bookCopyService.getBookCopies(bookId);
        return ResponseEntity.ok(ApiResponse.success(copies));
    }

    /**
     * 刪除書籍副本
     * @param copyId 副本ID
     */
    @DeleteMapping("/book-copies/{copyId}")
    public ResponseEntity<ApiResponse<Void>> deleteBookCopy(@PathVariable Long copyId) {
        bookCopyService.deleteBookCopy(copyId);
        return ResponseEntity.ok(ApiResponse.success("副本刪除成功"));
    }

    /**
     * 查詢副本借閱記錄
     * @param copyId 副本ID
     */
    @GetMapping("/book-copies/{copyId}/loan-history")
    public ResponseEntity<ApiResponse<List<?>>> getBookCopyLoanHistory(@PathVariable Long copyId) {
        List<?> history = bookCopyService.getBookCopyLoanHistory(copyId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * 查詢副本預約記錄
     * @param copyId 副本ID
     */
    @GetMapping("/book-copies/{copyId}/reservation-history")
    public ResponseEntity<ApiResponse<List<?>>> getBookCopyReservationHistory(@PathVariable Long copyId) {
        List<?> history = bookCopyService.getBookCopyReservationHistory(copyId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
