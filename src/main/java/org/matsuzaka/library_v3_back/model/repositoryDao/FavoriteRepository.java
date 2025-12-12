package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.model.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    /**
     * 查詢使用者的所有收藏
     */
    @Query("SELECT f FROM Favorite f " +
           "JOIN FETCH f.book b " +
           "WHERE f.user.id = :userId " +
           "ORDER BY f.createdAt DESC")
    List<Favorite> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    /**
     * 查詢使用者是否已收藏某書籍
     */
    Optional<Favorite> findByUserIdAndBookId(Long userId, Long bookId);
    
    /**
     * 查詢使用者收藏的書籍 ID 列表
     */
    @Query("SELECT f.book.id FROM Favorite f WHERE f.user.id = :userId")
    List<Long> findBookIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 檢查使用者是否已收藏該書籍
     */
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    
    /**
     * 刪除使用者的收藏
     */
    void deleteByUserIdAndBookId(Long userId, Long bookId);
}
