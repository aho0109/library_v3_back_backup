package org.matsuzaka.library_v3_back.service;


import org.matsuzaka.library_v3_back.dto.loanDTO.BorrowRespDto;
import org.matsuzaka.library_v3_back.dto.loanDTO.LoanItemRespDto;
import org.matsuzaka.library_v3_back.dto.loanDTO.ReturnResponseDto;

import java.util.List;
import java.util.Set;

public interface LoanService {

    Set<LoanItemRespDto> getCurrentByUserId(Long userId);
    List<LoanItemRespDto> getHistoryByUserId(Long userId);
    List<LoanItemRespDto> getOverdueByUserId(Long userId);

    /* 借閱功能 */
    /**
     * Admin processes borrow (pickup).
     * @param uniqueCode Book copy unique code
     * @param userId User ID
     */
    BorrowRespDto borrowBook(String uniqueCode, Long userId);

    /* 歸還功能 */
    /**
     * Admin processes return.
     * @param uniqueCode Book copy unique code
     */
    ReturnResponseDto returnBook(String uniqueCode);
    
    /* 續借功能 */
    /**
     * User renews book.
     */
    void renewBook(Long loanId, Long userId);

}
