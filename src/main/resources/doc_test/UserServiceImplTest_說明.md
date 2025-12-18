UserServiceImpl 測試計畫
測試目標
•
registerUser: 驗證註冊邏輯，特別是帳號和 Email 的重複性檢查。
•
updateUserProfile: 驗證更新邏輯，同樣包含重複性檢查。
•
verifyOldPassword: 驗證密碼比對邏輯，以及失敗次數鎖定的機制。
•
changePassword: 驗證密碼更新的完整流程。
•
activateUser, suspendUser, restoreUser: 驗證管理員操作的正確性。
測試案例規劃
1.
registerUser_Success: 正常註冊。
2.
registerUser_Fail_AccountExists: 帳號已存在。
3.
registerUser_Fail_EmailExists: Email 已存在。
4.
updateUserProfile_Success: 正常更新。
5.
updateUserProfile_Fail_AccountExists: 嘗試更新為一個已存在的帳號。
6.
updateUserProfile_Fail_EmailExists: 嘗試更新為一個已存在的 Email。
7.
verifyOldPassword_Success: 舊密碼驗證成功。
8.
verifyOldPassword_Fail_IncorrectPassword: 舊密碼錯誤，驗證失敗次數增加。
9.
verifyOldPassword_Fail_AccountLocked: 密碼錯誤次數過多，帳號被鎖定。
10.
verifyOldPassword_Success_AfterLockout: 帳號鎖定時間過後，驗證成功。
11.
changePassword_Success: 成功修改密碼。
12.
changePassword_Fail_IncorrectOldPassword: 修改密碼時，舊密碼輸入錯誤。
13.
activateUser_Success: 成功開通帳號。
14.
suspendUser_Success: 成功停權帳號。
15.
restoreUser_Success: 成功復權帳號。
執行撰寫
我現在為您建立 src/test/java/org/matsuzaka/library_v3_back/service/Impl/UserServiceImplTest.java 並填入完整的測試程式碼。
UserServiceImplTest.java
+276
我已經為您撰寫了
UserServiceImplTest.java 的完整測試程式碼。
測試案例 摘要：
我總共撰寫了 15 個測試 案例，涵蓋了 UserServiceImpl 的所有主要公共方法：
•
registerUser (3 個案例): 成功、帳號重複、Email 重複。
•
updateUserProfile (3 個案例): 成功、更新的帳號與他人重複、更新的 Email 與他人重複。
•
verifyOldPassword (3 個案例): 成功、密碼錯誤、因錯誤次數過多而鎖定。
•
changePassword (2 個案例): 成功修改、因舊密碼錯誤而修改失敗。
•
activateUser, suspendUser, restoreUser (4 個案例): 成功開通、重複開通、成功停權、成功復權（並驗證罰點歸零）。
特殊處理：
•
@Spy vs @Mock: 對於 BCryptPasswordEncoder，我使用了 @Spy 而不是 @Mock。
◦
@Mock: 創建一個完全假的物件，所有方法預設都什麼也不做，除非你用 when(...).thenReturn(...) 明確設定。
◦
@Spy: 創建一個真實物件的「間諜」。它會呼叫真實的方法（例如，真的去執行加密），但我們仍然可以像 Mock 物件一樣去驗證它 的方法是否被呼叫 (verify)。
◦
原因: 我們需要 passwordEncoder 執行真實的加密和比對 (encode, matches)，才能正確驗證密碼邏輯，所以必須用 @Spy。
下一步：
1.
請您執行 mvn clean install。
2.
查看 JaCoCo 報告中 UserServiceImpl 的覆蓋率。這份測試應該能覆蓋絕大部分的邏輯。
3.
接下來，您可以依照同樣的模式，為 BookServiceImpl 或 ReservationServiceImpl 撰寫測試。BookServiceImpl 中的 updateBook 方法，特別是處理「代表作」的邏輯，會是一個很好的測試目標。
請問您希望我繼續為哪個檔案撰寫測試？