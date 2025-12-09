package org.matsuzaka.library_v3_back.security;

import org.matsuzaka.library_v3_back.model.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

// 實現 Spring Security 的 UserDetails 介面
// 此實作類可將 User 實體包裝成 Spring Security 可理解的使用者詳細資訊。是 UserDetailSecuService 的回傳
// 概念類似再做一個 Entity ， 但是是將你的 User 實體轉換為 Spring Security 所需的 Entity，以便進行身份驗證和授權。
// 由 UserDetailSecuService 先去撈取使用者資訊，再裝進這裡 UserDetailSecu
public class UserDetailSecu implements UserDetails {

    private final User user; // 包裝你的 User 實體

    public UserDetailSecu(User user) {
        this.user = user;
    }

    // 返回使用者的權限 (角色)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 將你的 User 實體中的 role 字串轉換為 Spring Security 的 GrantedAuthority 物件
        // 確保你的 role 欄位儲存的是 "ROLE_USER", "ROLE_ADMIN" 這樣的格式
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString()));
        // 如果一個使用者可能有多個角色，可將多個 SimpleGrantedAuthority 放入一個 List 或其他 Collection，例如 Arrays.asList 或 ArrayList，而不是用 singletonList。這樣可以正確回傳多個權限給 Spring Security。
    }

    // 返回使用者的密碼
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // 返回使用者的帳號 (username)
    @Override
    public String getUsername() {
        return user.getAccount();
    }

    public String getRole() {
        return user.getRole().toString(); // 如果有角色欄位，返回角色
    }

    // 以下方法用於帳號狀態管理，通常預設為 true
    // 如果你的 User 實體有 enabled, accountNonExpired 等欄位，則在這裡返回它們的值
    @Override
    public boolean isAccountNonExpired() {
        return true; // 預設帳號不過期
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 預設帳號不鎖定
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 預設憑證不過期
    }

    @Override
    public boolean isEnabled() {
        return true; // 預設帳號啟用
    }

    // 提供一個方法來獲取底層的 User 實體，如果需要訪問其 ID 或其他資訊
    public User getUser() {
        return user;
    }

}
