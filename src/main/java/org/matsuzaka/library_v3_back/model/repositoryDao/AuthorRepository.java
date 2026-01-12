package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.model.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    Optional<Author> findByName(String name);
    List<Author> findByNameContaining(String name);

    @Override
    @Query("SELECT a FROM Author a ORDER BY a.id DESC ")
    List<Author> findAll();

    // 只是補充，目前多對多只先讓標籤可單獨刪除
    // 刪除 Author 和 book 的關聯
    // 關聯數量大，改為在 Repository 用單一 SQL 刪除 join table 的關聯，再刪除 `tag`，比迭代每本書性能好。範例（native query）：
    @Modifying
    @Query(value = "DELETE FROM book_author WHERE author_id = :id", nativeQuery = true)
    void deleteAuthorAssociations(@Param("id") Long id);
}