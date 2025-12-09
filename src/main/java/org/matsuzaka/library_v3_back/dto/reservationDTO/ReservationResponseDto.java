package org.matsuzaka.library_v3_back.dto.reservationDTO;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReservationResponseDto {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String imageUrl;
    private Long bookCopyId;
    private String status; // Enum string
    private Integer queuePosition;
    private LocalDateTime reserveDate;
    private LocalDate expirationDate;
}

