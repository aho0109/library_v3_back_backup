package org.matsuzaka.library_v3_back.exception;

/**
 * 資源不存在異常
 * 用於表示請求的資源不存在（例如：書籍、使用者、借閱記錄等）
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public ResourceNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

