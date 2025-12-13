package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.model.entity.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Long> {
    Optional<Series> findByTitle(String title);
    List<Series> findByTitleContaining(String title);

    // findAll 方法已經由 JpaRepository 提供，service 層可以直接使用
}
