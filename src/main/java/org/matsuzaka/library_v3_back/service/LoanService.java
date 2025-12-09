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
    //List<LoanItemRespDto> getFavoritesByUserId(Integer userId); // 如果你實現了收藏功能


    /* 借閱功能 */
    /**
     * 處理書籍借閱的業務邏輯。
     * 自動選擇 ID 最小的可借閱副本，更新其狀態，並創建借閱記錄。
     * @param bookId 要借閱的書籍 ID
     * @param userId 借閱者的使用者 ID
     * @return BorrowRespDto 包含操作結果
     */
    BorrowRespDto borrowBook(Long bookId, Long userId);

    /**
     * 處理書籍歸還的業務邏輯。
     * 更新借閱記錄的歸還日期，並更新書籍副本狀態為 'A' (Available)。
     * @param loanId 要歸還的借閱記錄 ID
     * @param userId 歸還者的使用者 ID (用於權限檢查)
     * @return ReturnResponseDto 包含操作結果
     */
    ReturnResponseDto returnBook(Long loanId, Long userId);

}
