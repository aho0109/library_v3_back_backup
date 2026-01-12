package org.matsuzaka.library_v3_back.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
import org.matsuzaka.library_v3_back.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
/**
 * JWT 認證過濾器
 *
 * 職責：
 * 1. 從請求 Header 中提取 JWT Token
 * 2. 驗證 Token 的有效性
 * 3. 將認證資訊設定到 Spring Security Context
 * 4. 處理 JWT 相關異常並返回統一的錯誤回應
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailSecuService userDetailSecuService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailSecuService userDetailSecuService) {
        this.jwtUtil = jwtUtil;
        this.userDetailSecuService = userDetailSecuService;
        this.objectMapper = new ObjectMapper();
        // 註冊 JavaTimeModule 以支援 LocalDateTime 等 Java 8 時間類型
        this.objectMapper.registerModule(new JavaTimeModule());
        // 將日期序列化為 ISO-8601 格式字串，而不是 timestamp
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        //final String jwt;
        //final String userAccount;
        //final Long userIdStr; // 使用者 ID，若有需要，可用


        // 檢查 Authorization Header 是否存在且格式正確 (Bearer Token)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // 不處理，交給下一個過濾器
            return;
        }

        try {
            // 提取 JWT Token
            String jwt = authHeader.substring(7);

            // 從 JWT 中提取使用者資訊
            String userAccount = jwtUtil.extractUsername(jwt); // 從 JWT 中提取使用者帳號
            Long userId = jwtUtil.extractUserId(jwt);          // 從 JWT 中提取使用者 ID，若有需要，可用

            // 如果帳號不為空，且當前安全上下文沒有認證資訊
            if (userAccount != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 使用提取出的 userAccount，呼叫 UserDetailSecuService 查詢使用者資料
                // 讓 Filter 的邏輯和 Spring Security 的標準流程 邏輯保持一致
                UserDetails userDetails = this.userDetailSecuService.loadUserByUsername(userAccount);

                // 驗證 JWT 是否有效
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // 創建認證物件
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // 憑證為 null，因為 JWT 已驗證
                            userDetails.getAuthorities()
                    );

                    // 設定認證請求的詳細資訊（例如 IP 地址等）
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 將認證物件設定到安全上下文，供後續使用，表示使用者已通過認證
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    logger.debug("JWT 驗證成功 - 使用者: {}", userAccount);
                }
            }

            // 繼續執行過濾鏈中的下一個過濾器
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            logger.warn("JWT Token 已過期: {}", e.getMessage());
            handleJwtException(response, "登入已過期，請重新登入");

        } catch (MalformedJwtException e) {
            logger.warn("JWT Token 格式錯誤: {}", e.getMessage());
            handleJwtException(response, "無效的認證資訊，請重新登入");

        } catch (SignatureException e) {
            logger.warn("JWT 簽名驗證失敗: {}", e.getMessage());
            handleJwtException(response, "認證資訊驗證失敗，請重新登入");

        } catch (JwtException e) {
            logger.warn("JWT 處理異常: {}", e.getMessage());
            handleJwtException(response, "認證資訊異常，請重新登入");

        } catch (Exception e) {
            logger.error("JWT Filter 發生未預期的錯誤", e);
            handleJwtException(response, "認證處理失敗，請重新登入");
        }
    }

    /**
     * 處理 JWT 異常，返回統一的錯誤回應
     */
    private void handleJwtException(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> apiResponse = ApiResponse.error(
            ErrorCode.UNAUTHORIZED.getCode(),
            message
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
