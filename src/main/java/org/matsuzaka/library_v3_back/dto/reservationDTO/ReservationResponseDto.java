package org.matsuzaka.library_v3_back.dto.reservationDTO;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class ReservationResponseDto {
    private Long id;
    private Long bookId;
    private String title;
    private String imageUrl;
    private Long bookCopyId;
    private String status; // Enum string
    private Integer queuePosition;
    private LocalDateTime reserveDate;
    private LocalDate expirationDate;

    private String uniqueCode;
    private Set<String> authors;
    private LocalDate notifyDate;
    private LocalDate pickupDate;

}

