package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.dto.userDTO.UserDetailRespDto;
import org.matsuzaka.library_v3_back.model.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 根據卡號查詢
    Optional<User> findByCardId(String cardId);

    // 根據帳號查詢
    //Optional<User> findByAccount(String account);

    // 檢查卡號是否存在
    boolean existsByCardId(String cardId);

    // 檢查帳號是否存在
    boolean existsByAccount(String account);

    // 根據帳號和密碼查詢（用於登入驗證）
    Optional<User> findByAccountAndPassword(String account, String password);

    // findAll
    @Override
    List<User> findAll();

    // 個人帳號頁面
    /*
    // 定義一個 EntityGraph，用於載入使用者詳細資訊時，同時載入其 UserDetail
    @EntityGraph(value = "user-with-details", type = EntityGraph.EntityGraphType.LOAD)
    Optional<User> findById(Long id); // 覆寫 JpaRepository 的 findById
    */

    // 或者，如果你想直接查詢 DTO (不推薦用於這種巢狀結構，但作為範例)
    @Query(value = """
            SELECT u.id, ud.name, u.card_id, u.account, ud.email, ud.phone, ud.address, 
                   u.penalty_points, u.status, u.role, u.suspended_until, ud.created_at
            FROM user u
            JOIN user_detail ud ON u.id = ud.user_id
            WHERE u.id = :userId;\s""", nativeQuery = true)
    UserDetailRespDto findUserDetailById(@Param("userId") Long userId);
    // AI 原本是寫返回類型 Optional<UserDetailRespDto>，但這樣會導致查詢失敗，可能因我這寫法 DTO 不是實體類別，所以不能直接返回 Optional？


    // Security user
    // 新增此方法：根據帳號查詢使用者
    Optional<User> findByAccount(String account);


    @EntityGraph(value = "user-with-details", type = EntityGraph.EntityGraphType.LOAD)
    Optional<User> findById(Long id);

    // 查詢最大的 card_id 數字部分
    // CAST(SUBSTRING(card_id, 4) AS UNSIGNED) 將 'LIB001' 轉為 1
    @Query(value = "SELECT MAX(CAST(SUBSTRING(u.card_id, 4) AS UNSIGNED)) FROM user u WHERE u.card_id LIKE 'LIB%'", nativeQuery = true)
    Long findMaxCardIdNumber();
}