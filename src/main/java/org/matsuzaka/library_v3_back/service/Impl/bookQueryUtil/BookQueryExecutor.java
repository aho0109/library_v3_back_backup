package org.matsuzaka.library_v3_back.service.Impl.bookQueryUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.matsuzaka.library_v3_back.dto.queryDTO.StatItemDTO;
import org.matsuzaka.library_v3_back.model.entity.Book;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 書籍查詢執行器
 *
 * 職責說明:
 * 1. 封裝並執行複雜的書籍查詢邏輯
 * 2. 提供實體統計數據查詢方法
 * 3. 與EntityManager互動，執行動態查詢
 *
 * 設計理念:
 * - 通過分離查詢執行邏輯，降低BookServiceImpl的複雜度
 * - 提供可重複使用的查詢方法，增強代碼的可維護性
 * - 遵循單一職責原則，專注於查詢執行
 */
@Component
public class BookQueryExecutor {

    private final EntityManager entityManager;

    public BookQueryExecutor(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * 執行實體統計查詢
     *
     * @param keyword 搜尋關鍵字
     * @param mainCategoryId 主分類ID
     * @param subCategoryId 子分類ID
     * @param seriesDisplay 系列顯示模式
     * @param authorId 作者ID
     * @param publisherId 出版社ID
     * @param tagIds 標籤ID列表
     * @param seriesId 系列ID（可為null）
     * @param joinAttribute 要JOIN的書籍屬性名稱
     * @param secondJoinAttribute 要二次JOIN的屬性名稱（可為null）
     * @param idAttribute 要分組的ID屬性名稱
     * @param nameAttribute 要分組的名稱屬性名稱
     * @param representativeOnly 是否只統計代表作
     * @return 統計結果列表
     */
    public List<StatItemDTO> executeEntityStatsQuery(
            String keyword, Long mainCategoryId, Long subCategoryId, Integer seriesDisplay,
            Long authorId, Long publisherId, List<Long> tagIds, Long seriesId,
            String authorKeyword,
            String publisherKeyword,
            String bookTitleKeyword,
            Short publishYear,
            String isbn,
            String joinAttribute, String secondJoinAttribute, String idAttribute, String nameAttribute,
            boolean representativeOnly) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StatItemDTO> query = cb.createQuery(StatItemDTO.class);
        Root<Book> root = query.from(Book.class);

        // 建立JOIN關係和篩選條件
        Map<String, Join<?, ?>> joinMap = new HashMap<>();
        List<Predicate> predicates = new ArrayList<>();

        // 建立主要JOIN關係
        Join<?, ?> mainJoin = root.join(joinAttribute, JoinType.LEFT);
        joinMap.put("Book." + joinAttribute, mainJoin);

        // 如果需要二次JOIN，則建立二次JOIN關係
        Join<?, ?> secondJoin = null;
        if (secondJoinAttribute != null) {
            secondJoin = mainJoin.join(secondJoinAttribute, JoinType.LEFT);
            joinMap.put(mainJoin.getJavaType().getSimpleName() + "." + secondJoinAttribute, secondJoin);
        }

        // 應用通用篩選條件
        BookSpecificationHelper.applyFilterConditions(root, query, cb, predicates,
                keyword, mainCategoryId, subCategoryId, seriesDisplay, authorId, publisherId, tagIds, seriesId,
                authorKeyword,
                publisherKeyword,
                bookTitleKeyword,
                publishYear,
                isbn,joinMap);

        // 如果需要只統計代表作，添加額外條件
        if (representativeOnly) {
            predicates.add(cb.isTrue(root.get("representative")));
        }

        // 特別處理系列統計，過濾掉 null 值的系列
        if ("series".equals(joinAttribute)) {
            predicates.add(cb.isNotNull(mainJoin.get(idAttribute)));
            predicates.add(cb.isNotNull(mainJoin.get(nameAttribute)));
        }

        // 應用WHERE條件
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // 確定要分組和選擇的對象
        Join<?, ?> targetJoin = secondJoin != null ? secondJoin : mainJoin;

        // 按ID和名稱分組
        query.groupBy(targetJoin.get(idAttribute), targetJoin.get(nameAttribute));

        // 選擇查詢結果應包含的欄位
        query.multiselect(
                targetJoin.get(idAttribute),
                targetJoin.get(nameAttribute),
                cb.countDistinct(root.get("id"))
        );

        // 執行查詢並返回結果
        return entityManager.createQuery(query).getResultList();
    }
}
