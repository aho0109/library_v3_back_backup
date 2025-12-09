package org.matsuzaka.library_v3_back.controller;

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
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reserve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> reserveBook(@AuthenticationPrincipal UserDetailSecu currentUser,
                                         @RequestBody ReservationRequestDto request) {
        try {
            reservationService.reserveBook(currentUser.getUser().getId(), request.getBookId());
            return ResponseEntity.ok("Reservation successful");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancelReservation(@AuthenticationPrincipal UserDetailSecu currentUser,
                                               @PathVariable Long id) {
        try {
            reservationService.cancelReservation(currentUser.getUser().getId(), id);
            return ResponseEntity.ok("Reservation cancelled");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservationResponseDto>> getMyReservations(@AuthenticationPrincipal UserDetailSecu currentUser) {
        return ResponseEntity.ok(reservationService.getUserReservations(currentUser.getUser().getId()));
    }
}

