package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryTitle(String categoryTitle);

    /**
     * 查詢所有分類
     * 預計內涵 subCategory 的資料
     * @return 所有分類列表
     */
    @Override
    @Query("SELECT c FROM Category c ORDER BY c.id DESC ")
    List<Category> findAll();
}
