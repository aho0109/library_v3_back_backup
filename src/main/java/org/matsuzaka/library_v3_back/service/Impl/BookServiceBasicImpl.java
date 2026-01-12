package org.matsuzaka.library_v3_back.service.Impl;


import org.matsuzaka.library_v3_back.dto.queryDTO.BookListItemDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.queryOneDTO.BookRespDtoOneDetails;
import org.matsuzaka.library_v3_back.exception.ResourceNotFoundException;
import org.matsuzaka.library_v3_back.model.entity.Author;
import org.matsuzaka.library_v3_back.model.entity.Book;
import org.matsuzaka.library_v3_back.model.mapper.BookMapper;
import org.matsuzaka.library_v3_back.model.repositoryDao.BookRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.LoanRepository;
import org.matsuzaka.library_v3_back.service.BookServiceBasic;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookServiceBasicImpl implements BookServiceBasic {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final BookMapper bookMapper; // 建構子注入，這是更推薦的方式，記得要在 BookServiceImpl 的建構子中注入 BookMapper


    public BookServiceBasicImpl(BookRepository bookRepository, LoanRepository loanRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.bookMapper = bookMapper;
    }

    /* 首頁，各種預設熱門查詢 */

    /**
     * 查詢借閱前五名。
     *
     * @param categoryId 分類ID
     * @return 借閱量前五名的書籍列表
     */
    @Override
    public List<BookListItemDTO> getTop5Loan(Long categoryId) {
        Pageable pageable = PageRequest.of(0, 5); // 限制查詢結果為前五名
        List<Book> top5Books = bookRepository.findTop5Loan(categoryId, pageable); // 使用自定義查詢方法獲取借閱量前五名的書籍
        return top5Books.stream().map(book -> { // map 操作將每個 Book 對象轉換為 BookListItemDTO 對象
            BookListItemDTO dto = new BookListItemDTO();
            dto.setId(book.getId());
            dto.setTitle(book.getTitle());
            dto.setImageUrl(book.getImageUrl());
            dto.setAuthors(book.getAuthors().stream()
                    .map(Author::getName) // 使用流式操作將每個 Author 對象映射為作者名稱
                    .collect(Collectors.toList()));
            dto.setPublisherName(book.getPublisher().getPubName());
            return dto; // 返回轉換後的 DTO 對象
        }).collect(Collectors.toList()); // 將所有轉換後的 DTO 對象收集到一個列表中並返回
    }

    /**
     * 查詢最新前五名。
     *
     * @param categoryId 分類ID
     * @return 最新上架前五名的書籍列表
     */
    @Override
    public List<BookListItemDTO> getTop5New(Long categoryId) {
        Pageable pageable = PageRequest.of(0, 5); // 限制查詢結果為前五名
        List<Book> top5Books = bookRepository.findTop5New(categoryId, pageable); // 使用自定義查詢方法獲取最新上架前五名的書籍
        return top5Books.stream().map(book -> { // map 操作將每個 Book 對象轉換為 BookListItemDTO 對象
            BookListItemDTO dto = new BookListItemDTO();
            dto.setId(book.getId());
            dto.setTitle(book.getTitle());
            dto.setImageUrl(book.getImageUrl());
            dto.setAuthors(book.getAuthors().stream()
                    .map(Author::getName) // 使用流式操作將每個 Author 對象映射為作者名稱
                    .collect(Collectors.toList()));
            dto.setPublisherName(book.getPublisher().getPubName());
            return dto; // 返回轉換後的 DTO 對象
        }).collect(Collectors.toList()); // 將所有轉換後的 DTO 對象收集到一個列表中並返回
    }

    /**
     * 修正版，回傳單純的 BookRespDtoOneDetails
     * 根據書籍 ID 查詢書籍詳細資訊
     */
    @Override
    public BookRespDtoOneDetails getOneByIdWithDetails(Long bookId) {
        BookRespDtoOneDetails dto = bookRepository.findOneByIdWithDetails(bookId)
                .map(bookMapper::toBookRespDtoOneDetails)
                .orElseThrow(() -> new ResourceNotFoundException(
                        org.matsuzaka.library_v3_back.exception.ErrorCode.BOOK_NOT_FOUND,
                        "書籍ID: " + bookId));

        // 補上每本副本的預計歸還日（如果該副本為「已借出」）
        if (dto.getBookCopies() != null) {
            dto.getBookCopies().forEach(copyDto -> {
                if (copyDto.getStatusDescription() != null && copyDto.getStatusDescription().contains("已借出")) {
                    loanRepository.findFirstByBookCopyIdAndReturnDateIsNullOrderByLoanDateDesc(copyDto.getId())
                            .ifPresent(loan -> {
                                if (loan.getDueDate() != null) {
                                    copyDto.setStatusDescription(copyDto.getStatusDescription() + "(" + loan.getDueDate() + " 到期)");
                                }
                            });
                }
            });
        }

        return dto;
    }


    /**
     * 舊版
     * 根據書籍ID查詢詳細資訊(for 讀者端)。
     * 使用 JOIN FETCH 來避免 N+1 問題，確保在查詢書籍時，同時載入相關的作者、出版社、系列、分類子項、分類、書籍副本和標籤等關聯實體。
     * 參與到的table有：book, author, publisher, series, category, categorySub, bookCopy, tag
     */
    // 根據書籍 ID 查詢書籍詳細資訊02
    // @EntityGraph + mapstruct(BookMapper類、BookCopyMapper類)
    @Override
    public Optional<BookRespDtoOneDetails> getOneByIdWithDetails00(Long bookId) {
        Optional<BookRespDtoOneDetails> opt = bookRepository.findOneByIdWithDetails(bookId)
                .map(bookMapper::toBookRespDtoOneDetails);

        // 補上每本副本的預計歸還日（如果該副本為「已借出」）
        opt.ifPresent(dto -> {
            if (dto.getBookCopies() != null) {
                dto.getBookCopies().forEach(copyDto -> {
                    // 判斷是否為借出狀態：依照你的 mapStatusToChinese 會產生包含「借出」字樣
                    // 也可以改為檢查 BookCopy 的 status enum (若 DTO 有該欄位)
                    if (copyDto.getStatusDescription() != null && copyDto.getStatusDescription().contains("已借出")) {
                        loanRepository.findFirstByBookCopyIdAndReturnDateIsNullOrderByLoanDateDesc(copyDto.getId())
                                .ifPresent(loan -> {
                                    if (loan.getDueDate() != null) {
                                        copyDto.setStatusDescription(copyDto.getStatusDescription() + "(" + loan.getDueDate() + " 到期)"); // 想要特定格式可改用 DateTimeFormatter
                                    }
                                });
                    }
                });
            }
        });

        return opt;
        }
}