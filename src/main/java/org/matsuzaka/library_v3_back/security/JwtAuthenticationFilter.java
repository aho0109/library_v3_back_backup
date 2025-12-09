package org.matsuzaka.library_v3_back.security;

import jakarta.servlet.FilterChain;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// 確保每個請求只執行一次的過濾器
// 步驟 8: 建立 JWT 認證過濾器 (JwtAuthenticationFilter.java)
// 這個過濾器會攔截每個前端來的請求，檢查 JWT，並設定 Spring Security 的安全上下文。

// 實務上常見組合應該是解析 jwt + 解析出來的資料放快取，要確認使用者資料就從快取查，而不是現在我寫的從 SQL 查
// 但我就還沒弄快取，待優化
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailSecuService userDetailSecuService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailSecuService userDetailSecuService) {
        this.jwtUtil = jwtUtil;
        this.userDetailSecuService = userDetailSecuService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userAccount;
        final Long userIdStr; // 使用者 ID，若有需要，可用


        // 檢查 Authorization Header 是否存在且格式正確 (Bearer Token)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // 不處理，交給下一個過濾器
            return;
        }

        jwt = authHeader.substring(7); // 提取 JWT 字串
        userAccount = jwtUtil.extractUsername(jwt); // 從 JWT 中提取使用者帳號
        userIdStr = jwtUtil.extractUserId(jwt); // 從 JWT 中提取使用者 ID，若有需要，可用

        // 如果帳號不為空，且當前安全上下文沒有認證資訊
        if (userAccount != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 使用提取出的 userAccount，呼叫標準的 loadUserByUsername 方法來從資料庫查詢使用者。
            // 這讓 Filter 的邏輯與 Spring Security 的標準流程完全一致。
            UserDetails userDetails = this.userDetailSecuService.loadUserByUsername(userAccount);

            // 驗證 JWT 是否有效
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // 如果 JWT 有效，創建認證物件並設定到安全上下文
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // 憑證為 null，因為 JWT 已驗證
                        userDetails.getAuthorities()
                );
                // 設定認證請求的詳細資訊 (例如 IP 位址)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // 將認證物件設定到安全上下文，表示使用者已認證
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response); // 繼續執行過濾鏈中的下一個過濾器
    }
}
