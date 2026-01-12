package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
import org.matsuzaka.library_v3_back.dto.reservationDTO.ReservationRequestDto;
import org.matsuzaka.library_v3_back.dto.reservationDTO.ReservationResponseDto;
import org.matsuzaka.library_v3_back.security.UserDetailSecu;
import org.matsuzaka.library_v3_back.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
@PreAuthorize("isAuthenticated()")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * 預約書籍副本
     * @param currentUser 由 Spring Security 從有效的 JWT 中解析並安全注入的使用者物件。
     * @param request 包含 bookCopyId 的預約請求 DTO
     * @return 預約操作的結果
     */
    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<Void>> reserveBookCopy(
            @AuthenticationPrincipal UserDetailSecu currentUser,
            @RequestBody ReservationRequestDto request) {
        reservationService.reserveBookCopy(currentUser.getUser().getId(), request.getBookCopyId());
        return ResponseEntity.ok(ApiResponse.success("預約成功"));
    }

    /**
     * 取消預約書籍副本
     * @param currentUser 由 Spring Security 從有效的 JWT 中解析並安全注入的使用者物件。
     * @param id 要取消預約的預約 ID
     * @return 取消預約操作的結果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(
            @AuthenticationPrincipal UserDetailSecu currentUser,
            @PathVariable Long id) {
        reservationService.cancelReservation(currentUser.getUser().getId(), id);
        return ResponseEntity.ok(ApiResponse.success("取消預約成功"));
    }

    /**
     * 獲取當前使用者所有預約列表
     * @param currentUser 由 Spring Security 從有效的 JWT 中解析並安全注入的使用者物件。
     * @return 當前使用者所有預約列表
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReservationResponseDto>>> getMyReservations(
            @AuthenticationPrincipal UserDetailSecu currentUser) {
        List<ReservationResponseDto> reservations = reservationService.getUserReservations(currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }
}
