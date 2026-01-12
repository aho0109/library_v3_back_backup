package org.matsuzaka.library_v3_back.exceptionHandler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.matsuzaka.library_v3_back.dto.common.ApiResponse;
import org.matsuzaka.library_v3_back.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全域異常處理器
 * 統一處理所有異常，並回應統一格式的 ApiResponse
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    // ==================== 自定義業務異常處理 ====================

    /**
     * 處理基礎異常 (BaseException 及其子類別)
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        logger.error("業務異常 [{}]: {}", e.getErrorCode().getCode(), e.getFullMessage(), e);

        ApiResponse<Void> response = ApiResponse.error(
            e.getErrorCode().getCode(),
            e.getFullMessage()
        );

        return ResponseEntity
            .status(getHttpStatusFromErrorCode(e.getErrorCode()))
            .body(response);
    }

    /**
     * 處理資源不存在異常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException e) {
        logger.warn("資源不存在 [{}]: {}", e.getErrorCode().getCode(), e.getFullMessage());

        ApiResponse<Void> response = ApiResponse.error(
            e.getErrorCode().getCode(),
            e.getFullMessage()
        );

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(response);
    }

    /**
     * 處理業務邏輯異常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        logger.warn("業務邏輯異常 [{}]: {}", e.getErrorCode().getCode(), e.getFullMessage());

        ApiResponse<Void> response = ApiResponse.error(
            e.getErrorCode().getCode(),
            e.getFullMessage()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * 處理參數驗證異常
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException e) {
        logger.warn("參數驗證異常 [{}]: {}", e.getErrorCode().getCode(), e.getFullMessage());

        ApiResponse<Void> response = ApiResponse.error(
            e.getErrorCode().getCode(),
            e.getFullMessage()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    // ==================== Spring Security 認證異常處理 ====================
    // 一般情況下，照目前設計，ErrorCode 應該在 Service（serviceImpl） 層決定
    // 但Spring Security 認證比較特例
    //   認證流程在 Controller 之前就完成了
    //   異常是 Spring Security 框架拋出的
    //   我們無法在 Service 層控制
    //   所以在 GlobalExceptionHandler 映射是合理的例外


    /**
     * 處理帳號密碼錯誤異常（登入失敗）
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException e) {
        logger.warn("登入失敗 - 帳號或密碼錯誤: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INVALID_CREDENTIALS.getCode(),
            ErrorCode.INVALID_CREDENTIALS.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(response);
    }

    /**
     * 處理使用者不存在異常（登入時找不到帳號）
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(UsernameNotFoundException e) {
        logger.warn("登入失敗 - 使用者不存在: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INVALID_CREDENTIALS.getCode(),
            ErrorCode.INVALID_CREDENTIALS.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(response);
    }

    /**
     * 處理其他認證異常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        logger.warn("認證失敗: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.UNAUTHORIZED.getCode(),
            "認證失敗，請檢查您的登入資訊"
        );

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(response);
    }

    // ==================== 相容舊異常（保留以確保平滑過渡）====================

    /**
     * 處理舊的自定義異常 SourceNotFoundException
     * @deprecated 使用 ResourceNotFoundException 替代
     */
    @ExceptionHandler(SourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSourceNotFoundException(SourceNotFoundException ex) {
        logger.error("SourceNotFoundException (已棄用): {}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.RESOURCE_NOT_FOUND.getCode(),
            ex.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(response);
    }

    // ==================== JPA/標準異常處理 ====================

    /**
     * 處理 JPA EntityNotFoundException
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(EntityNotFoundException ex) {
        logger.error("Entity not found: {}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.RESOURCE_NOT_FOUND.getCode(),
            "請求的資源不存在"
        );

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(response);
    }

    /**
     * 處理 IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("非法參數: {}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INVALID_PARAMETER.getCode(),
            ex.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * 處理 IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        logger.error("非法狀態: {}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.BAD_REQUEST.getCode(),
            ex.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * 處理 NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointerException(NullPointerException ex) {
        logger.error("空指標異常", ex);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.NULL_POINTER_ERROR.getCode(),
            "系統發生空指標異常"
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
    }

    // ==================== 參數驗證異常處理 ====================

    /**
     * 處理 MethodArgumentNotValidException (Bean Validation 失敗)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        logger.error("參數驗證失敗", e);

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.error(
            ErrorCode.INVALID_PARAMETER.getCode(),
            "參數驗證失敗",
            errors
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * 處理 JSON 解析錯誤
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleJsonParseException(HttpMessageNotReadableException e) {
        logger.error("JSON 解析錯誤", e);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.JSON_PARSE_ERROR.getCode(),
            "資料格式錯誤，請檢查傳送的數據類型"
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * 處理參數類型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        logger.error("參數類型不匹配: {}", e.getName(), e);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.TYPE_MISMATCH.getCode(),
            "參數類型錯誤: " + e.getName()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * 處理數字格式錯誤
     */
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ApiResponse<Void>> handleNumberFormat(NumberFormatException e) {
        logger.error("數字格式錯誤", e);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.NUMBER_FORMAT_ERROR.getCode(),
            "數字格式錯誤"
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    // ==================== 資料庫相關異常處理 ====================

    /**
     * 處理資料完整性錯誤
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        logger.error("資料完整性錯誤", e);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.DATA_INTEGRITY_ERROR.getCode(),
            "資料庫資料不完整或格式錯誤"
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * 處理約束違反錯誤
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        logger.error("約束違反錯誤", e);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.CONSTRAINT_VIOLATION.getCode(),
            "資料驗證失敗：" + e.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * 處理 JPA 系統異常
     */
    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleJpaException(JpaSystemException e) {
        logger.error("JPA 系統錯誤", e);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.JPA_ERROR.getCode(),
            "資料庫查詢或映射發生錯誤"
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
    }

    // ==================== 通用異常處理 ====================

    /**
     * 處理所有未被特定處理器捕獲的異常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        logger.error("未預期的系統異常", ex);

        ApiResponse<Void> response = ApiResponse.error(
            ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            "系統發生錯誤，請稍後再試"
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
    }

    // ==================== 輔助方法 ====================

    /**
     * 根據錯誤碼映射到 HTTP 狀態碼
     */
    private HttpStatus getHttpStatusFromErrorCode(ErrorCode errorCode) {
        String code = errorCode.getCode();

        // 2xxx: 使用者相關錯誤
        if (code.startsWith("2")) {
            if (code.equals("2001") || code.equals("2013")) {
                return HttpStatus.NOT_FOUND; // 使用者不存在
            }
            if (code.equals("2002") || code.equals("2007") || code.equals("2008") || code.equals("2014")) {
                return HttpStatus.CONFLICT; // 已存在
            }
            if (code.equals("2003")) {
                return HttpStatus.UNAUTHORIZED; // 認證失敗
            }
            if (code.equals("2004") || code.equals("2005") || code.equals("2006") || code.equals("2012")) {
                return HttpStatus.FORBIDDEN; // 帳號狀態問題、帳號鎖定
            }
            return HttpStatus.BAD_REQUEST;
        }

        // 3xxx: 圖書相關錯誤
        if (code.startsWith("3")) {
            // 資源不存在 (末尾為 01, 03, 05, 06, 07, 08, 09, 10)
            if (code.equals("3001") || code.equals("3003") || code.equals("3005") ||
                code.equals("3006") || code.equals("3007") || code.equals("3008") ||
                code.equals("3009") || code.equals("3010")) {
                return HttpStatus.NOT_FOUND;
            }
            // 資源已存在 (3002, 3012, 3014, 3016, 3018)
            if (code.equals("3002") || code.equals("3012") || code.equals("3014") ||
                code.equals("3016") || code.equals("3018")) {
                return HttpStatus.CONFLICT;
            }
            // 其他業務邏輯錯誤 (3004, 3011, 3013, 3015, 3017, 3019, 3020, 3021)
            return HttpStatus.BAD_REQUEST;
        }

        // 4xxx, 5xxx, 6xxx, 7xxx, 8xxx: 業務邏輯錯誤
        if (code.startsWith("4") || code.startsWith("5") ||
            code.startsWith("6") || code.startsWith("7") || code.startsWith("8")) {
            // 記錄不存在 (末尾為 01, 04)
            if (code.endsWith("01") || code.endsWith("04")) {
                return HttpStatus.NOT_FOUND;
            }
            // 未授權操作 (4009, 5008, 6005, 8002)
            if (code.equals("4009") || code.equals("5008") ||
                code.equals("6005") || code.equals("8002")) {
                return HttpStatus.FORBIDDEN;
            }
            return HttpStatus.BAD_REQUEST;
        }

        // 1xxx: 通用錯誤
        if (code.startsWith("1")) {
            if (code.equals("1002")) {
                return HttpStatus.UNAUTHORIZED;
            }
            if (code.equals("1003")) {
                return HttpStatus.FORBIDDEN;
            }
            if (code.equals("1004")) {
                return HttpStatus.NOT_FOUND;
            }
            if (code.equals("1001") || code.equals("1005")) {
                return HttpStatus.BAD_REQUEST;
            }
            if (code.equals("1006")) {
                return HttpStatus.METHOD_NOT_ALLOWED;
            }
        }

        // 9xxx: 系統錯誤
        if (code.startsWith("9")) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}

