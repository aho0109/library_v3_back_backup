package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.loanDTO.*;
import org.matsuzaka.library_v3_back.security.UserDetailSecu;
import org.matsuzaka.library_v3_back.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/loans") // 統一的 loans API 路徑
@CrossOrigin(origins = "*")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    /* 借閱 */
    /**
     * 處理書籍借閱請求 (Admin).
     * @param requestDto 包含 uniqueCode 和 cardId 的借閱請求 DTO
     * @return 借閱操作的結果
     */
    @PostMapping("/borrow")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") 
    public ResponseEntity<BorrowRespDto> borrowBook(@RequestBody BorrowRequestDto requestDto) {
        BorrowRespDto response = loanService.borrowBook(requestDto.getUniqueCode(), requestDto.getCardId());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /* 歸還 */
    /**
     * 處理書籍歸還請求 (Admin).
     * @param requestDto 包含 uniqueCode 的歸還請求 DTO
     * @return 歸還操作的結果
     */
    @PostMapping("/return")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ReturnResponseDto> returnBook(@RequestBody ReturnRequestDto requestDto) {
        ReturnResponseDto response = loanService.returnBook(requestDto.getUniqueCode());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /* 續借 */
    @PostMapping("/{loanId}/renew")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> renewBook(@PathVariable Long loanId, @AuthenticationPrincipal UserDetailSecu currentUser) {
        try {
            loanService.renewBook(loanId, currentUser.getUser().getId());
            return ResponseEntity.ok("Renew successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }





    /**
     * 【核心修改 1：實現安全的 "my" 端點】
     * 將原本的 /current/{userId} 改為 /my-current。
     * "my" 這個詞清楚地表明了這是獲取「當前登入者自己」的資料。
     *
     * @param currentUser 由 Spring Security 從有效的 JWT 中解析並安全注入的使用者物件。
     * @return 當前登入者的借閱中書籍列表。
     */
    @GetMapping("/my-current")
    @PreAuthorize("isAuthenticated() and (hasAuthority('ROLE_ADMIN'))") // 可根據需求調整權限
    public ResponseEntity<Set<LoanItemRespDto>> getCurrentLoans(@AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId(); // 從安全的物件中獲取 ID
        Set<LoanItemRespDto> loans = loanService.getCurrentByUserId(userId);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated() and (hasAuthority('ROLE_ADMIN'))") // 可根據需求調整權限
    public ResponseEntity<List<LoanItemRespDto>> getLoanHistory(@AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId(); // 從安全的物件中獲取 ID
        List<LoanItemRespDto> history = loanService.getHistoryByUserId(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/my-overdue")
    @PreAuthorize("isAuthenticated() and (hasAuthority('ROLE_ADMIN'))") // 可根據需求調整權限
    public ResponseEntity<List<LoanItemRespDto>> getOverdueLoans(@AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId(); // 從安全的物件中獲取 ID
        List<LoanItemRespDto> overdue = loanService.getOverdueByUserId(userId);
        return ResponseEntity.ok(overdue);
    }
}