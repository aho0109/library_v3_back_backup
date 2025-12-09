package org.matsuzaka.library_v3_back.controller;


import org.matsuzaka.library_v3_back.dto.loanDTO.ReturnRequestDto;
import org.matsuzaka.library_v3_back.dto.loanDTO.ReturnResponseDto;
import org.matsuzaka.library_v3_back.dto.queryDTO.BookCopyDTO;
import org.matsuzaka.library_v3_back.service.BookCopyService;
import org.springframework.http.ResponseEntity;
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
public class AdminBookCopyController {

    private final BookCopyService bookCopyService;

    public AdminBookCopyController(BookCopyService bookCopyService) {
        this.bookCopyService = bookCopyService;
    }

    /**
     * 查詢指定書籍的所有副本
     *
     * @param bookId 書籍ID
     * @return 副本列表
     */
    @GetMapping("/books/{bookId}/copies")
    public ResponseEntity<List<BookCopyDTO>> getBookCopies(@PathVariable Long bookId) {
        List<BookCopyDTO> copies = bookCopyService.getBookCopies(bookId);
        return ResponseEntity.ok(copies);
    }

    /**
     * 刪除書籍副本
     *
     * @param copyId 副本ID
     * @return 操作結果
     */
    @DeleteMapping("/book-copies/{copyId}")
    public ResponseEntity<String> deleteBookCopy(@PathVariable Long copyId) {
        bookCopyService.deleteBookCopy(copyId);
        return ResponseEntity.ok("副本刪除成功");
    }
}
