package org.matsuzaka.library_v3_back.service;


import org.matsuzaka.library_v3_back.dto.loanDTO.ReturnResponseDto;
import org.matsuzaka.library_v3_back.dto.queryDTO.BookCopyDTO;

import java.util.List;

/**
 * 書籍副本服務介面
 */
public interface BookCopyService {

    /**
     * 獲取指定書籍的所有副本
     * @param bookId 書籍ID
     * @return 副本列表
     */
    List<BookCopyDTO> getBookCopies(Long bookId);

    /**
     * 刪除書籍副本
     * @param copyId 副本ID
     */
    void deleteBookCopy(Long copyId);
}
