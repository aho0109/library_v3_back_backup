package org.matsuzaka.library_v3_back.security;

// 引入 LoginRequest DTO
// 引入 LoginResponse DTO

import org.matsuzaka.library_v3_back.dto.userDTO.LoginRequest;
import org.matsuzaka.library_v3_back.dto.userDTO.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 步驟 9: 建立認證控制器 (AuthController.java)
// 這個控制器將提供登入 API 端點，用於處理使用者登入並返回 JWT。
// 認證相關的 API
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailSecuService userDetailSecuService; // 注入 CustomUserDetailsService

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailSecuService userDetailSecuService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailSecuService = userDetailSecuService;
    }

    // 接收前端傳來的 JSON 資料，具體：使用 @RequestBody 將 JSON 轉換為 LoginRequest 物件
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        // 1. 嘗試認證使用者
        // 呼叫 AuthenticationManager.authenticate() 方法，並傳入一個 UsernamePasswordAuthenticationToken 物件，其中包含使用者帳號和原始密碼。
        // 在 authenticationManager.authenticate 的內部流程中，就已經自動完成驗證了
        // Spring Security 會自動呼叫 userDetailSecuService.loadUserByUsername(...) 來查詢使用者資料並驗證密碼。
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getAccount(), loginRequest.getPassword())
        );

        // 2. 認證成功後，將認證資訊設定到安全上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 在請求的剩餘生命週期中（包括控制器、服務層、資料庫操作等），任何需要安全資訊的地方
        // 都可以透過 SecurityContextHolder.getContext().getAuthentication() 來獲取當前已認證的使用者資訊。


        // 3. 載入使用者詳細資訊 (從安全上下文獲取帳號)
        // 由於上方的 authenticationManager.authenticate(...) 已經完成了認證，這裏只是為了提供資料，以便下方的 JWT 生成。
        // UserDetails 是 Spring Security 的核心介面
        // 按照 JAVA 設計風格，這裡都是用介面（多形）來宣告，實際運作的就是我們自定義的實作類
        // 這裡使用自定義的 UserDetailSecuService 來載入使用者詳細資訊，UserDetailSecu 接收資訊
        //UserDetailSecu userDetails = userDetailSecuService.loadUserByUsername(loginRequest.getAccount());
        // UserDetailSecuService 的職責：
        // 它的 loadUserByUsername(String account) 方法會被呼叫。
        // 它能做什麼？ 它使用 userRepository.findByAccount(account) 從資料庫查詢對應的 User 實體。
        // 這裡產生什麼？ 如果找到使用者，它會將 User 實體包裝成 UserDetailSecu 物件（你的自定義 UserDetails 實現），並返回給 AuthenticationManager。
        // UserDetailSecu 包含了 Spring Security 所需的使用者名稱、雜湊密碼和角色資訊。

        // 3. 從認證結果中獲取我們的自訂 UserDetails 物件
        UserDetailSecu userDetails = (UserDetailSecu) authentication.getPrincipal();

        // 4. 生成 JWT
        // 傳入完整的 userDetails 物件。jwtUtil 會自動將 userId、username 和 role 全部打包進 token。
        String jwt = jwtUtil.generateToken(userDetails);

        /*// 5. 從 CustomUserDetails 獲取 userId 和 role
        Long userId = null;
        String role = null;
        userId = userDetails.getUser().getId(); // 獲取底層 User 實體的 ID
        role = userDetails.getUser().getRole(); // 獲取底層 User 實體的 Role

        // 6. 返回 JWT 和使用者資訊給前端
        return ResponseEntity.ok(new LoginResponse(jwt, userId, role));*/

        // 5. 返回一個只包含 JWT 的 LoginResponse。
        //    這一步徹底杜絕了前端儲存獨立身份資訊的可能性，從根源上解決了安全漏洞。
        return ResponseEntity.ok(new LoginResponse(jwt));
    }
}
