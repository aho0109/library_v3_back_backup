package org.matsuzaka.library_v3_back.service.Impl.bookQueryUtil;

import org.matsuzaka.library_v3_back.dto.queryDTO.BookSearchParamsDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.StatItemDTO;
import org.matsuzaka.library_v3_back.model.entity.Book;
import org.matsuzaka.library_v3_back.model.repositoryDao.BookCopyRepository;
import org.matsuzaka.library_v3_back.model.repositoryDao.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 預設書籍查詢策略實現
 *
 * 職責說明:
 * 1. 實現 BookQueryStrategy 接口的所有方法
 * 2. 提供基於 JPA Specification 的查詢實現
 * 3. 優化查詢效能和資源使用
 *
 * 實現邏輯:
 * - 利用 JPA Specification 實現動態查詢
 * - 通過批量查詢優化性能
 * - 使用 Stream API 提高代碼可讀性和效率
 */
@Component
public class DefaultBookQueryStrategy implements BookQueryStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBookQueryStrategy.class);

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookQueryExecutor queryExecutor;

    public DefaultBookQueryStrategy(BookRepository bookRepository,
                                   BookCopyRepository bookCopyRepository,
                                   BookQueryExecutor queryExecutor) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.queryExecutor = queryExecutor;
    }

    /**
     * 根據參數獲取書籍分頁查詢結果
     */
    @Override
    public Page<Book> queryBooks(BookSearchParamsDTO params, Pageable pageable) {
        Specification<Book> spec = buildSearchSpecification(params);
        return bookRepository.findAll(spec, pageable);
    }

    /**
     * 獲取書籍可借閱狀態
     */
    @Override
    public Map<Long, Boolean> getAvailabilityStatus(List<Book> books) {
        Map<Long, Boolean> availabilityMap = new HashMap<>();

        if (CollectionUtils.isEmpty(books)) {
            logger.debug("書籍列表為空，無需查詢可借閱狀態");
            return availabilityMap;
        }

        try {
            // 獲取所有書籍的ID
            List<Long> bookIds = books.stream()
                    .map(Book::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 查詢所有書籍的可用副本數量
            if (!bookIds.isEmpty()) {
                List<Object[]> availabilityCounts = bookCopyRepository.countAvailableCopiesByBookIds(bookIds);

                // 將查詢結果轉換為Map
                for (Object[] result : availabilityCounts) {
                    Long bookId = (Long) result[0];
                    Long count = (Long) result[1];
                    // 將可用副本數量轉換為可借閱狀態(true表示有可借閱副本)
                    availabilityMap.put(bookId, count > 0);
                }

                // 為沒有副本的書籍設置預設值
                bookIds.forEach(id -> availabilityMap.putIfAbsent(id, false));
            }

            return availabilityMap;
        } catch (Exception e) {
            logger.error("獲取書籍可借閱狀態時發生錯誤", e);
            // 發生錯誤時，假設所有書籍都不可借閱
            books.forEach(book -> availabilityMap.put(book.getId(), false));
            return availabilityMap;
        }
    }

    /**
     * 批量獲取書籍總副本數量
     */
    @Override
    public Map<Long, Integer> getTotalCopiesCount(List<Book> books) {
        Map<Long, Integer> totalCopiesMap = new HashMap<>();

        if (CollectionUtils.isEmpty(books)) {
            logger.debug("書籍列表為空，無需查詢總副本數量");
            return totalCopiesMap;
        }

        try {
            List<Long> bookIds = books.stream()
                    .map(Book::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!bookIds.isEmpty()) {
                List<Object[]> totalCounts = bookCopyRepository.countTotalCopiesByBookIds(bookIds);

                for (Object[] result : totalCounts) {
                    Long bookId = (Long) result[0];
                    Long count = (Long) result[1];
                    totalCopiesMap.put(bookId, count.intValue());
                }

                // 為沒有副本的書籍設置預設值
                bookIds.forEach(id -> totalCopiesMap.putIfAbsent(id, 0));
            }

            return totalCopiesMap;
        } catch (Exception e) {
            logger.error("獲取書籍總副本數量時發生錯誤", e);
            books.forEach(book -> totalCopiesMap.put(book.getId(), 0));
            return totalCopiesMap;
        }
    }

    /**
     * 批量獲取書籍可借閱副本數量
     */
    @Override
    public Map<Long, Integer> getAvailableCopiesCount(List<Book> books) {
        Map<Long, Integer> availableCopiesMap = new HashMap<>();

        if (CollectionUtils.isEmpty(books)) {
            logger.debug("書籍列表為空，無需查詢可借閱副本數量");
            return availableCopiesMap;
        }

        try {
            List<Long> bookIds = books.stream()
                    .map(Book::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!bookIds.isEmpty()) {
                List<Object[]> availableCounts = bookCopyRepository.countAvailableCopiesByBookIds(bookIds);

                for (Object[] result : availableCounts) {
                    Long bookId = (Long) result[0];
                    Long count = (Long) result[1];
                    availableCopiesMap.put(bookId, count.intValue());
                }

                // 為沒有副本的書籍設置預設值
                bookIds.forEach(id -> availableCopiesMap.putIfAbsent(id, 0));
            }

            return availableCopiesMap;
        } catch (Exception e) {
            logger.error("獲取書籍可借閱副本數量時發生錯誤", e);
            books.forEach(book -> availableCopiesMap.put(book.getId(), 0));
            return availableCopiesMap;
        }
    }

    /**
     * 獲取實體統計數據
     */
    @Override
    public List<StatItemDTO> getEntityStats(BookSearchParamsDTO params,
                                            String joinAttribute,
                                            String secondJoinAttribute,
                                            String idAttribute,
                                            String nameAttribute,
                                            boolean representativeOnly) {
        return queryExecutor.executeEntityStatsQuery(
                params.getKeyword(), params.getMainCategoryId(), params.getSubCategoryId(),
                params.getSeriesDisplay(), params.getAuthorId(), params.getPublisherId(),
                params.getTagIds(), params.getSeriesId(),

                params.getAuthorKeyword(), params.getPublisherKeyword(),
                params.getBookTitleKeyword(), params.getPublishYear(), params.getIsbn(),

                joinAttribute, secondJoinAttribute, idAttribute,
                nameAttribute, representativeOnly);
    }

    /**
     * 構建動態查詢規格
     */
    @Override
    public Specification<Book> buildSearchSpecification(BookSearchParamsDTO params) {
        return BookSpecificationHelper.buildFilterSpec(
                params.getKeyword(), params.getMainCategoryId(), params.getSubCategoryId(),
                params.getSeriesDisplay(), params.getAuthorId(), params.getPublisherId(),
                params.getTagIds(), params.getSeriesId(),
                params.getAuthorKeyword(), params.getPublisherKeyword(), params.getBookTitleKeyword(),
                params.getPublishYear(), params.getIsbn());
    }
}
