package org.matsuzaka.library_v3_back.model.repositoryDao;

import org.matsuzaka.library_v3_back.model.entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    UserDetail findByEmail(String email);
    UserDetail findByPhone(String phone);


    // 檢查email是否存在
    boolean existsByEmail(String email);

    // 檢查phone是否存在
    boolean existsByPhone(String phone);


}