package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.model.entity.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTitle(String title);
    List<Tag> findByTitleIn(List<String> titles);
    List<Tag> findByTitleContaining(String title);

    // 跨表查詢，特定category下，從 loan 取得熱門 tag 前10名，顯示tag_id, tag_title, book_count
    // 由于我们只需要Tag实体并计算相关数量，而不需要加载所有关联实体的完整数据，所以普通JOIN就足够了。
    // 如果需要返回Tag实体及其关联实体的数据，才需要使用JOIN FETCH。
    @Query(value = "SELECT t FROM Tag t " +
                    "JOIN t.books b " +
                    "JOIN b.bookCopies bc " +
                    "JOIN bc.loans l " +
                    "JOIN b.categorySub cs " +
                    "JOIN cs.category c " +
                    "WHERE (:categoryId IS NULL OR c.id = :categoryId) " +
                    "GROUP BY t.id, t.title " +
                    "ORDER BY COUNT(DISTINCT l.id) DESC")
    List<Tag> findTop10(@Param("categoryId") Long categoryId, Pageable pageable);

    // findAll 方法已經由 JpaRepository 提供，service 層可以直接使用

}
