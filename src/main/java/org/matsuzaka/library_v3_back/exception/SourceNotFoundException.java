package org.matsuzaka.library_v3_back.exception;

// 設定自定義異常，並加進 @ControllerAdvice 全局處理

// (可選) 也可以在這裡直接指定 HTTP 狀態碼，但更推薦在 @ControllerAdvice 中統一處理
// @ResponseStatus(HttpStatus.NOT_FOUND)
public class SourceNotFoundException extends RuntimeException {
    // 方法名要跟類別名一致
    public SourceNotFoundException(String message) {
        super(message);
    }
}




