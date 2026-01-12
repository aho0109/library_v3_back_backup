package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.model.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 只是補充
    // 在刪除 Publisher 前把關聯的 book 的 publisher_id 設為 NULL
    // 另一方法是在資料庫層 讓 book 的 publisher_id 可以 null
    // 且把 FK 設為 ON DELETE SET NULL，刪除 Publisher 時 DB 自行把 publisher_id 設為 NULL，就不用額外 update。
    @Modifying
    @Query("UPDATE Book b SET b.publisher = NULL WHERE b.publisher.id = :publisherId")
    void nullifyPublisher(@Param("publisherId") Long publisherId);
}
