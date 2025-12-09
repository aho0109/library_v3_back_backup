CREATE DATABASE IF NOT EXISTS MariaLibraryV3;
USE MariaLibraryV3;

DROP TABLE IF EXISTS email_log;;
DROP TABLE IF EXISTS email_template;;
DROP TABLE IF EXISTS announcement_read;;
DROP TABLE IF EXISTS announcement;;
DROP TABLE IF EXISTS notification;;
DROP TABLE IF EXISTS review_like;;
DROP TABLE IF EXISTS review;;
DROP TABLE IF EXISTS book_tag;;
DROP TABLE IF EXISTS book_author;;
DROP TABLE IF EXISTS tag;;
DROP TABLE IF EXISTS reservation;;
DROP TABLE IF EXISTS favorite;;
DROP TABLE IF EXISTS loan;;
DROP TABLE IF EXISTS book_copy;;
DROP TABLE IF EXISTS book;;
DROP TABLE IF EXISTS series;;
DROP TABLE IF EXISTS author;;
DROP TABLE IF EXISTS category_sub;;
DROP TABLE IF EXISTS category;;
DROP TABLE IF EXISTS publisher;;
DROP TABLE IF EXISTS user_detail;;
DROP TABLE IF EXISTS user;;

-- 1. user 表
-- 新增 penalty_points 用於處罰機制
-- role 擴展支援市民
CREATE TABLE IF NOT EXISTS user
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '使用者ID',
    card_id         VARCHAR(50)                                         NOT NULL UNIQUE COMMENT '圖書證號，唯一',
    account         VARCHAR(100)                                        NOT NULL UNIQUE COMMENT '登入帳號，唯一',
    password        VARCHAR(255)                                        NOT NULL COMMENT '密碼雜湊值',
    role            ENUM ('ROLE_USER', 'ROLE_CITIZEN', 'ROLE_ADMIN')    NOT NULL DEFAULT 'ROLE_USER' COMMENT '使用者角色 (ROLE_USER:一般民眾, ROLE_CITIZEN:台中市民, ROLE_ADMIN:管理員)',
    penalty_points  INT                                                 NOT NULL DEFAULT 0 COMMENT '處罰點數 (累計10點停權30天)',
    status          ENUM ('PENDING', 'ACTIVE', 'SUSPENDED')             NOT NULL DEFAULT 'PENDING' COMMENT '帳號狀態 (PENDING:待開通, ACTIVE:已開通, SUSPENDED:停權中)',
    suspended_until TIMESTAMP                                           NULL COMMENT '帳號停權截止時間 (NULL表示未停權)'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='使用者帳號資訊表';


-- 2. user_detail 表 (無變更，但 email 可用於通知發送)
CREATE TABLE IF NOT EXISTS user_detail
(
    user_id    BIGINT PRIMARY KEY COMMENT '使用者ID (同時是主鍵和外鍵)',
    name       VARCHAR(100) NOT NULL COMMENT '使用者姓名',
    email      VARCHAR(255) UNIQUE COMMENT '電子郵件，唯一 (可為NULL，但如果存在則唯一，用於通知發送)',
    phone      VARCHAR(20) COMMENT '電話號碼',
    address    VARCHAR(255) COMMENT '住址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='使用者詳細資訊表';


-- 3. publisher 表 (無變更)
CREATE TABLE IF NOT EXISTS publisher
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '出版社ID',
    pub_name VARCHAR(255) NOT NULL UNIQUE COMMENT '出版社名稱，唯一'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='出版社資訊表';


-- 4. category 表 (無變更)
CREATE TABLE IF NOT EXISTS category
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分類ID',
    category_title VARCHAR(100) NOT NULL UNIQUE COMMENT '分類名稱，唯一 (e.g., 漫畫, 輕小說)'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='主分類表';


-- 5. category_sub 表 (無變更)
CREATE TABLE IF NOT EXISTS category_sub
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '子分類ID',
    category_id        BIGINT       NOT NULL COMMENT '所屬主分類ID',
    category_sub_title VARCHAR(100) NOT NULL COMMENT '子分類名稱',
    UNIQUE KEY uk_category_sub_title (category_id, category_sub_title), -- 同一個主分類下子分類名稱唯一
    FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='子分類表';


-- 6. author 表 (無變更)
CREATE TABLE IF NOT EXISTS author
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '作者ID',
    name VARCHAR(100) NOT NULL COMMENT '作者姓名'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='作者資訊表';


-- 7. series 表 (無變更)
CREATE TABLE IF NOT EXISTS series
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '系列ID',
    title VARCHAR(255) NOT NULL UNIQUE COMMENT '系列標題，唯一 (e.g., 鬼滅之刃系列)'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='書籍系列表';


-- 8. book 表 
-- 新增: average_rating 用於計算平均評分，可由 Trigger 或應用更新)
-- 新增: 借閱次數快照（配合trigger）
-- 新增: 上架日
-- 新增: 可否借閱快照（配合 trigger ，計算 bookcopy 借閱狀態）
CREATE TABLE IF NOT EXISTS book
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '書籍ID',
    title           VARCHAR(255) NOT NULL COMMENT '書籍標題',
    series_id       BIGINT                DEFAULT NULL COMMENT '系列ID (可為NULL，若無系列)',
    category_sub_id BIGINT       NOT NULL COMMENT '所屬子分類ID',
    publish_year    SMALLINT COMMENT '出版年份',
    publisher_id    BIGINT       NOT NULL COMMENT '出版社ID',
    image_url       VARCHAR(500) COMMENT '書籍封面圖片URL',
    ISBN            VARCHAR(20)  NOT NULL UNIQUE COMMENT '國際標準書號，唯一',
    representative  TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否為代表作 (1:是, 0:否)',
    
    average_rating  DECIMAL(2, 1)         DEFAULT NULL COMMENT '平均評分 (1.0-5.0，NULL表示尚無評分)', -- 若0分前端要顯示尚無評論
    rating_count    INT          NOT NULL DEFAULT 0 COMMENT '評分人數',
    
    -- 新增欄位
    added_date      DATE         NOT NULL DEFAULT CURRENT_DATE COMMENT '上架日期',
    total_loan_count INT         NOT NULL DEFAULT 0 COMMENT '累計借閱次數',
    
    FOREIGN KEY (series_id) REFERENCES series (id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (category_sub_id) REFERENCES category_sub (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (publisher_id) REFERENCES publisher (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='書籍資訊表';


-- 9. book_copy 表
-- status 註解更新
-- 新增：location
CREATE TABLE IF NOT EXISTS book_copy
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '實體書ID (副本ID)',
    book_id     BIGINT                    NOT NULL COMMENT '所屬書籍ID',
    unique_code VARCHAR(100)              NOT NULL UNIQUE COMMENT '每本實體書的唯一編碼',
    status      ENUM ('A', 'L', 'P', 'R', 'U') NOT NULL DEFAULT 'A' COMMENT '狀態 (A:Available 在館, L:Loaned 已借出, R:RESERVED 被預約, P:PROCESSING 處理中, U:UNAVALIBLE 下架)',
    location    VARCHAR(100)                       DEFAULT '新書上架區' COMMENT '書籍位置',
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_book_status (book_id, status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='書籍實體副本資訊表';


-- 10. loan 表 (無變更，但逾期點數計算基於 due_date vs return_date)
-- 新增: renew_count 續借次數（最多2次，每次加10天）
CREATE TABLE IF NOT EXISTS loan
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '借閱記錄ID',
    user_id      BIGINT                                  NOT NULL COMMENT '借閱者使用者ID',
    book_copy_id BIGINT                                  NOT NULL COMMENT '借閱的實體書ID',
    renew_count  INT                                     NOT NULL DEFAULT 0 COMMENT '續借次數',
    loan_date    TIMESTAMP                                        DEFAULT CURRENT_TIMESTAMP COMMENT '借閱日期',
    due_date     DATE                                    NOT NULL COMMENT '應歸還日期',
    return_date  TIMESTAMP                               NULL COMMENT '實際歸還日期 (NULL表示未歸還)',
    status       ENUM ('ON_LOAN', 'RETURNED', 'OVERDUE') NOT NULL DEFAULT 'ON_LOAN' COMMENT '借閱狀態',
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (book_copy_id) REFERENCES book_copy (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='書籍借閱記錄表';


-- 11. favorite 表 (無變更)
CREATE TABLE IF NOT EXISTS favorite
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
    user_id    BIGINT NOT NULL COMMENT '使用者ID',
    book_id    BIGINT NOT NULL COMMENT '收藏的書籍ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏時間',
    UNIQUE KEY uk_user_book_favorite (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='使用者收藏書籍表';


-- 12. reservation 表 (新增 type 欄位區分快速/一般預約；調整唯一鍵)
CREATE TABLE IF NOT EXISTS reservation
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '預約記錄ID',
    user_id         BIGINT                                                             NOT NULL COMMENT '預約者使用者ID',
    book_copy_id    BIGINT                                                             NOT NULL COMMENT '預約的實體書ID',
    queue_position  INT                                                                NOT NULL COMMENT '排隊順位（1表示第一順位）',
    reserve_date    TIMESTAMP                                                          DEFAULT CURRENT_TIMESTAMP COMMENT '預約日期',
    notify_date     TIMESTAMP                                                          NULL COMMENT '通知日期（書到館時通知使用者）',
    expiration_date DATE                                                               NOT NULL COMMENT '取書截止日期 (通知日期 +6天)',
    pickup_date     TIMESTAMP                                                          NULL COMMENT '實際取書日期 (轉為借閱時填入)',
    status          ENUM ('PENDING', 'AVAILABLE', 'PICKED_UP', 'EXPIRED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '預約狀態 (PENDING:等待中, AVAILABLE:可取書, PICKED_UP:已取書, EXPIRED:逾期未取, CANCELLED:已取消)',
    UNIQUE KEY uk_active_reservation (user_id, book_copy_id, status), -- 防止同一使用者對同一書重複活躍預約
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (book_copy_id) REFERENCES book_copy (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_user_status (user_id, status),
    INDEX idx_expiration (expiration_date, status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='書籍預約記錄表';
-- 變更說明：新增 type (區分預約，應用檢查 book_copy.status 是否在館決定可否快速)；唯一鍵保留但調整為活躍狀態 (非過期/取消)。


-- 13. tag 表 (無變更)
CREATE TABLE IF NOT EXISTS tag
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '標籤ID',
    title VARCHAR(100) NOT NULL UNIQUE COMMENT '標籤名稱，唯一 (e.g., 科幻, 搞笑)'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='書籍標籤表';


-- 14. book_author 表 (無變更)
CREATE TABLE IF NOT EXISTS book_author
(
    book_id   BIGINT NOT NULL COMMENT '書籍ID',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (author_id) REFERENCES author (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='書籍與作者關聯表';


-- 15. book_tag 表 (無變更)
CREATE TABLE IF NOT EXISTS book_tag
(
    book_id BIGINT NOT NULL COMMENT '書籍ID',
    tag_id  BIGINT NOT NULL COMMENT '標籤ID',
    PRIMARY KEY (book_id, tag_id),
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='書籍與標籤關聯表';

-- ==================================
-- 為了支援社群和通知功能，新增以下表：
-- ==================================

-- 16. review 表 (書籍評分，僅限開通使用者)
CREATE TABLE IF NOT EXISTS review
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '評分ID',
    user_id     BIGINT  NOT NULL COMMENT '評分使用者ID',
    book_id     BIGINT  NOT NULL COMMENT '書籍ID',
    rating      TINYINT NOT NULL COMMENT '評分 (1-5)',
    review_text TEXT COMMENT '評論內容',
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP COMMENT '評分時間',
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    likes_count INT     NOT NULL DEFAULT 0 COMMENT '按讚數',
    UNIQUE KEY uk_user_book_rating (user_id, book_id), -- 一人一書一評分
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_book_created (book_id, created_at DESC),
    INDEX idx_book_rating (book_id, rating),
    CHECK ( rating  >= 1 AND  rating  <= 5)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='書籍評分表';
-- 說明：評分後更新 book.average_rating (用 AVG(score) 計算)。
-- 說明：like_count 由 Trigger 或應用更新。


-- 17. review_like 表 (評論按讚)
CREATE TABLE IF NOT EXISTS review_like
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '按讚ID',
    user_id    BIGINT NOT NULL COMMENT '按讚使用者ID',
    review_id  BIGINT NOT NULL COMMENT '評論ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '按讚時間',
    UNIQUE KEY uk_user_comment_like (user_id, review_id), -- 一人一評論一讚
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`review_id`) REFERENCES `review`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='評論按讚表';
-- 說明：按讚後更新 review.like_count。


-- 18. notification 表 (個別通知，可辨識已讀與否) (系統通知，支援註冊歡迎、預約相關)
CREATE TABLE IF NOT EXISTS notification
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    user_id        BIGINT                                                                                              NOT NULL COMMENT '接收使用者ID',
    type           ENUM ('WELCOME', 'RESERVE_SUCCESS', 'RESERVE_EXPIRING', 'RESERVE_AVAILABLE', 'LOAN_DUE', 'PENALTY') NOT NULL COMMENT '通知類型',
    title          VARCHAR(200)                                                                                        NOT NULL COMMENT '通知標題',
    content        TEXT                                                                                                NOT NULL COMMENT '通知內容',
    created_at     TIMESTAMP                                                                                                    DEFAULT CURRENT_TIMESTAMP COMMENT '通知時間',
    is_read        TINYINT(1)                                                                                          NOT NULL DEFAULT 0 COMMENT '是否已讀 (0: 未讀, 1: 已讀)',
    related_id     BIGINT                                                                                              NULL COMMENT '相關ID (e.g., reservation.id)',
    reference_id   BIGINT                                                                                              NULL COMMENT '關聯記錄ID',
    reference_type ENUM ('RESERVATION', 'LOAN', 'PENALTY')                                                             NULL COMMENT '關聯類型',
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_user_read_created (user_id, is_read, created_at DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='使用者通知表';
-- 說明：應用邏輯產生通知 (e.g., 預約成功插入 RESERVE_SUCCESS)；Email 基於此表發送 (篩選未讀/特定類型)。


-- 以下考慮中

-- 19. announcement 表 (全站公告)
CREATE TABLE IF NOT EXISTS announcement
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '公告ID',
    title             VARCHAR(200)                            NOT NULL COMMENT '公告標題',
    content           TEXT                                    NOT NULL COMMENT '公告內容',
    announcement_type ENUM ('SYSTEM', 'EVENT', 'MAINTENANCE') NOT NULL COMMENT '公告類型 (SYSTEM:系統公告, EVENT:活動公告, MAINTENANCE:維護公告)',
    target_audience   ENUM ('ALL', 'GENERAL', 'CITIZEN')      NOT NULL DEFAULT 'ALL' COMMENT '目標對象 (ALL:全體, GENERAL:一般民眾, CITIZEN:市民)',
    is_active         TINYINT(1)                              NOT NULL DEFAULT 1 COMMENT '是否啟用 (0:停用, 1:啟用)',
    start_date        TIMESTAMP                               NOT NULL COMMENT '公告開始時間',
    end_date          TIMESTAMP                               NULL COMMENT '公告結束時間 (NULL表示永久有效)',
    created_at        TIMESTAMP                                        DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    created_by        BIGINT                                  NOT NULL COMMENT '建立者ID (管理員)',
    FOREIGN KEY (created_by) REFERENCES user (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_active_dates (is_active, start_date, end_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='全站公告表';

-- 20. announcement_read 表 (公告已讀記錄)
CREATE TABLE IF NOT EXISTS announcement_read
(
    user_id         BIGINT NOT NULL COMMENT '使用者ID',
    announcement_id BIGINT NOT NULL COMMENT '公告ID',
    read_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '已讀時間',
    PRIMARY KEY (user_id, announcement_id),
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (announcement_id) REFERENCES announcement (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='公告已讀記錄表';

-- 21. email_template 表 (Email 模板)
CREATE TABLE IF NOT EXISTS email_template
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模板ID',
    template_code VARCHAR(50)  NOT NULL UNIQUE COMMENT '模板代碼 (e.g., RESERVE_SUCCESS, LOAN_DUE)',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名稱',
    subject       VARCHAR(500) NOT NULL COMMENT 'Email主旨 (支援變數 {{var}})',
    body          TEXT         NOT NULL COMMENT 'Email內容 (支援變數 {{var}})',
    variables     VARCHAR(500) COMMENT '可用變數說明 (JSON格式)',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Email模板表';

-- 22. email_log 表 (Email 發送記錄)
CREATE TABLE IF NOT EXISTS email_log
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Email記錄ID',
    user_id         BIGINT                                       NULL COMMENT '接收Email的使用者ID (NULL表示批次發送)',
    template_code   VARCHAR(50)                                  NOT NULL COMMENT '使用的模板代碼',
    recipient_email VARCHAR(255)                                 NOT NULL COMMENT '收件者Email',
    sent_at         TIMESTAMP                                             DEFAULT CURRENT_TIMESTAMP COMMENT '發送時間',
    status          ENUM ('PENDING', 'SENT', 'FAILED')           NOT NULL DEFAULT 'PENDING' COMMENT '發送狀態',
    error_message   TEXT                                         NULL COMMENT '錯誤訊息 (如果發送失敗)',
    reference_id    BIGINT                                       NULL COMMENT '關聯記錄ID',
    reference_type  ENUM ('RESERVATION', 'LOAN', 'ANNOUNCEMENT') NULL COMMENT '關聯類型',
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_status_sent (status, sent_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Email發送記錄表';

-- ============================================
-- 補充：常用查詢索引優化
-- ============================================

-- 加速查詢使用者的借閱中書籍
CREATE INDEX idx_user_status_loan ON loan (user_id, status);

-- 加速查詢使用者的活動預約
CREATE INDEX idx_user_type_status ON reservation (user_id, status);

-- 加速查詢書籍的可用副本
CREATE INDEX idx_book_location ON book_copy (book_id, location, status);

-- 加速依評分排序書籍
CREATE INDEX idx_book_rating ON book (average_rating DESC, rating_count DESC);

-- ============================================
-- 結束
-- ============================================


SELECT * FROM user;
SELECT * FROM user_detail;
SELECT * FROM publisher;
SELECT * FROM category;
SELECT * FROM category_sub;
SELECT * FROM author;
SELECT * FROM series;
SELECT * FROM book;
SELECT * FROM book_copy;
SELECT * FROM loan;
SELECT * FROM favorite;
SELECT * FROM reservation;
SELECT * FROM tag;
SELECT * FROM book_author;
SELECT * FROM book_tag;
SELECT * FROM review;
SELECT * FROM review_like;
SELECT * FROM notification;
SELECT * FROM announcement;
SELECT * FROM announcement_read;
SELECT * FROM email_template;
SELECT * FROM email_log;

SELECT l.id, b.id, b.title, b.image_url, bc.unique_code, l.loan_date, l.due_date, l.return_date, GROUP_CONCAT(DISTINCT a.name ORDER BY a.name SEPARATOR ', ') AS author_name
FROM loan l
         JOIN book_copy bc ON l.book_copy_id = bc.id
         JOIN book b ON bc.book_id = b.id
         JOIN book_author ba ON b.id = ba.book_id
         JOIN author a ON ba.author_id = a.id
WHERE l.user_id = 1
  AND l.status = 'ON_LOAN'
GROUP BY l.id, b.id, b.title, b.image_url, bc.unique_code, l.loan_date, l.due_date, l.return_date
ORDER BY l.loan_date DESC ;