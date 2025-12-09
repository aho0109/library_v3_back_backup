package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.model.entity.CategorySub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategorySubRepository extends JpaRepository<CategorySub, Long> {
    Optional<CategorySub> findByCategorySubTitle(String categorySubTitle);

    // 列出所有，依據 id 排序
    List<CategorySub> findAllByOrderByIdAsc();
}
