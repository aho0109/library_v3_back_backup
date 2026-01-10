package org.matsuzaka.library_v3_back.exception;

import lombok.Getter;

/**
 * 自定義異常基類
 * 所有業務異常都應繼承此類
 */
@Getter
public class BaseException extends RuntimeException {

    /**
     * 錯誤碼
     */
    private final ErrorCode errorCode;

    /**
     * 額外的詳細資訊
     */
    private final String detail;

    /**
     * 建構子 - 只有錯誤碼
     */
    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    /**
     * 建構子 - 錯誤碼 + 詳細資訊
     */
    public BaseException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    /**
     * 建構子 - 錯誤碼 + 原因異常
     */
    public BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detail = null;
    }

    /**
     * 建構子 - 錯誤碼 + 詳細資訊 + 原因異常
     */
    public BaseException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detail = detail;
    }

    /**
     * 獲取完整的錯誤訊息（包含詳細資訊）
     */
    public String getFullMessage() {
        if (detail != null && !detail.isEmpty()) {
            return errorCode.getMessage() + ": " + detail;
        }
        return errorCode.getMessage();
    }
}

