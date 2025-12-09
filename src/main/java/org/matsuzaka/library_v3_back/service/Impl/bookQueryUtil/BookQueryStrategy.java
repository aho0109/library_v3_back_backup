package org.matsuzaka.library_v3_back.service.Impl.bookQueryUtil;

import org.matsuzaka.library_v3_back.dto.queryDTO.BookSearchParamsDTO;
import org.matsuzaka.library_v3_back.dto.queryDTO.StatItemDTO;
import org.matsuzaka.library_v3_back.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

/**
 * 書籍查詢策略接口
 *
 * 職責說明:
 * 1. 定義書籍查詢相關的操作
 * 2. 提供策略模式的抽象接口，便於實現不同的查詢策略
 * 3. 隔離查詢邏輯與業務邏輯，提高代碼可維護性
 *
 * 設計理念:
 * - 策略模式的應用，將不同查詢邏輯封裝為獨立的策略
 * - 依賴倒置原則，依賴抽象而非具體實現
 * - 開閉原則，便於擴展新的查詢策略而無需修改現有代碼
 */
public interface BookQueryStrategy {

    /**
     * 根據參數獲取書籍分頁查詢結果
     *
     * @param params 查詢參數
     * @param pageable 分頁參數
     * @return 書籍實體分頁結果
     */
    Page<Book> queryBooks(BookSearchParamsDTO params, Pageable pageable);



    /**
     * 獲取書籍可借閱狀態
     *
     * @param books 書籍列表
     * @return 書籍ID到可借閱狀態的映射
     */
    Map<Long, Boolean> getAvailabilityStatus(List<Book> books);

    /**
     * 批量獲取書籍總副本數量
     *
     * @param books 書籍列表
     * @return 書籍ID到總副本數量的映射
     */
    Map<Long, Integer> getTotalCopiesCount(List<Book> books);

    /**
     * 批量獲取書籍可借閱副本數量
     *
     * @param books 書籍列表
     * @return 書籍ID到可借閱副本數量的映射
     */
    Map<Long, Integer> getAvailableCopiesCount(List<Book> books);

    /**
     * 獲取實體統計數據
     *
     * @param params 查詢參數
     * @param joinAttribute 主要JOIN屬性
     * @param secondJoinAttribute 次要JOIN屬性 (可為null)
     * @param idAttribute ID屬性名
     * @param nameAttribute 名稱屬性名
     * @param representativeOnly 是否只包含代表作
     * @return 統計結果列表
     */
    List<StatItemDTO> getEntityStats(BookSearchParamsDTO params,
                                     String joinAttribute,
                                     String secondJoinAttribute,
                                     String idAttribute,
                                     String nameAttribute,
                                     boolean representativeOnly);

    /**
     * 構建動態查詢規格
     *
     * @param params 查詢參數
     * @return 查詢規格
     */
    Specification<Book> buildSearchSpecification(BookSearchParamsDTO params);
}
