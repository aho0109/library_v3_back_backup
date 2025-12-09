package org.matsuzaka.library_v3_back.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    /*
     * 【修改說明】
     * 移除了從 application.properties 讀取固定密鑰的方式。
     * 改為在應用程式啟動時動態生成一個安全的密鑰。
     *
     * 【優點】
     * 1.  安全性更高：密鑰保存在記憶體中，不會洩露在配置文件或原始碼裡。
     * 2.  方便性：不需要手動生成和管理密鑰字串。
     * 3.  唯一性：每次應用程式重啟都會使用新的密鑰，這使得舊的 token 在重啟後失效，可以強制所有使用者重新登入，在某些情境下這是一種安全策略。
     *
     * 【提醒】
     * 在分散式系統（例如多個服務實例）中，你需要確保所有實例使用相同的密鑰。
     * 在那種情況下，通常會使用一個集中的密鑰管理服務（如 HashiCorp Vault, AWS KMS）
     * 或在所有實例間共享一個安全的環境變數。但對於單體應用，動態生成是很好的實踐。
     */
    // 使用 io.jsonwebtoken 提供的 Keys 工具類動態生成一個適用於 HS256 算法的密鑰
    // 不再直接從 application.properties 讀取 jwt.secret 字串。
    // 這讓 JwtUtil 的職責更單純，只負責使用密鑰，而不關心密鑰如何產生或儲存。
    // 優點：
    // 安全性更高：密鑰只存在於應用程式的記憶體中，不會出現在任何檔案裡，大大降低了洩漏的風險。
    // 自動化：您不再需要手動去生成或管理密鑰。
    // 自動失效：每次應用程式重啟後，都會產生新密鑰，這會讓舊的 JWT (JSON Web Token) 自然失效，強制所有使用者需要重新登入，這本身也是一種安全策略。
    private Key signKey;
    // 視需求，也可以直接宣告為 SecretKey，這樣可以更明確地表示這是一個密鑰。

    // 從 application.properties 讀取 JWT 過期時間 (毫秒)
    @Value("${jwt.expiration}")
    private long expiration;

    // @PostConstruct 註解確保此方法在 JwtUtil bean 初始化後立即執行
    @PostConstruct
    public void init() {
        // 使用 io.jsonwebtoken 提供的 Keys 工具類動態生成一個適用於 HS256 算法的密鑰
        // this.signKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); 過時寫法
        /*try {
            this.signKey = Keys.hmacShaKeyFor(SecureRandom.getInstanceStrong().generateSeed(32));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error generating 密碼演算加密密鑰: " + e.getMessage());
            throw new RuntimeException(e);
        }*/
        // 使用SecureRandom.getInstanceStrong()獲取高強度的隨機數生成器
        // generateSeed(32)生成32字節(256位)的隨機種子，符合HMAC-SHA256算法的密鑰長度要求
        // Keys.hmacShaKeyFor()方法接收這個隨機字節數組並創建一個適用於HMAC-SHA算法的密鑰

        // 這樣的寫法確保了密鑰的安全性和適用性，並且符合最新的安全標準。
        // 每次應用重啟都會生成新的密鑰，密鑰只存在於內存中，不會被持久化
        this.signKey = Jwts.SIG.HS256.key().build();
        // 似乎是JJWT官方推薦最新寫法，更現代化
        // 1. Jwts.SIG - 訪問JWT簽名（Signature）相關的工具類
        // 2. .HS256 - 指定使用HMAC-SHA256算法（Hash-based Message Authentication Code with SHA-256）
        // 3. .key() - 創建一個密鑰生成器
        // 4. .build() - 構建並生成一個隨機密鑰
        // 自動生成一個適合HMAC-SHA256算法的隨機密鑰
        // 每次應用程序啟動時創建全新的密鑰，密鑰只存在於內存中，不會被持久化
        byte[] keyBytes = signKey.getEncoded();
        log.info("JWT 密鑰 (Base64 URL編碼後): {}", Base64.getUrlEncoder().encodeToString(keyBytes));
        // 使用 Base64 編碼輸出密鑰，測試用
        // 原本是寫 getEncoder() ，但實際官方認可的寫法是 getUrlEncoder()？
    }

    // 生成 JWT
    /**
     * 【核心修改點 1】
     * generateToken 方法現在直接接收 UserDetailSecu 物件。
     * 這樣做是為了能同時獲取到使用者的 ID、帳號和角色，並將它們全部放入 JWT 中。
     *
     * @param userDetails 包含完整使用者資訊的 UserDetails 物件
     * @return 包含使用者 ID 和角色的 JWT
     */
    public String generateToken(/*UserDetails userDetails*/UserDetailSecu userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // 可以將角色等資訊添加到 claims 中。這是安全的，因為 claims 會被簽名保護。
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority()); // 假設只有一個角色

        // 【核心修改點 2 - 實踐最佳做法】
        // 將「使用者帳號」也作為一個自訂聲明加入。
        // 為何要這麼做？因為 Spring Security 的 UserDetailsService 核心介面是透過 `loadUserByUsername` 來運作的。
        // 在 JWT 過濾器中，直接提取 username 來查詢使用者，是最符合 Spring Security 設計模式的做法。
        claims.put("username", userDetails.getUsername());
        // 將使用者的 Long 型別 ID 作為 JWT 的主題 (Subject)。
        // 'subject' 是 JWT 的一個標準聲明欄位，專門用來存放獨一無二的使用者識別碼。
        // 這是最關鍵的一步，從此 userId 就被 JWT 的簽名保護起來了。
        String subject = String.valueOf(userDetails.getUser().getId());
        // 這樣，JWT 中就同時包含了用於驗證的 username 和用於後端業務邏輯的 userId，兩者各司其職。


        return createToken(claims, subject);
        //return createToken(claims, userDetails.getUsername());
    }

    // 產生 JWT Token，用使用者的 username 來當成 subject
    // 這裏有修改成目前推薦新語法，不然都被警示過時
    private String createToken00(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims) // 自定義 claims
                .subject(subject) // 主題 (通常是使用者帳號)，現將 userId 設置為 subject
                .issuedAt(new Date(System.currentTimeMillis())) // 簽發時間
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 過期時間
                .signWith(getSignKey()) // 簽名算法和密鑰
                //.signWith(signKey) // 也可以
                .compact(); // 壓縮為 JWT 字串
    }
    // 產生 JWT Token，用使用者的 username 來當成 subject
    // 這裏有修改成目前推薦新語法，不然都被警示過時
    private String createToken(Map<String, Object> claims, String subject) {
        String token = Jwts.builder()
                .claims(claims) // 自定義 claims
                .subject(subject) // 主題 (通常是使用者帳號)，現將 userId 設置為 subject
                .issuedAt(new Date(System.currentTimeMillis())) // 簽發時間
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 過期時間
                .signWith(getSignKey()) // 簽名算法和密鑰
                //.signWith(signKey) // 也可以
                .compact(); // 壓縮為 JWT 字串

        // 輸出到 console 以便使用 Postman 測試
        System.out.println("Generated JWT: " + token);
        // 也使用 logger 記錄（可視需求調整為不記錄完整 token）
        log.info("Generated JWT (truncated): {}", token.length() > 64 ? token.substring(0, 64) + "..." : token);

        return token;
    }

    // 獲取簽名密鑰
    private Key getSignKey() {
        // 直接返回在 init() 中生成的密鑰
        return signKey;
    }


    /** * 從 JWT 中提取帳號
     * @param token JWT 字串
     * @return 使用者帳號 (subject)
     */

    /*// 從 JWT 中提取帳號
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }*/
    /**
     * 【核心修改點 3】
     * extractUsername 方法現在從自訂聲明 "username" 中提取使用者帳號。
     * 這使得 JwtAuthenticationFilter 可以直接獲取帳號，並呼叫 `loadUserByUsername`，
     * 完美契合 Spring Security 的標準流程。
     *
     * @param token JWT 字串
     * @return 使用者帳號 (String)
     */
    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    /**
     * 【核心修改點 3】
     * 新增一個方法，專門用來從 token 中安全地提取使用者 ID (Subject)。
     * 後端所有需要 userId 的地方，都應該呼叫這個方法，而不是信任前端傳來的任何 ID。
     * @param token JWT 字串
     * @return 使用者 ID (Long)
     */
    public Long extractUserId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        return Long.parseLong(subject);
    }

    // 從 JWT 中提取單個 Claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 從 JWT 中提取所有 Claims
    // 以改成最新寫法
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignKey()) // 取代setSigningKey，verifyWith 會更嚴格檢視用的是什麼類型的鑰，公或私，會檢查 payload 與簽名是否一致，通常用於 HMAC 或 RSA 簽章。
                // .decryptWith((SecretKey) getSignKey()) 不能用這個，是用於加密 JWT 的情境（JWE），會使用密鑰對加密過的內容進行解密，與簽名驗證的目的不同。
                .build()
                .parseSignedClaims(token)  // 取代parseClaimsJws
                .getPayload();             // 取代getBody

    }

    /*// 驗證 JWT 是否有效
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }*/
    /**
     * 【核心修改點 4】
     * validateToken 方法現在使用新修改的 extractUsername 方法來進行驗證。
     * 它的邏輯變為：從 token 中提取出的 username 是否與從資料庫查出的 userDetails 中的 username 相符。
     * 這是 JWT 驗證的標準步驟。
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // 判斷 JWT 是否過期
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 從 JWT 中提取過期時間
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
