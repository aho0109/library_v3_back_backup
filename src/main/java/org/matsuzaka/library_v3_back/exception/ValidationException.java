package org.matsuzaka.library_v3_back.exception;

/**
 * 參數驗證異常
 * 用於表示請求參數驗證失敗
 */
public class ValidationException extends BaseException {

    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ValidationException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}

