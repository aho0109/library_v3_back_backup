package org.matsuzaka.library_v3_back.controller;

import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
import org.matsuzaka.library_v3_back.dto.loanDTO.LoanItemRespDto;
import org.matsuzaka.library_v3_back.dto.reservationDTO.ReservationResponseDto;
import org.matsuzaka.library_v3_back.dto.userDTO.UserBriefDTO;
import org.matsuzaka.library_v3_back.dto.userDTO.UserDetailRespDto;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.enums.Role;
import org.matsuzaka.library_v3_back.model.repositoryDao.LoanRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.UserRepository;
import org.matsuzaka.library_v3_back.service.LoanService;
import org.matsuzaka.library_v3_back.service.ReservationService;
import org.matsuzaka.library_v3_back.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理員會員管理控制器
 */
@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final LoanRepository loanRepository;
    private final LoanService loanService;
    private final ReservationService reservationService;

    public AdminUserController(UserRepository userRepository,
                               UserService userService,
                               LoanRepository loanRepository,
                               LoanService loanService,
                               ReservationService reservationService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.loanRepository = loanRepository;
        this.loanService = loanService;
        this.reservationService = reservationService;
    }

    /**
     * 根據 cardId 查詢使用者基本資訊（借還書用）
     */
    // TODO: 整理業務邏輯，移到 service
    @GetMapping("/by-card/{cardId}")
    public ResponseEntity<ApiResponse<UserBriefDTO>> getUserByCardId(@PathVariable String cardId) {
        User user = userRepository.findByCardId(cardId)
                .orElseThrow(() -> new org.matsuzaka.library_v3_back.exception.ResourceNotFoundException(
                    org.matsuzaka.library_v3_back.exception.ErrorCode.USER_NOT_FOUND,
                    "借書證號: " + cardId));

        // 計算目前借閱數量
        long currentLoansCount = loanRepository.countByUserIdAndStatus(
                user.getId(), 
                org.matsuzaka.library_v3_back.model.enums.LoanStatus.ON_LOAN
        );

        // 計算最大借閱額度
        int maxLoansAllowed = user.getRole() == Role.ROLE_CITIZEN ? 10 : 5;

        UserBriefDTO dto = new UserBriefDTO();
        dto.setId(user.getId());
        dto.setCardId(user.getCardId());
        dto.setName(user.getUserDetail() != null ? user.getUserDetail().getName() : "未知");
        dto.setAccount(user.getAccount());
        dto.setRole(user.getRole().name());
        dto.setStatus(user.getStatus().name());
        dto.setPenaltyPoints(user.getPenaltyPoints());
        dto.setCurrentLoansCount((int) currentLoansCount);
        dto.setMaxLoansAllowed(maxLoansAllowed);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**
     * 會員搜尋（支援多個條件）
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserDetailRespDto>>> searchUsers(
            @RequestParam(required = false) String cardId,
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {
        
        List<UserDetailRespDto> users = userService.searchUsers(cardId, account, name, email, phone);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * 開通帳號
     */
    @PutMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long userId) {
        userService.activateUser(userId);
        return ResponseEntity.ok(ApiResponse.success("帳號開通成功"));
    }

    /**
     * 停權帳號
     */
    @PutMapping("/{userId}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendUser(@PathVariable Long userId) {
        userService.suspendUser(userId);
        return ResponseEntity.ok(ApiResponse.success("帳號已停權"));
    }

    /**
     * 復權帳號
     */
    @PutMapping("/{userId}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreUser(@PathVariable Long userId) {
        userService.restoreUser(userId);
        return ResponseEntity.ok(ApiResponse.success("帳號已復權"));
    }

    /**
     * 查詢會員借閱記錄
     */
    @GetMapping("/{userId}/loans")
    public ResponseEntity<ApiResponse<List<LoanItemRespDto>>> getUserLoans(@PathVariable Long userId) {
        List<LoanItemRespDto> loans = loanService.getHistoryByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    /**
     * 查詢會員預約記錄
     */
    @GetMapping("/{userId}/reservations")
    public ResponseEntity<ApiResponse<List<ReservationResponseDto>>> getUserReservations(@PathVariable Long userId) {
        List<ReservationResponseDto> reservations = reservationService.getUserReservationsAdmin(userId);
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

    /**
     * 查詢會員詳細資訊
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailRespDto> getUserDetail(@PathVariable Long userId) {
        UserDetailRespDto user = userService.getUserDetailById(userId);
        return ResponseEntity.ok(user);
    }
}

