package org.matsuzaka.library_v3_back.exception;

/**
 * 業務邏輯異常
 * 用於表示業務規則違反（例如：借閱上限、預約限制、權限不足等）
 */
public class BusinessException extends BaseException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public BusinessException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail, cause);
    }
}

