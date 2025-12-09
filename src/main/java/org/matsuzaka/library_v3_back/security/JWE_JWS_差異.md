## 1. 最新提取 Claims 寫法
```java
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
```

`decryptWith` 是專門用於處理**加密的 JWT (JWE - JSON Web Encryption)**，而你目前使用的是**簽名的 JWT (JWS - JSON Web Signature)**。
**關鍵差異：**

1. **JWS (你的情況)**：
    - Token 內容是**明文 Base64 編碼**，任何人都能讀取
    - 使用 `signWith` **簽名**，防止竄改
    - 使用 `verifyWith` **驗證簽名**後即可讀取 Claims
    - 流程：簽名 → 驗證 → 讀取

2. **JWE (加密場景)**：
- Token 內容是**加密的**，無法直接讀取
- 需要先用 `decryptWith` **解密**才能取得 Claims
- 流程：加密 → 解密 → 讀取

**你的程式碼分析：**
```java
 .signWith(getSignKey())  // 使用 HS256 簽名，產生 JWS
 ```


因為你用的是 `signWith` 產生**簽名 JWT**，所以必須用 `verifyWith` 來驗證；若改用 `decryptWith` 會失敗，因為 token 根本沒有被加密，只是被簽名而已。

**總結：**
- 簽名 (JWS) → `verifyWith` - 驗證完整性
- 加密 (JWE) → `decryptWith` - 解密內容


## 2. 最新提取 Claims 寫法的 verifyWith 該用 SecretKey 還是 PublicKey

我的寫法是使用 jjwt，其 `Keys.hmacShaKeyFor(...)` 會產生對稱 HMAC 密鑰，用來對稱簽名／驗證。
RSA/ECDSA 屬於非對稱簽名，常見於需要伺服器公開簽名、讓不同系統驗證的情境，例如 API gateway 驗證由授權中心簽發的 JWT。
這種情況下會公開 `PublicKey` 供其他服務驗證，只有簽發者持有私鑰。

範例（RSA）：
```java
var keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
String token = Jwts.builder()
        .setSubject("user@example.com")
        .signWith(keyPair.getPrivate())
        .compact();

Claims claims = Jwts.parserBuilder()
        .setSigningKey(keyPair.getPublic())
        .build()
        .parseClaimsJws(token)
        .getBody();
```

此模式讓發行者用私鑰簽章，其他服務拿公開鑰驗證。