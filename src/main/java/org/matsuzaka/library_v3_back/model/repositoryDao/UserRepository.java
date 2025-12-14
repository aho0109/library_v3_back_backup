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
    Optional<User> findByAccount(String account);

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
    /* 修正：將這個方法從原生 SQL 查詢改為了 JPQL 查詢，並加入了 SELECT new ... 語法。
    當 JPQL/SQL 查詢 SELECT a, b, c FROM ... 時，如果沒有明確指定如何將這些欄位組合成一個物件，
    JPA 會回傳一個 Object[] (或 List<Object[]>)。
    當 Spring Data 試圖將這個 Object[] 的第一個元素 (一個 Long) 強制轉換為 UserDetailRespDto 時，就會發生這個錯誤。
    解決方案：在 UserRepository.java 的 findUserDetailById 和 searchUsers 兩個方法的 @Query 中，
    明確使用 SELECT new org.matsuzaka.library_v3_back.dto.userDTO.UserDetailRespDto(...) 語法，
    告訴 JPA 如何使用我之前定義的 12 參數建構子來創建 DTO 物件。
    */
    @Query("""
            SELECT new org.matsuzaka.library_v3_back.dto.userDTO.UserDetailRespDto(
                u.id, ud.name, u.cardId, u.account, ud.email, ud.phone, ud.address, 
                u.penaltyPoints, u.status, u.role, u.suspendedUntil, ud.createdAt
            )
            FROM User u
            JOIN u.userDetail ud
            WHERE u.id = :userId
            """)
    UserDetailRespDto findUserDetailById(@Param("userId") Long userId);

    @EntityGraph(value = "user-with-details", type = EntityGraph.EntityGraphType.LOAD)
    Optional<User> findById(Long id);

    // 查詢最大的 card_id 數字部分
    @Query(value = "SELECT MAX(CAST(SUBSTRING(u.card_id, 4) AS UNSIGNED)) FROM user u WHERE u.card_id LIKE 'LIB%'", nativeQuery = true)
    Long findMaxCardIdNumber();

    // 管理員會員搜尋
    /* 修正：加入了 SELECT new ... 語法。*/
    @Query("""
            SELECT new org.matsuzaka.library_v3_back.dto.userDTO.UserDetailRespDto(
                u.id, ud.name, u.cardId, u.account, ud.email, ud.phone, ud.address,
                u.penaltyPoints, u.status, u.role, u.suspendedUntil, ud.createdAt
            )
            FROM User u
            JOIN u.userDetail ud
            WHERE (:cardId IS NULL OR u.cardId LIKE %:cardId%)
            AND (:account IS NULL OR u.account LIKE %:account%)
            AND (:name IS NULL OR ud.name LIKE %:name%)
            AND (:email IS NULL OR ud.email LIKE %:email%)
            AND (:phone IS NULL OR ud.phone LIKE %:phone%)
            """)
    List<UserDetailRespDto> searchUsers(
            @Param("cardId") String cardId,
            @Param("account") String account,
            @Param("name") String name,
            @Param("email") String email,
            @Param("phone") String phone
    );
}
