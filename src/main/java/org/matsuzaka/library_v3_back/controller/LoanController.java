package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
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
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    /**
     * 處理書籍借閱請求 (Admin).
     * @param requestDto 包含 uniqueCode 和 cardId 的借閱請求 DTO
     * @return 借閱操作的結果
     */
    @PostMapping("/borrow")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") 
    public ResponseEntity<ApiResponse<BorrowRespDto>> borrowBook(@RequestBody BorrowRequestDto requestDto) {
        BorrowRespDto result = loanService.borrowBook(requestDto.getUniqueCode(), requestDto.getCardId());
        return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
    }

    /**
     * 處理書籍歸還請求 (Admin).
     * @param requestDto 包含 uniqueCode 的歸還請求 DTO
     * @return 歸還操作的結果
     */
    @PostMapping("/return")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ReturnResponseDto>> returnBook(@RequestBody ReturnRequestDto requestDto) {
        ReturnResponseDto result = loanService.returnBook(requestDto.getUniqueCode());
        return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
    }

    /* 續借 */
    @PutMapping("/{loanId}/renew")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RenewResponseDto>> renewBook(
            @PathVariable Long loanId,
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        RenewResponseDto response = loanService.renewBook(loanId, currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }





    /**
     * 【核心修改 1：實現安全的 "my" 端點】
     * 原本的 /current/{userId} 改為 /my-current。
     * @param currentUser 由 Spring Security 從有效的 JWT 中解析並安全注入的使用者物件。
     * @return 當前登入者的借閱中書籍列表。
     */
    @GetMapping("/my-current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Set<LoanItemRespDto>>> getCurrentLoans(
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId();
        Set<LoanItemRespDto> loans = loanService.getCurrentByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LoanItemRespDto>>> getLoanHistory(
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId();
        List<LoanItemRespDto> history = loanService.getHistoryByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/my-overdue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LoanItemRespDto>>> getOverdueLoans(
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId();
        List<LoanItemRespDto> overdue = loanService.getOverdueByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(overdue));
    }
}