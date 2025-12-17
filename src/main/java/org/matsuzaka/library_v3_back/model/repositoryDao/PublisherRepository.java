package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.model.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    Optional<Publisher> findByPubName(String pubName);
    List<Publisher> findByPubNameContaining(String pubName);

    @Override
    @Query("SELECT p FROM Publisher p ORDER BY p.id DESC ")
    List<Publisher> findAll();
}
