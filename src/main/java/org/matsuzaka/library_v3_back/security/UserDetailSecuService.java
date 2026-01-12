package org.matsuzaka.library_v3_back.security;

import org.matsuzaka.library_v3_back.model.entity.User;
import org.matsuzaka.library_v3_back.model.repositoryDao.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 實現 Spring Security 的 UserDetailsService 介面
// 主要職責是根據使用者名稱（帳號）載入使用者詳細資訊，去資料庫撈資料
// 撈到了就包進 MyUDImpl（我自定義的 UserDetails 的實作類），以便 Spring Security 進行身份驗證和授權。
//      1. 根據使用者帳號載入使用者詳細資訊（用於登入認證）
//      2. 根據使用者 ID 載入使用者詳細資訊（用於 JWT Token 驗證）
@Service
public class UserDetailSecuService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailSecuService.class);
    private final UserRepository userRepository;

    public UserDetailSecuService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Spring Security 標準方法：根據帳號載入使用者
     * 用於登入時的帳號密碼認證
     *
     * @param account 使用者帳號
     * @return UserDetailSecu 物件
     * @throws UsernameNotFoundException 如果找不到對應帳號的使用者
     */
    @Override
    @Transactional(readOnly = true) // 這裡使用 readOnly = true，因為我們只是查詢資料，不需要修改
    // 在方法簽名上宣告 throws 主要是為了符合 UserDetailsService 的介面契約並增強可讀性，但不是編譯上的必須。
    public UserDetailSecu loadUserByUsername(String account) throws UsernameNotFoundException {
        log.debug("載入使用者資訊 - 帳號: {}", account);

        // 根據帳號從資料庫查詢使用者
        //User user = userRepository.findByAccount(account) // 需要在 UserRepository 中新增 findByAccount 方法
        //        .orElseThrow(() -> new UsernameNotFoundException("找不到此帳號的使用者: " + account));
        User user = userRepository.findByAccount(account)
                .orElseThrow(() -> {
                    log.warn("使用者不存在 - 帳號: {}", account); // 因為多這一行，要顯式寫出 lambda 寫法 return
                    return new UsernameNotFoundException("找不到帳號: " + account); // 這裡才是真正拋出例外的作用點
                });

        log.debug("成功載入使用者 - ID: {}, 帳號: {}, 角色: {}", user.getId(), user.getAccount(), user.getRole());

        // 將查詢到的 User 實體包裝成 UserDetailSecu 返回
        return new UserDetailSecu(user);
    }

    /**
     * 【核心修改點 1】
     * 新增一個方法，專門用來依據「使用者 ID」載入使用者。
     * 這個方法將被 JwtAuthenticationFilter 呼叫，用來處理後續的 API 請求驗證。
     * 這樣職責更清晰：登入用帳號，驗證 Token 用 ID。
     *
     * @param id 使用者 ID
     * @return UserDetailSecu 物件
     * @throws UsernameNotFoundException 如果找不到對應 ID 的使用者
     */
    @Transactional(readOnly = true)
    public UserDetailSecu loadUserById(Long id) throws UsernameNotFoundException {
        log.debug("載入使用者資訊 - ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("使用者不存在 - ID: {}", id);
                    return new UsernameNotFoundException("找不到使用者 ID: " + id);
                });

        log.debug("成功載入使用者 - ID: {}, 帳號: {}, 角色: {}", user.getId(), user.getAccount(), user.getRole());

        return new UserDetailSecu(user);
    }
}
