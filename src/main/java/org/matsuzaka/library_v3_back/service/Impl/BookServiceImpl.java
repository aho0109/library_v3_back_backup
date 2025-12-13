package org.matsuzaka.library_v3_back.service.Impl;


import jakarta.persistence.EntityNotFoundException;
import org.matsuzaka.library_v3_back.dto.adminDTO.CreateBookDTO;
import org.matsuzaka.library_v3_back.dto.adminDTO.ManyToManyInputDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.*;
import org.matsuzaka.library_v3_back.dto.queryDTO.queryOneDTO.BookRespDtoOneDetails;
import org.matsuzaka.library_v3_back.model.entity.*;
import org.matsuzaka.library_v3_back.model.enums.BookCopyStatus;
import org.matsuzaka.library_v3_back.model.mapper.BookMapper;
import org.matsuzaka.library_v3_back.model.repositoryDao.*;
import org.matsuzaka.library_v3_back.service.BookService;
import org.matsuzaka.library_v3_back.service.Impl.bookQueryUtil.BookQueryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 職責說明:
 * 1. 作為應用程式的業務邏輯核心，負責處理書籍相關的複雜操作
 * 2. 實現多條件篩選搜尋功能，支援分頁與排序
 * 3. 提供分面搜尋(Faceted Search)統計功能，優化使用者體驗
 * 4. 將資料庫實體(Entity)轉換為傳輸物件(DTO)，確保層間解耦
 *
 * 設計原則:
 * - 單一職責原則(SRP): 專注於書籍相關業務邏輯
 * - 依賴注入(DI): 通過建構函數注入依賴，便於測試
 * - 關注點分離(SoC): 業務邏輯、資料存取、展示邏輯明確分離
 * - 最小知識原則(LoD): 只依賴必要的外部組件
 */
@Service
@Transactional(readOnly = true) // 預設為只讀交易，提高效能並減少資料庫鎖定
public class BookServiceImpl implements BookService {

    // 日誌記錄器，用於記錄操作和追蹤錯誤
    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookQueryStrategy queryStrategy; // 書籍查詢策略，處理所有查詢邏輯

    public BookServiceImpl(BookQueryStrategy queryStrategy, BookRepository bookRepository, CategorySubRepository categorySubRepository, PublisherRepository publisherRepository, SeriesRepository seriesRepository, AuthorRepository authorRepository, TagRepository tagRepository, BookMapper bookMapper) {
        // 注入必要的存儲庫與工具
        // 書籍資料庫操作
        // JPA EntityManager，用於原生查詢
        // 查詢執行器，專門負責執行複雜查詢
        this.queryStrategy = queryStrategy;
        this.bookRepository = bookRepository;
        this.categorySubRepository = categorySubRepository;
        this.publisherRepository = publisherRepository;
        this.seriesRepository = seriesRepository;
        this.authorRepository = authorRepository;
        this.tagRepository = tagRepository;
        this.bookMapper = bookMapper;
    }

    /**
     * 主要的搜尋方法：結合書籍搜尋、篩選與統計功能。
     *
     * 注意: 此方法為向後兼容性而保留，推薦使用 searchAndFilterBooks(BookSearchParamsDTO, Pageable) 方法
     *
     * @param keyword        搜尋關鍵字，用於匹配書名或作者名
     * @param mainCategoryId 主分類ID，過濾特定分類的書籍
     * @param subCategoryId  子分類ID，進一步細化分類過濾
     * @param seriesDisplay  系列顯示模式，1表示僅顯示代表作
     * @param authorId       作者ID，過濾特定作者的書籍
     * @param publisherId    出版社ID，過濾特定出版社的書籍
     * @param tagIds         標籤ID列表，支持多標籤過濾
     * @param pageable       分頁和排序參數，包含頁碼、每頁大小和排序條件
     * @return 包含書籍列表和分面統計的複合DTO
     */
    @Override
    public BookSearchResponseDTO searchAndFilterAndGetStats(String keyword, Long mainCategoryId, Long subCategoryId, Integer seriesDisplay,
                                                            Long authorId, Long publisherId, List<Long> tagIds, Long seriesId,
                                                            String authorKeyword,
                                                            String publisherKeyword,
                                                            String bookTitleKeyword,
                                                            Short publishYear,
                                                            String isbn,
                                                            Pageable pageable) {
        // 創建搜尋參數物件並委託給新方法處理
        BookSearchParamsDTO params = BookSearchParamsDTO.from(keyword, mainCategoryId, subCategoryId,
                                                             seriesDisplay, authorId, publisherId, tagIds, seriesId,
                                                             authorKeyword, publisherKeyword, bookTitleKeyword, publishYear, isbn);

        logger.debug("通過傳統接口調用搜尋方法，將轉發至新實現");
        return searchAndFilterBooks(params, pageable);
    }

    /**
     * 主要的搜尋方法：結合書籍搜尋、篩選與統計功能。
     * 這個版本使用了封裝的參數物件來簡化參數傳遞，提高代碼可讀性。
     *
     * 功能說明:
     * 1. 接收多種篩選條件，構建動態查詢
     * 2. 執行分頁查詢，獲取符合條件的書籍列表
     * 3. 批量查詢書籍的可借閱狀態，避免N+1查詢問題
     * 4. 同時獲取分面統計數據，用於前端顯示篩選面板
     * 5. 將結果封裝為 DTO 格式
     *
     * 效能優化:
     * - 策略模式分離查詢邏輯，提高可維護性
     * - 利用參數物件模式簡化方法調用
     * - 清晰的流程分段提高代碼可讀性
     *
     * @param params 封裝的搜尋參數物件
     * @param pageable 分頁和排序參數
     * @return 包含書籍列表和分面統計的複合DTO
     */
    @Override
    public BookSearchResponseDTO searchAndFilterBooks(BookSearchParamsDTO params, Pageable pageable) {
        logger.debug("執行書籍搜尋與篩選, 參數: {}, 分頁: {}", params, pageable);

        try {
            // 創建返回DTO對象
            BookSearchResponseDTO responseDTO = new BookSearchResponseDTO();

            // 1. 獲取書籍列表及分頁資訊
            Page<BookListItemDTO> bookPage = fetchBookPageWithAvailability(params, pageable);
            responseDTO.setBookPage(PageResponseDTO.fromPage(bookPage));

            // 2. 獲取分面統計資料
            FacetedSearchStatsDTO stats = fetchFacetedSearchStats(params);
            responseDTO.setStats(stats);

            return responseDTO;
        } catch (Exception e) {
            logger.error("書籍搜尋過程發生錯誤", e);
            throw new RuntimeException("書籍搜尋服務暫時不可用，請稍後再試", e);
        }
    }

    /**
     * 獲取書籍分頁列表並附加可借閱狀態
     *
     * @param params 搜尋參數
     * @param pageable 分頁參數
     * @return 書籍列表分頁結果
     */
    private Page<BookListItemDTO> fetchBookPageWithAvailability(BookSearchParamsDTO params, Pageable pageable) {
        // 使用查詢策略獲取書籍
        Page<Book> booksPage = queryStrategy.queryBooks(params, pageable);

        // 使用查詢策略批量獲取書籍的可借閱狀態
        Map<Long, Boolean> availabilityMap = queryStrategy.getAvailabilityStatus(booksPage.getContent());

        // 批量獲取副本統計資料
        Map<Long, Integer> totalCopiesMap = queryStrategy.getTotalCopiesCount(booksPage.getContent());
        Map<Long, Integer> availableCopiesMap = queryStrategy.getAvailableCopiesCount(booksPage.getContent());

        // 將實體轉換為DTO，並注入可借閱狀態和副本統計
        return booksPage.map(book -> {
            boolean isAvailable = availabilityMap.getOrDefault(book.getId(), false);
            BookListItemDTO dto = BookListItemDTO.fromEntity(book, isAvailable, params.getSeriesDisplay());

            // 設定副本統計資料
            dto.setTotalCopies(totalCopiesMap.getOrDefault(book.getId(), 0));
            dto.setAvailableCopies(availableCopiesMap.getOrDefault(book.getId(), 0));

            return dto;
        });
    }

    /**
     * 獲取分面搜尋統計資料
     *
     * @param params 搜尋參數
     * @return 分面統計資料
     */
    private FacetedSearchStatsDTO fetchFacetedSearchStats(BookSearchParamsDTO params) {
        FacetedSearchStatsDTO stats = new FacetedSearchStatsDTO();

        // 使用查詢策略獲取各類統計數據
        stats.setCategories(queryStrategy.getEntityStats(params, "categorySub", "category", "id", "categoryTitle", false));
        stats.setSubCategories(queryStrategy.getEntityStats(params, "categorySub", null, "id", "categorySubTitle", false));
        stats.setSeries(queryStrategy.getEntityStats(params, "series", null, "id", "title", true));
        stats.setAuthors(queryStrategy.getEntityStats(params, "authors", null, "id", "name", false));
        stats.setPublishers(queryStrategy.getEntityStats(params, "publisher", null, "id", "pubName", false));
        stats.setTags(queryStrategy.getEntityStats(params, "tags", null, "id", "title", false));

        return stats;
    }


    private final BookRepository bookRepository;
    private final CategorySubRepository categorySubRepository;
    private final PublisherRepository publisherRepository;
    private final SeriesRepository seriesRepository;
    private final AuthorRepository authorRepository;
    private final TagRepository tagRepository;
    private final BookMapper bookMapper; // 用於將實體轉換為 DTO 的映射器

    /**
     * 建立一本新書，包含所有複雜的關聯和業務邏輯
     * @Transactional 確保所有資料庫操作要麼全部成功，要麼全部失敗，保證資料一致性
     */
    @Override
    @Transactional
    public BookRespDtoOneDetails createBook(CreateBookDTO dto) {
        Book book = new Book();

        // 1. 映射基本屬性
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        book.setPublishYear(dto.getPublishYear());
        book.setImageUrl(dto.getImageUrl());

        // 2. 處理簡單的 ToOne 關聯
        CategorySub categorySub = categorySubRepository.findById(dto.getCategorySubId())
                .orElseThrow(() -> new EntityNotFoundException("子分類不存在，ID: " + dto.getCategorySubId()));
        book.setCategorySub(categorySub);

        Publisher publisher = publisherRepository.findById(dto.getPublisherId())
                .orElseThrow(() -> new EntityNotFoundException("出版社不存在，ID: " + dto.getPublisherId()));
        book.setPublisher(publisher);

        // 3. 處理系列與代表作的業務邏輯
        if (dto.getSeriesId() != null) {
            Series series = seriesRepository.findById(dto.getSeriesId())
                    .orElseThrow(() -> new EntityNotFoundException("系列不存在，ID: " + dto.getSeriesId()));
            book.setSeries(series);

            // 將此系列舊的代表作標記為 false(0)
            bookRepository.findBySeriesAndRepresentative(series, true)
                    .ifPresent(oldRep -> oldRep.setRepresentative(false));

            // 將新書設為代表作
            book.setRepresentative(true);
        } else {
            // 單行本，自己就是代表作
            book.setSeries(null);
            book.setRepresentative(true);
        }

        // 4. 處理複雜的 ToMany 關聯 (作者和標籤)
        Set<Author> authors = processAuthors(dto.getAuthors()); // 前端送來的作者(ManyToManyInputDTO型別)，可能已經有，可能是新作者
        book.setAuthors(authors);

        Set<Tag> tags = processTags(dto.getTags());
        book.setTags(tags);

        // 5. 儲存 Book 實體 (級聯設定會自動儲存中間表)
        Book latestBook = bookRepository.save(book);
        // 因為我在 entity 裡面設定了
        // CascadeType.PERSIST：當儲存一個新的 Book 時 (persist 操作)，JPA 會檢查 authors 集合。如果裡面有任何新的、尚未儲存的 Author 物件，JPA 會自動地先幫您儲存這些 Author，然後再儲存 Book，最後建立它們之間的關聯。
        // CascadeType.MERGE：當更新一個已存在的 Book 時 (merge 操作)，如果 authors 集合中有任何變動（新增或修改了 Author），JPA 也會一併更新。

        bookMapper.toBookRespDtoOneDetails(latestBook);

        return bookMapper.toBookRespDtoOneDetails(latestBook);
    }

    /**
     * 輔助方法：處理作者的關聯邏輯
     */
    private Set<Author> processAuthors(ManyToManyInputDTO authorInput) {
        // ManyToManyInputDTO 裡面有
        // private List<Long> existingIds = new ArrayList<>();
        // private List<String> newNames = new ArrayList<>();
        if (authorInput == null) return new HashSet<>();

        // 處理已存在的作者
        Set<Author> authors = new HashSet<>(authorRepository.findAllById(authorInput.getExistingIds()));
        // findAllById 是 Spring Data JPA Repository 介面提供的方法，可以一次傳入多個 ID（例如 List<Long>），它會回傳所有符合這些 ID 的實體集合。
        // 動態期間生成程式碼：List<T> findAllById(Iterable<ID> ids)，只要是 Iterable 的實作類別都可以傳入，例如 List、Set 等。

        // 處理需要新建的作者
        if (authorInput.getNewNames() != null) {
            for (String name : authorInput.getNewNames()) {
                Author newAuthor = authorRepository.findByName(name)
                        .orElseGet(() -> authorRepository.save(new Author(null, name, new HashSet<>())));
                authors.add(newAuthor);
                // 如果 findByName 沒找到（回傳 Optional.empty），orElseGet 裡的 Lambda 函式就會執行。
                // authorRepository.save(new Author(...))：
                // 在這個 Lambda 中，我們先建立一個新的 Author 物件，然後立刻儲存它。JPA 會產生一條 INSERT INTO author ... 語句，並將儲存後帶有 ID 的實體回傳，也就是 newAuthor。
                //
            }
        }
        return authors; // 即將被加入新書的資料的作者們的資料
    }

    /**
     * 輔助方法：處理標籤的關聯邏輯
     */
    private Set<Tag> processTags(ManyToManyInputDTO tagInput) {
        if (tagInput == null) return new HashSet<>();

        // 處理已存在的標籤
        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagInput.getExistingIds()));

        // 處理需要新建的標籤
        if (tagInput.getNewNames() != null) {
            for (String title : tagInput.getNewNames()) {
                Tag tag = tagRepository.findByTitle(title)
                        .orElseGet(() -> tagRepository.save(new Tag(null, title, new HashSet<>())));
                tags.add(tag);
            }
        }
        return tags;
    }

    /**
     * 更新書籍資訊
     */
    @Override
    @Transactional
    public BookRespDtoOneDetails updateBook(Long id, CreateBookDTO dto) {
        // 查詢現有書籍
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("書籍不存在，ID: " + id));

        // 1. 更新基本屬性
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        book.setPublishYear(dto.getPublishYear());
        book.setImageUrl(dto.getImageUrl());

        // 2. 更新分類
        CategorySub categorySub = categorySubRepository.findById(dto.getCategorySubId())
                .orElseThrow(() -> new EntityNotFoundException("子分類不存在，ID: " + dto.getCategorySubId()));
        book.setCategorySub(categorySub);

        // 3. 更新出版社
        Publisher publisher = publisherRepository.findById(dto.getPublisherId())
                .orElseThrow(() -> new EntityNotFoundException("出版社不存在，ID: " + dto.getPublisherId()));
        book.setPublisher(publisher);

        // 4. 更新系列與代表作
        if (dto.getSeriesId() != null) {
            Series series = seriesRepository.findById(dto.getSeriesId())
                    .orElseThrow(() -> new EntityNotFoundException("系列不存在，ID: " + dto.getSeriesId()));
            book.setSeries(series);

            // 如果要設為代表作，將此系列其他代表作標記為 false
            if (dto.getRepresentative() != null && dto.getRepresentative()) {
                bookRepository.findBySeriesAndRepresentative(series, true)
                        .ifPresent(oldRep -> {
                            if (!oldRep.getId().equals(id)) {
                                oldRep.setRepresentative(false);
                            }
                        });
                book.setRepresentative(true);
            }
        } else {
            book.setSeries(null);
            book.setRepresentative(true);
        }

        // 5. 更新作者和標籤（清除舊的，設定新的）
        book.getAuthors().clear();
        Set<Author> authors = processAuthors(dto.getAuthors());
        book.setAuthors(authors);

        book.getTags().clear();
        Set<Tag> tags = processTags(dto.getTags());
        book.setTags(tags);

        // 6. 儲存更新
        Book updatedBook = bookRepository.save(book);

        return bookMapper.toBookRespDtoOneDetails(updatedBook);
    }

    /**
     * 刪除書籍
     */
    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("書籍不存在，ID: " + id));
        
        // 檢查是否有借閱中或預約中的副本
        long activeLoansCount = book.getBookCopies().stream()
                .filter(copy -> copy.getStatus() == BookCopyStatus.L || copy.getStatus() == BookCopyStatus.R)
                .count();
        
        if (activeLoansCount > 0) {
            throw new IllegalStateException("此書籍仍有副本在借閱或預約中，無法刪除");
        }
        
        bookRepository.delete(book);
    }

    /**
     * 根據 ID 查詢書籍詳細資訊（管理員用）
     */
    @Override
    public BookRespDtoOneDetails getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("書籍不存在，ID: " + id));
        return bookMapper.toBookRespDtoOneDetails(book);
    }


}
