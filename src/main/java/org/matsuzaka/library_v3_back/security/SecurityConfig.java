package org.matsuzaka.library_v3_back.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 啟用 Spring Security
@Configuration
@EnableWebSecurity
// 啟用方法級別的安全性，例如 @PreAuthorize
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // 注入 JWT 認證過濾器和自定義的 UserDetailsService
    // ⾃⼰设置的 Filter ，⽤於檢測頭部是否有 token ，没則放⾏，有則從緩存中拿到數據再放⾏
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final UserDetailSecuService userDetailSecuService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailSecuService userDetailSecuService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailSecuService = userDetailSecuService;
    }

    // 配置密碼編碼器
    // Spring 容器中必須有 BCryptPasswordEncoder 這個 bean 才能注入。
    // BCrypt 是一種強大的密碼雜湊 (hash) 函數，專門設計用於安全地存儲密碼
    // 它自動處理加鹽（salt）並可調整運算複雜度，能有效抵禦彩虹表攻擊
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 配置 AuthenticationManager，用於處理認證
    // 雖然 SecurityFilterChain 中請求的認證流程會自動透過 AuthenticationConfiguration 提供的 AuthenticationManager，
    // 但明確宣告 AuthenticationManager，可以在其他需要直接呼叫（例如 /login endpoint）時明確注入，避免自動配置帶來的不確定性，
    // 也方便未來自訂更多認證流程或替換 AuthenticationProvider
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 配置安全過濾鏈
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF (對於 RESTful API 通常禁用)
                .authorizeHttpRequests(authorize -> authorize
                        // 1. 公開訪問的端點
                        // 允許未認證的用戶訪問的端點 (例如登入、註冊、靜態資源)
                        .requestMatchers("/api/auth/**").permitAll() // 登入 API
                        .requestMatchers("/api/users/**").permitAll() // 註冊，修改 API
                        .requestMatchers("/api/books/**").permitAll()
                        .requestMatchers("/api/bookCopies/**").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()
                        .requestMatchers("/api/categorySubs/**").permitAll()
                        .requestMatchers("/api/publishers/**").permitAll()
                        .requestMatchers("/api/series/**").permitAll()
                        .requestMatchers("/api/tags/**").permitAll()
                        .requestMatchers("/api/authors/**").permitAll()
                        .requestMatchers("/api/loans/**").permitAll()
                        .requestMatchers("/api/favorites/**").permitAll()
                        .requestMatchers("/api/notifications/**").authenticated()

                        .requestMatchers("/api/admin/**").permitAll() // 暫時允許管理端點


                        // 2. 新增的特定認證端點
                        // 允許借閱 API 只有在認證後才能訪問 (配合 @PreAuthorize("isAuthenticated()") 進一步深層確認)
                        .requestMatchers("/api/loans/borrow").authenticated()
                        // 允許歸還 API 只有在認證後才能訪問 (配合 @PreAuthorize("isAuthenticated()") 進一步深層確認)
                        .requestMatchers("/api/loans/return").authenticated()

                        // 3. 任何其他請求都需要認證
                        // 臨時修改：允許所有其他請求，不進行認證 (僅用於測試和診斷)
                        .anyRequest().authenticated() // 修正此行：將 authenticated() 改為 permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT 應用程式使用無狀態會話
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 過濾器

        return http.build();
    }

    /*
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .defaultSuccessUrl("/", true) // 登入成功後的頁面
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }*/
}
