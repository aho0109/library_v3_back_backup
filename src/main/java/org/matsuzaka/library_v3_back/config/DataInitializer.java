package org.matsuzaka.library_v3_back.config;


import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.entity.UserDetail;
import org.matsuzaka.library_v3_back.model.enums.Role;
import org.matsuzaka.library_v3_back.model.enums.UserStatus;
import org.matsuzaka.library_v3_back.model.repositoryDao.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // 標記為配置類別，讓 Spring 掃描到
public class DataInitializer {

    // 定義一個 Bean，Spring Boot 應用程式啟動時會自動執行這個方法
    @Bean
    public CommandLineRunner initDataAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 檢查是否已存在帳號為 "adminJAVA" 的使用者
            if (userRepository.findByAccount("adminJAVA1925").isEmpty()) {
                System.out.println("--- 正在初始化 測試管理員 adminJAVA1925 ---");

                // 創建 UserDetail (個人詳細資訊)
                UserDetail userDetail = new UserDetail();
                userDetail.setName("管理員");
                userDetail.setEmail("adminJAVA1925@example.com");
                userDetail.setPhone("0999999999");
                userDetail.setAddress("管理員的家");
                // 注意：userDetail 的 user 欄位會在 User 實體保存時自動設置，因為是雙向關聯

                // 創建 User (安全相關資訊)
                User user = new User();
                user.setAccount("adminJAVA1925");
                // 使用 PasswordEncoder 對密碼進行編碼
                user.setPassword(passwordEncoder.encode("123456789")); // 明文密碼是 "123456789"
                user.setCardId("LIB000");
                user.setRole(Role.ROLE_ADMIN); // 設定角色
                user.setPenaltyPoints(0); // 初始罰點數為 0
                user.setStatus(UserStatus.ACTIVE);

                // 建立 User 和 UserDetail 之間的關聯
                user.setUserDetail(userDetail);
                userDetail.setUser(user); // 確保雙向關聯設置

                // 保存 User 實體。由於 User 和 UserDetail 配置了 CascadeType.ALL，
                // 保存 User 時會自動保存其關聯的 UserDetail。
                userRepository.save(user);
                System.out.println("測試用戶 'adminJAVA1925' (密碼: 123456789) 和其詳細資訊已成功創建。");
            } else {
                System.out.println("測試用戶 'adminJAVA1925' 已存在，跳過數據初始化。");
            }
        };
    }

    @Bean
    public CommandLineRunner initDataCitizen(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 檢查是否已存在帳號為 "adminJAVA" 的使用者
            if (userRepository.findByAccount("citizen777").isEmpty()) {
                System.out.println("--- 正在初始化 測試市民 citizen777 ---");

                // 創建 UserDetail (個人詳細資訊)
                UserDetail userDetail = new UserDetail();
                userDetail.setName("市民");
                userDetail.setEmail("citizen777@example.com");
                userDetail.setPhone("0977777777");
                userDetail.setAddress("市民的家");
                // 注意：userDetail 的 user 欄位會在 User 實體保存時自動設置，因為是雙向關聯

                // 創建 User (安全相關資訊)
                User user = new User();
                user.setAccount("citizen777");
                // 使用 PasswordEncoder 對密碼進行編碼
                user.setPassword(passwordEncoder.encode("123456789")); // 明文密碼是 "123456789"
                user.setCardId("LIB001");
                user.setRole(Role.ROLE_CITIZEN); // 設定角色
                user.setPenaltyPoints(0); // 初始罰點數為 0
                user.setStatus(UserStatus.ACTIVE);

                // 建立 User 和 UserDetail 之間的關聯
                user.setUserDetail(userDetail);
                userDetail.setUser(user); // 確保雙向關聯設置

                // 保存 User 實體。由於 User 和 UserDetail 配置了 CascadeType.ALL，
                // 保存 User 時會自動保存其關聯的 UserDetail。
                userRepository.save(user);
                System.out.println("測試用戶 'citizen777' (密碼: 123456789) 和其詳細資訊已成功創建。");
            } else {
                System.out.println("測試用戶 'citizen777' 已存在，跳過數據初始化。");
            }
        };
    }

    @Bean
    public CommandLineRunner initDataUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 檢查是否已存在帳號為 "user1925" 的使用者
            if (userRepository.findByAccount("user1925").isEmpty()) {
                System.out.println("--- 正在初始化 測試一般人 user1925 ---");

                // 創建 UserDetail (個人詳細資訊)
                UserDetail userDetail = new UserDetail();
                userDetail.setName("一般人");
                userDetail.setEmail("user1925@example.com");
                userDetail.setPhone("0988888888");
                userDetail.setAddress("一般人的家");
                // 注意：userDetail 的 user 欄位會在 User 實體保存時自動設置，因為是雙向關聯

                // 創建 User (安全相關資訊)
                User user = new User();
                user.setAccount("user1925");
                // 使用 PasswordEncoder 對密碼進行編碼
                user.setPassword(passwordEncoder.encode("123456789")); // 明文密碼是 "123456789"
                user.setCardId("LIB002");
                user.setRole(Role.ROLE_USER); // 設定角色
                user.setPenaltyPoints(0); // 初始罰點數為 0
                user.setStatus(UserStatus.ACTIVE);

                // 建立 User 和 UserDetail 之間的關聯
                user.setUserDetail(userDetail);
                userDetail.setUser(user); // 確保雙向關聯設置

                // 保存 User 實體。由於 User 和 UserDetail 配置了 CascadeType.ALL，
                // 保存 User 時會自動保存其關聯的 UserDetail。
                userRepository.save(user);
                System.out.println("測試用戶 'user1925' (密碼: 123456789) 和其詳細資訊已成功創建。");
            } else {
                System.out.println("測試用戶 'user1925' 已存在，跳過數據初始化。");
            }
        };
    }

}
