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

    @GetMapping("/current/{userId}")
    public ResponseEntity<Set<LoanItemRespDto>> getCurrentLoans(@PathVariable Long userId) {
        Set<LoanItemRespDto> loans = loanService.getCurrentByUserId(userId);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<LoanItemRespDto>> getLoanHistory(@PathVariable Long userId) {
        List<LoanItemRespDto> history = loanService.getHistoryByUserId(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/overdue/{userId}")
    public ResponseEntity<List<LoanItemRespDto>> getOverdueLoans(@PathVariable Long userId) {
        List<LoanItemRespDto> overdue = loanService.getOverdueByUserId(userId);
        return ResponseEntity.ok(overdue);
    }

    // 如果有收藏功能，新增一個 FavoritesController 或在 LoanController 中處理
    // @GetMapping("/favorites/{userId}")
    // public ResponseEntity<List<LoanItemRespDto>> getFavorites(@PathVariable Long userId) {
    //     List<LoanItemRespDto> favorites = loanService.getFavoritesByUserId(userId);
    //     return ResponseEntity.ok(favorites);
    // }



    /* 借閱 */
    /**
     * 處理書籍借閱請求。
     * 只有已認證的使用者才能執行此操作。
     * @param requestDto 包含 bookId 和 userId 的借閱請求 DTO
     * @return 借閱操作的結果
     */
    @PostMapping("/borrow")
    @PreAuthorize("isAuthenticated()") // 只有已認證的使用者才能訪問此端點
    public ResponseEntity<BorrowRespDto> borrowBook(@RequestBody BorrowRequestDto requestDto) {
        BorrowRespDto response = loanService.borrowBook(requestDto.getBookId(), requestDto.getUserId());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            // 根據業務需求，可以返回 400 Bad Request 或其他狀態碼
            System.out.println("嘗試借閱書本ID為：" + requestDto.getBookId());
            System.out.println("使用者為：" + requestDto.getUserId());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /* 歸還 */
    /**
     * 處理書籍歸還請求。
     * 只有已認證的使用者才能執行此操作。
     * @param requestDto 包含 loanId 和 userId 的歸還請求 DTO
     * @return 歸還操作的結果
     */
    @PostMapping("/return")
    @PreAuthorize("isAuthenticated()") // 只有已認證的使用者才能訪問此端點
    public ResponseEntity<ReturnResponseDto> returnBook(@RequestBody ReturnRequestDto requestDto) {
        ReturnResponseDto response = loanService.returnBook(requestDto.getLoanId(), requestDto.getUserId());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
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
    @PreAuthorize("isAuthenticated()") // 確保只有登入的使用者才能訪問
    public ResponseEntity<Set<LoanItemRespDto>> getCurrentLoans(@AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId(); // 從安全的物件中獲取 ID
        Set<LoanItemRespDto> loans = loanService.getCurrentByUserId(userId);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LoanItemRespDto>> getLoanHistory(@AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId(); // 從安全的物件中獲取 ID
        List<LoanItemRespDto> history = loanService.getHistoryByUserId(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/my-overdue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LoanItemRespDto>> getOverdueLoans(@AuthenticationPrincipal UserDetailSecu currentUser) {
        Long userId = currentUser.getUser().getId(); // 從安全的物件中獲取 ID
        List<LoanItemRespDto> overdue = loanService.getOverdueByUserId(userId);
        return ResponseEntity.ok(overdue);
    }

    /**
     * 【核心修改 2：簡化借閱請求】
     * 借閱書籍時，前端不再需要（也不應該）傳送 userId。
     * 後端會直接從 JWT 中得知是誰在進行操作。
     *
     * @param currentUser 當前登入的使用者。
     * @param requestDto 只包含 bookId 的請求 DTO。
     * @return 借閱操作的結果。
     */
    @PostMapping("/borrow0822")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BorrowRespDto> borrowBook(@AuthenticationPrincipal UserDetailSecu currentUser, @RequestBody BorrowRequestDto requestDto) {
        Long userId = currentUser.getUser().getId();
        // 呼叫 Service 層時，傳入從 JWT 中安全獲取的 userId
        BorrowRespDto response = loanService.borrowBook(requestDto.getBookId(), userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            // 根據業務需求，可以返回 400 Bad Request 或其他狀態碼
            System.out.println("嘗試借閱書本ID為：" + requestDto.getBookId());
            System.out.println("使用者為：" + userId);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 【核心修改 3：簡化歸還請求】
     * 同樣地，歸還書籍時，前端也不再需要傳送 userId。
     *
     * @param currentUser 當前登入的使用者。
     * @param requestDto 只包含 loanId 的請求 DTO。
     * @return 歸還操作的結果。
     */
    @PostMapping("/return0822")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReturnResponseDto> returnBook(@AuthenticationPrincipal UserDetailSecu currentUser, @RequestBody ReturnRequestDto requestDto) {
        Long userId = currentUser.getUser().getId();
        // 呼叫 Service 層時，傳入從 JWT 中安全獲取的 userId
        ReturnResponseDto response = loanService.returnBook(requestDto.getLoanId(), userId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }




}