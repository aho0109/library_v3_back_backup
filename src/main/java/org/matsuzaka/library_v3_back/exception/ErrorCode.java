package org.matsuzaka.library_v3_back.exception;

import lombok.Getter;

/**
 * 統一錯誤碼枚舉
 *
 * 錯誤碼規則:
 * - 1xxx: 通用錯誤
 * - 2xxx: 使用者相關錯誤
 * - 3xxx: 圖書相關錯誤
 * - 4xxx: 借閱相關錯誤
 * - 5xxx: 預約相關錯誤
 * - 6xxx: 評論相關錯誤
 * - 7xxx: 收藏相關錯誤
 * - 9xxx: 系統錯誤
 */
@Getter
public enum ErrorCode {

    // ==================== 通用錯誤 (1xxx) ====================
    INTERNAL_SERVER_ERROR("1000", "系統內部錯誤"),
    INVALID_PARAMETER("1001", "參數驗證失敗"),
    UNAUTHORIZED("1002", "未授權，請先登入"),
    FORBIDDEN("1003", "無權限訪問"),
    RESOURCE_NOT_FOUND("1004", "資源不存在"),
    BAD_REQUEST("1005", "請求格式錯誤"),
    METHOD_NOT_ALLOWED("1006", "不支援的請求方法"),

    // ==================== 使用者相關錯誤 (2xxx) ====================
    USER_NOT_FOUND("2001", "使用者不存在"),
    USER_ALREADY_EXISTS("2002", "使用者已存在"),
    INVALID_CREDENTIALS("2003", "帳號或密碼錯誤"),
    USER_DISABLED("2004", "帳號已停用"),
    USER_SUSPENDED("2005", "帳號已停權"),
    USER_NOT_ACTIVATED("2006", "帳號未啟用"),
    ACCOUNT_ALREADY_EXISTS("2007", "帳號已存在"),
    EMAIL_ALREADY_EXISTS("2008", "電子郵件已存在"),
    PHONE_ALREADY_EXISTS("2014", "手機號碼已存在"),
    PASSWORD_TOO_SHORT("2009", "密碼長度不足"),
    OLD_PASSWORD_INCORRECT("2010", "原密碼不正確"),
    PASSWORD_ATTEMPT_EXCEEDED("2011", "密碼錯誤次數過多，帳號已鎖定"),
    ACCOUNT_LOCKED("2012", "帳號已鎖定"),
    USER_DETAIL_NOT_FOUND("2013", "使用者詳細資料不存在"),

    // ==================== 圖書相關錯誤 (3xxx) ====================
    BOOK_NOT_FOUND("3001", "圖書不存在"),
    BOOK_ALREADY_EXISTS("3002", "圖書已存在"),
    BOOK_COPY_NOT_FOUND("3003", "圖書副本不存在"),
    BOOK_COPY_UNAVAILABLE("3004", "圖書副本目前無法借閱"),
    CATEGORY_NOT_FOUND("3005", "分類不存在"),
    CATEGORY_SUB_NOT_FOUND("3006", "子分類不存在"),
    AUTHOR_NOT_FOUND("3007", "作者不存在"),
    PUBLISHER_NOT_FOUND("3008", "出版社不存在"),
    SERIES_NOT_FOUND("3009", "系列不存在"),
    TAG_NOT_FOUND("3010", "標籤不存在"),
    BOOK_HAS_COPIES("3011", "圖書仍有副本，無法刪除"),

    AUTHOR_ALREADY_EXISTS("3012", "作者已存在"),
    AUTHOR_HAS_BOOKS("3013", "作者仍有關聯書籍，無法刪除"),

    PUBLISHER_ALREADY_EXISTS("3014", "出版社已存在"),
    PUBLISHER_HAS_BOOKS("3015", "出版社仍有關聯書籍，無法刪除"),

    SERIES_ALREADY_EXISTS("3016", "系列已存在"),
    SERIES_HAS_BOOKS("3017", "系列仍有關聯書籍，無法刪除"),

    TAG_ALREADY_EXISTS("3018", "標籤已存在"),
    TAG_HAS_BOOKS("3019", "標籤仍有關聯書籍，無法刪除"),

    CATEGORY_HAS_SUBCATEGORIES("3020", "分類仍有子分類，無法刪除"),
    CATEGORY_SUB_HAS_BOOKS("3021", "子分類仍有關聯書籍，無法刪除"),

    // ==================== 借閱相關錯誤 (4xxx) ====================
    LOAN_NOT_FOUND("4001", "借閱記錄不存在"),
    BORROW_LIMIT_EXCEEDED("4002", "借閱數量已達上限"),
    BOOK_ALREADY_BORROWED("4003", "此書已被借出"),
    BOOK_NOT_BORROWED("4004", "此書無借出記錄"),
    RENEW_LIMIT_EXCEEDED("4005", "續借次數已達上限"),
    RENEW_WINDOW_NOT_OPEN("4006", "續借功能尚未開放"),
    BOOK_RESERVED_CANNOT_RENEW("4007", "此書已被預約，無法續借"),
    NOT_ON_LOAN_STATUS("4008", "非借閱中狀態，無法執行續借"),
    UNAUTHORIZED_LOAN_OPERATION("4009", "帳號不符，無權執行此借閱"),
    USER_HAS_OVERDUE_BOOKS("4010", "有逾期圖書未歸還"),

    // ==================== 預約相關錯誤 (5xxx) ====================
    RESERVATION_NOT_FOUND("5001", "預約記錄不存在"),
    RESERVATION_LIMIT_EXCEEDED("5002", "預約數量已達上限"),
    ALREADY_RESERVED("5003", "您已預約此書籍"),
    BOOK_AVAILABLE_NO_RESERVATION("5004", "非外借中書籍，不提供預約"),
    BOOK_RESERVED_BY_OTHERS("5005", "此書已被其他使用者預約"),
    CANNOT_RESERVE_OWN_LOAN("5006", "無法預約自己正在借閱的書籍"),
    RESERVATION_CANNOT_CANCEL("5007", "無法取消已完成，或已取消的預約"),
    UNAUTHORIZED_RESERVATION_OPERATION("5008", "帳號不符，無權執行此預約"),

    // ==================== 評論相關錯誤 (6xxx) ====================
    REVIEW_NOT_FOUND("6001", "評論不存在"),
    REVIEW_ALREADY_EXISTS("6002", "已評論過此書籍"),
    CANNOT_LIKE_OWN_REVIEW("6003", "無法按讚自己的評論"),
    REVIEW_LIKE_NOT_FOUND("6004", "按讚記錄不存在"),
    UNAUTHORIZED_REVIEW_OPERATION("6005", "只能編輯或刪除自己的評論"),
    ALREADY_LIKED("6006", "已按過讚"),

    // ==================== 收藏相關錯誤 (7xxx) ====================
    FAVORITE_NOT_FOUND("7001", "收藏記錄不存在"),
    ALREADY_FAVORITED("7002", "已收藏過此書籍"),

    // ==================== 通知相關錯誤 (8xxx) ====================
    NOTIFICATION_NOT_FOUND("8001", "通知不存在"),
    UNAUTHORIZED_NOTIFICATION_OPERATION("8002", "無權限操作此通知"),

    // ==================== 系統錯誤 (9xxx) ====================
    DATABASE_ERROR("9001", "資料庫操作失敗"),
    DATA_INTEGRITY_ERROR("9002", "資料完整性錯誤"),
    CONSTRAINT_VIOLATION("9003", "資料驗證失敗"),
    JPA_ERROR("9004", "資料庫查詢或映射錯誤"),
    JSON_PARSE_ERROR("9005", "JSON 解析錯誤"),
    TYPE_MISMATCH("9006", "參數類型錯誤"),
    NUMBER_FORMAT_ERROR("9007", "數字格式錯誤"),
    NULL_POINTER_ERROR("9008", "空指標異常");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

