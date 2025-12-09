package org.matsuzaka.library_v3_back.exceptionHandler;// GlobalExceptionHandler.java

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.matsuzaka.library_v3_back.exception.SourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice // 聲明這是一個全局異常處理器
public class GlobalExceptionHandler {

    // 1. 引入日誌記錄器，讓系統輸出也顯示，而不是只給客戶端網頁顯示
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 定義一個錯誤響應 DTO (或者直接用 Map)
    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        private LocalDateTime timestamp;

        public ErrorResponse(HttpStatus status, String message) {
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }

        // Getters and Setters
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    // 處理自定義的 SourceNotFoundException 異常
    @ExceptionHandler(SourceNotFoundException.class) // 捕獲 BookNotFoundException 異常
    public ResponseEntity<ErrorResponse> handleSourceNotFoundException(SourceNotFoundException ex) {
        // 2. 記錄特定異常的錯誤訊息，使用 warn 或 error 級別
        //logger.warn("SourceNotFoundException caught: {}", ex.getMessage()); // 使用佔位符避免字串拼接開銷
        logger.error("Source not found for request.", ex); // 記錄完整的堆疊追蹤，暫時先用這個，比較好debug
        // 當捕獲到 BookNotFoundException 時，構建一個 404 Not Found 的 ResponseEntity
        // 響應體中包含統一的錯誤訊息格式
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    // 處理 JPA 提供的 EntityNotFoundException (例如當 findById().orElseThrow() 拋出時)
    // 通常也希望它返回 404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleJpaEntityNotFoundException(EntityNotFoundException ex) {
        logger.error("Entity not found for request.", ex); // 記錄完整的堆疊追蹤，暫時先用這個，比較好debug
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, "EntityNotFoundException 實體為空: " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 處理通用的 IllegalArgumentException (例如無效的輸入參數)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("Illegal argument for request.", ex);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 處理通用的 NullPointerException (例如空指針異常)
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "NullPointerException 空指向: ");
        // 這裡可以加入日誌記錄: logger.error("Null pointer exception:", ex);
        logger.error("Null pointer exception occurred.", ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 最後，捕獲所有其他未被特定處理器捕獲的異常，並返回 500 Internal Server Error
    /*@ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // 在生產環境中，這裡通常不應直接暴露 ex.getMessage()，
        // 而是返回一個通用錯誤訊息，並將詳細錯誤記錄到日誌中
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "非預期的例外產生。"
                // 或者 for development/debugging: "An unexpected error occurred: " + ex.getMessage()
        );
        // 這裡可以加入日誌記錄: logger.error("Unhandled exception:", ex);
        logger.error("未知未預期錯誤", ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }*/

    /**
     * 0819
     * DTO 型別錯誤，但卻是403*/
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleJsonParseException(HttpMessageNotReadableException e) {
        logger.error("JSON 解析錯誤: ", e);
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "資料格式錯誤，請檢查傳送的數據類型"
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        logger.error("參數類型不匹配: ", e);
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "參數類型錯誤: " + e.getName()
        ));
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<?> handleNumberFormat(NumberFormatException e) {
        logger.error("數字格式錯誤: ", e);
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "數字格式錯誤"
        ));
    }

    // sql結果跟dao或dto不匹配的情況
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        logger.error("資料完整性錯誤: ", e);
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "資料庫資料不完整或格式錯誤",
                "error", "DATA_INTEGRITY_ERROR"
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException e) {
        logger.error("資料驗證錯誤: ", e);
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "資料驗證失敗：" + e.getMessage(),
                "error", "VALIDATION_ERROR"
        ));
    }

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<?> handleJpaException(JpaSystemException e) {
        logger.error("JPA 系統錯誤: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "資料庫查詢或映射發生錯誤",
                "error", "JPA_ERROR"
        ));
    }

}