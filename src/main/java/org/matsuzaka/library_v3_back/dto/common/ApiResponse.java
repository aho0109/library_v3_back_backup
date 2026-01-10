package org.matsuzaka.library_v3_back.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 統一 API 回應格式
 * 
 * @param <T> 回應資料的類型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 只序列化非 null 的欄位
public class ApiResponse<T> {
    
    /**
     * 請求是否成功
     */
    private Boolean success;
    
    /**
     * 回應訊息
     */
    private String message;
    
    /**
     * 回應資料
     */
    private T data;
    
    /**
     * 錯誤碼 (僅在失敗時存在)
     */
    private String errorCode;
    
    /**
     * 時間戳記
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    // ==================== 成功回應的靜態工廠方法 ====================
    
    /**
     * 成功回應 (只有資料)
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 成功回應 (訊息 + 資料)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 成功回應 (只有訊息，無資料)
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // ==================== 失敗回應的靜態工廠方法 ====================
    
    /**
     * 錯誤回應 (只有訊息)
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 錯誤回應 (錯誤碼 + 訊息)
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 錯誤回應 (錯誤碼 + 訊息 + 資料)
     * 適用於需要回傳額外錯誤細節的情況
     */
    public static <T> ApiResponse<T> error(String errorCode, String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

