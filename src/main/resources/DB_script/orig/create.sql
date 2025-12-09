CREATE DATABASE IF NOT EXISTS MariaLibraryV3;
USE MariaLibraryV3;

DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS user_detail;
DROP TABLE IF EXISTS publisher;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS category_sub;
DROP TABLE IF EXISTS author;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS book_copy;
DROP TABLE IF EXISTS loan;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS reservation_status;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS book_author;
DROP TABLE IF EXISTS book_tag;

-- 啟用 Foreign Key 檢查 (預設是開啟的，但明確寫出更佳)
SET FOREIGN_KEY_CHECKS = 1;


-- 1. user 表
CREATE TABLE IF NOT EXISTS user (
                                      `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '使用者ID',
                                      `card_id` VARCHAR(50) NOT NULL UNIQUE COMMENT '圖書證號，唯一',
                                      `account` VARCHAR(100) NOT NULL UNIQUE COMMENT '登入帳號，唯一',
                                      `password` VARCHAR(255) NOT NULL COMMENT '密碼雜湊值',
                                      `role` VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER' COMMENT '使用者角色 (例如: ROLE_USER, ROLE_ADMIN)',
                                      `suspended_until` TIMESTAMP NULL COMMENT '帳號凍結截止時間 (NULL表示未凍結)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者帳號資訊表';

-- 2. user_detail 表
CREATE TABLE IF NOT EXISTS user_detail (
                                             `user_id` BIGINT PRIMARY KEY COMMENT '使用者ID (同時是主鍵和外鍵)',
                                             `name` VARCHAR(100) NOT NULL COMMENT '使用者姓名',
                                             `email` VARCHAR(255) UNIQUE COMMENT '電子郵件，唯一 (可為NULL，但如果存在則唯一)',
                                             `phone` VARCHAR(20) COMMENT '電話號碼',
                                             `address` VARCHAR(255) COMMENT '住址',
                                             `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
                                             `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
                                             FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者詳細資訊表';
-- SQL 的外鍵約束（FOREIGN KEY）只會在刪除（ON DELETE）和更新（ON UPDATE）時觸發連動，
-- 新增（INSERT）時不會自動幫你新增相關表的資料。
-- 新增時必須先確保主表（如 user）有資料，然後再插入子表（如 user_detail），這部分只能由 Java 端（或其他應用程式邏輯）來處理，SQL 本身無法自動連動新增。
-- `updated_at` 欄位的 `ON UPDATE CURRENT_TIMESTAMP` 屬性，會在該資料列被 SQL UPDATE 時自動更新時間，不需要 Java 撰寫邏輯。
-- 只要有 SQL 層的 UPDATE 行為（不論是手動或由應用程式執行），這個欄位就會自動更新。

-- 3. publisher 表
CREATE TABLE IF NOT EXISTS publisher (
                                           `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '出版社ID',
                                           `pub_name` VARCHAR(255) NOT NULL UNIQUE COMMENT '出版社名稱，唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出版社資訊表';

-- 4. category 表 (第一層)
CREATE TABLE IF NOT EXISTS category (
                                          `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分類ID',
                                          `category_title` VARCHAR(100) NOT NULL UNIQUE COMMENT '分類名稱，唯一 (e.g., 漫畫, 輕小說)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主分類表';

-- 5. category_sub 表 (第二層)
CREATE TABLE IF NOT EXISTS category_sub (
                                              `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '子分類ID',
                                              `category_id` BIGINT NOT NULL COMMENT '所屬主分類ID',
                                              `category_sub_title` VARCHAR(100) NOT NULL COMMENT '子分類名稱',
                                              UNIQUE KEY `uk_category_sub_title` (`category_id`, `category_sub_title`), -- 同一個主分類下子分類名稱唯一
                                              FOREIGN KEY (`category_id`) REFERENCES `category`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='子分類表';

-- 6. author 表
CREATE TABLE IF NOT EXISTS author (
                                        `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '作者ID',
                                        `name` VARCHAR(100) NOT NULL COMMENT '作者姓名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作者資訊表';

-- 7.0 series 表 (可選，若有書籍系列)
CREATE TABLE IF NOT EXISTS series(
                                    `id`    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '系列ID',
                                    `title` VARCHAR(255) NOT NULL UNIQUE COMMENT '系列標題，唯一 (e.g., 鬼滅之刃系列)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書籍系列表';

-- 7. book 表
CREATE TABLE IF NOT EXISTS book (
                                      `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '書籍ID',
                                      `title` VARCHAR(255) NOT NULL COMMENT '書籍標題',
                                      `series_id` BIGINT DEFAULT NULL COMMENT '系列ID (可為NULL，若無系列)',
                                      `category_sub_id` BIGINT NOT NULL COMMENT '所屬子分類ID',
                                      `publish_year` SMALLINT COMMENT '出版年份',
                                      `publisher_id` BIGINT NOT NULL COMMENT '出版社ID',
                                      `price` DECIMAL(10, 2) COMMENT '書籍價格',
                                      `image_url` VARCHAR(500) COMMENT '書籍封面圖片URL',
                                      `ISBN` VARCHAR(20) NOT NULL UNIQUE COMMENT '國際標準書號，唯一',
                                      `representative` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否為代表作 (1:是, 0:否)',
                                      FOREIGN KEY (`series_id`) REFERENCES `series`(`id`) ON DELETE SET NULL ON UPDATE CASCADE, -- 系列刪除時，書籍的series_id設為NULL
                                      FOREIGN KEY (`category_sub_id`) REFERENCES `category_sub`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE, -- 避免「懸空」資料，即子分類被刪除時，書籍不能存在；也可以說如果分類下還有書，SQL會阻止刪除該分類
                                      FOREIGN KEY (`publisher_id`) REFERENCES `publisher`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書籍資訊表';

-- 8. book_copy 表
CREATE TABLE IF NOT EXISTS book_copy (
                                           `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '實體書ID (副本ID)',
                                           `book_id` BIGINT NOT NULL COMMENT '所屬書籍ID',
                                           `unique_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '每本實體書的唯一編碼',
                                           `status` ENUM('A', 'L', 'R') NOT NULL COMMENT '狀態 (A:Available, L:Loaned, R:Reserved)',
                                           FOREIGN KEY (`book_id`) REFERENCES `book`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書籍實體副本資訊表';

-- 9. loan 表
CREATE TABLE IF NOT EXISTS loan (
                                      `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '借閱記錄ID',
                                      `user_id` BIGINT NOT NULL COMMENT '借閱者使用者ID',
                                      `book_copy_id` BIGINT NOT NULL COMMENT '借閱的實體書ID',
                                      `loan_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '借閱日期',
                                      `due_date` DATE NOT NULL COMMENT '應歸還日期',
                                      `return_date` TIMESTAMP NULL COMMENT '實際歸還日期 (NULL表示未歸還)',
                                      `status` ENUM('ON_LOAN', 'RETURNED', 'OVERDUE') NOT NULL DEFAULT 'ON_LOAN' COMMENT '借閱狀態',
                                      FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
                                      FOREIGN KEY (`book_copy_id`) REFERENCES `book_copy`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書籍借閱記錄表';
-- 把 book_copies_id 改為 book_copy_id --
-- ALTER TABLE loan CHANGE book_copies_id book_copy_id BIGINT NOT NULL COMMENT '借閱的實體書ID';

-- 10. favorite 表
CREATE TABLE IF NOT EXISTS favorite (
                                          `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
                                          `user_id` BIGINT NOT NULL COMMENT '使用者ID',
                                          `book_id` BIGINT NOT NULL COMMENT '收藏的書籍ID',
                                          UNIQUE KEY `uk_user_book_favorite` (`user_id`, `book_id`), -- 確保使用者不會重複收藏同一本書
                                          FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                          FOREIGN KEY (`book_id`) REFERENCES `book`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者收藏書籍表';

-- 11. reservation_status 表
CREATE TABLE IF NOT EXISTS reservation_status (
                                                    `id` TINYINT AUTO_INCREMENT PRIMARY KEY COMMENT '預約狀態ID',
                                                    `title` VARCHAR(50) NOT NULL UNIQUE COMMENT '預約狀態名稱 (e.g., 可取, 已取, 取消, 過期)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='預約狀態定義表';

-- 12. reservation 表
CREATE TABLE IF NOT EXISTS reservation (
                                             `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '預約記錄ID',
                                             `user_id` BIGINT NOT NULL COMMENT '預約者使用者ID',
                                             `book_copy_id` BIGINT NOT NULL COMMENT '預約的實體書ID',
                                             `reserve_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '預約日期',
                                             `expiration_date` DATE NOT NULL COMMENT '預約截止日期',
                                             `pickup_date` TIMESTAMP NULL COMMENT '實際取書日期 (轉為借閱時填入)',
                                             `reservation_status_id` TINYINT NOT NULL COMMENT '預約狀態ID',
    -- 確保一個使用者對同一本特定實體書只有一個活動預約 (例如尚未過期或尚未取書)
    -- UNIQUE KEY `uk_active_reservation` (`user_id`, `book_copy_id`, `reservation_status_id`), -- 較複雜的唯一鍵，需配合業務邏輯
                                             FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
                                             FOREIGN KEY (`book_copy_id`) REFERENCES `book_copy`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
                                             FOREIGN KEY (`reservation_status_id`) REFERENCES `reservation_status`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書籍預約記錄表';

-- 13. tag 表
CREATE TABLE IF NOT EXISTS tag (
                                     `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '標籤ID',
                                     `title` VARCHAR(100) NOT NULL UNIQUE COMMENT '標籤名稱，唯一 (e.g., 科幻, 搞笑)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書籍標籤表';

-- 14. book_author 表 (中間表)
CREATE TABLE IF NOT EXISTS book_author (
                                             `book_id` BIGINT NOT NULL COMMENT '書籍ID',
                                             `author_id` BIGINT NOT NULL COMMENT '作者ID',
                                             PRIMARY KEY (`book_id`, `author_id`), -- 聯合主鍵
                                             FOREIGN KEY (`book_id`) REFERENCES `book`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                             FOREIGN KEY (`author_id`) REFERENCES `author`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書籍與作者關聯表';

-- 15. book_tag 表 (中間表)
CREATE TABLE IF NOT EXISTS book_tag (
                                          `book_id` BIGINT NOT NULL COMMENT '書籍ID',
                                          `tag_id` BIGINT NOT NULL COMMENT '標籤ID',
                                          PRIMARY KEY (`book_id`, `tag_id`), -- 聯合主鍵
                                          FOREIGN KEY (`book_id`) REFERENCES `book`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                          FOREIGN KEY (`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE ON UPDATE CASCADE -- 更正: 外鍵指向 tag.id
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書籍與標籤關聯表';


-- 顯示目前所有資料表
SHOW TABLES;
-- 顯示每個資料表資料
SELECT * FROM user ORDER BY id ASC;
SELECT * FROM user_detail ORDER BY user_id ASC;
SELECT * FROM publisher ORDER BY id ASC;
SELECT * FROM category ORDER BY id ASC;
SELECT * FROM category_sub ORDER BY id ASC;
SELECT * FROM author ORDER BY id ASC;
SELECT * FROM series ORDER BY id ASC;
SELECT * FROM book ORDER BY id ASC;
SELECT * FROM book_copy ORDER BY id ASC;
SELECT * FROM loan ORDER BY id ASC;
SELECT * FROM favorite ORDER BY id ASC;
SELECT * FROM reservation_status ORDER BY id ASC;
SELECT * FROM reservation ORDER BY id ASC;
SELECT * FROM tag ORDER BY id ASC;
SELECT * FROM book_author ORDER BY book_id ASC, author_id ASC;
SELECT * FROM book_tag ORDER BY book_id ASC, tag_id ASC;

-- 跨表查詢借閱數最多的前五名，顯示book_id, book_title, author_name, pub_name, borrow_count
SELECT b.id AS book_id,
       b.title AS book_title,
       GROUP_CONCAT(DISTINCT a.name ORDER BY a.name SEPARATOR ', ') AS author_name,
       p.pub_name, COUNT(l.id) AS borrow_count
FROM book b
    JOIN book_copy bc ON b.id = bc.book_id
    JOIN loan l ON bc.id = l.book_copy_id
    JOIN publisher p ON b.publisher_id = p.id
    JOIN book_author ba ON b.id = ba.book_id
    JOIN author a ON ba.author_id = a.id
GROUP BY b.id, b.title, p.pub_name
ORDER BY borrow_count DESC
LIMIT 10;

-- 跨表查詢指定category底下借閱數最多的前五名，顯示book_id, book_title, author_name, pub_name, borrow_count
SELECT b.id AS book_id,
       b.title AS book_title,
       GROUP_CONCAT(DISTINCT a.name ORDER BY a.name SEPARATOR ', ') AS author_name,
       p.pub_name, COUNT(l.id) AS borrow_count
FROM book b
    JOIN book_copy bc ON b.id = bc.book_id
    JOIN loan l ON bc.id = l.book_copy_id
    JOIN publisher p ON b.publisher_id = p.id
    JOIN book_author ba ON b.id = ba.book_id
    JOIN author a ON ba.author_id = a.id
    JOIN category_sub cs ON b.category_sub_id = cs.id
    JOIN category c ON cs.category_id = c.id
WHERE c.category_title = '漫畫' -- 替換為你想查詢的主分類名稱
GROUP BY b.id, b.title, p.pub_name
ORDER BY borrow_count DESC
LIMIT 5;

-- 跨表查詢指定category底下最新的前五名，顯示book_id, book_title, author_name, pub_name, publish_year
SELECT b.id AS book_id,
       b.title AS book_title,
       GROUP_CONCAT(DISTINCT a.name ORDER BY a.name SEPARATOR ', ') AS author_name,
       p.pub_name, b.publish_year
FROM book b
    JOIN publisher p ON b.publisher_id = p.id
    JOIN book_author ba ON b.id = ba.book_id
    JOIN author a ON ba.author_id = a.id
    JOIN category_sub cs ON b.category_sub_id = cs.id
    JOIN category c ON cs.category_id = c.id
WHERE c.category_title = '漫畫' -- 替換為你想查詢的主分類名稱
GROUP BY b.id, b.title, p.pub_name, b.publish_year
ORDER BY b.publish_year DESC, b.id DESC
LIMIT 5;

-- 跨表查詢，列出所有book的tag，顯示book_id, book_title, tag_title
SELECT b.id AS book_id,
       b.title AS book_title,
       GROUP_CONCAT(DISTINCT t.title ORDER BY t.title SEPARATOR ', ') AS tag_title
FROM book b
    JOIN book_tag bt ON b.id = bt.book_id
    JOIN tag t ON bt.tag_id = t.id
GROUP BY b.id, b.title
ORDER BY b.id ASC;

-- 跨表查詢熱門tag前10名，顯示tag_id, tag_title, book_count
SELECT
    t.id AS tag_id,
    t.title AS tag_title,
    COUNT(bt.book_id) AS book_count
FROM tag t
    JOIN book_tag bt ON t.id = bt.tag_id
GROUP BY t.id, t.title
ORDER BY book_count DESC
LIMIT 10;

-- 跨表查詢，特定category下，從 loan 取得熱門 tag 前10名，顯示tag_id, tag_title, book_count
SELECT
    t.id AS tag_id,
    t.title AS tag_title,
    COUNT(DISTINCT l.id) AS book_count
FROM loan l
    JOIN book_copy bc ON l.book_copy_id = bc.id
    JOIN book b ON bc.book_id = b.id
    JOIN book_tag bt ON b.id = bt.book_id
    JOIN tag t ON bt.tag_id = t.id
    JOIN category_sub cs ON b.category_sub_id = cs.id
    JOIN category c ON cs.category_id = c.id
WHERE c.category_title = '漫畫' -- 這裡替換為你要查詢的主分類名稱
GROUP BY t.id, t.title
ORDER BY book_count DESC
LIMIT 10;

-- 跨表查詢，loan 表中，loan_id, book_id, book_title, tag_title
SELECT
    l.id AS loan_id,
    b.id AS book_id,
    b.title AS book_title,
    GROUP_CONCAT(DISTINCT t.title ORDER BY t.title SEPARATOR ', ') AS tag_title
FROM loan l
    JOIN book_copy bc ON l.book_copy_id = bc.id
    JOIN book b ON bc.book_id = b.id
    JOIN book_tag bt ON b.id = bt.book_id
    JOIN tag t ON bt.tag_id = t.id
GROUP BY l.id, b.id, b.title
ORDER BY l.id ASC;

DROP TABLE IF EXISTS book_tag;
DROP TABLE IF EXISTS book_author;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_status;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS loan;
DROP TABLE IF EXISTS book_copy;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS series;
DROP TABLE IF EXISTS author;
DROP TABLE IF EXISTS category_sub;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS publisher;
DROP TABLE IF EXISTS user_detail;
DROP TABLE IF EXISTS user;

-- 把某幾本書的 representative 設為 1
UPDATE book SET representative = 1 WHERE id IN (44,45); -- 替換為你要設為代表作的書籍ID