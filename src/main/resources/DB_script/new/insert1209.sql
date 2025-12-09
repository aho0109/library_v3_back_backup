USE MariaLibraryV3;
Use MariaLibraryV3Cloud;


SELECT * FROM `user`;
-- 1. 插入 user 資料
-- 角色修正為 ROLE_ADMIN 和 ROLE_USER
INSERT INTO `user` (`card_id`, `account`, `password`, `role`, `status`) VALUES
                                                                  ('LIB000', 'adminJAVA1925', '$2a$10$GNgVf2y7FOsOIhyuA6KIQON0YHNOu7FApwR2b1xqWLTfF9C2So3FS', 'ROLE_ADMIN', 'ACTIVE'),
                                                                  ('LIB001', 'citizen777', '$2a$10$nyAECeDBNJqb.3.sucbVSO4r75ttGv9aZBYjzbuPvr3TwswZtts3W', 'ROLE_CITIZEN', 'ACTIVE'),
                                                                  ('LIB002', 'user1925', '$2a$10$.aHdOI1H.EhAckbJ4dGE2OIICp4oJqXllt3INC8LXSuI5TjH8PGtK', 'ROLE_USER', 'ACTIVE');

-- 取得剛剛插入的 user IDs 以供後續使用
SET @admin_user_id_1925 = (SELECT id FROM `user` WHERE account = 'adminJAVA1925');
SET @user1925 = (SELECT id FROM `user` WHERE account = 'user1925');
SET @citizen777 = (SELECT id FROM `user` WHERE account = 'citizen777');


-- 2. 插入 user_detail 資料
INSERT INTO `user_detail` (`user_id`, `name`, `email`, `phone`, `address`) VALUES
                                                                               (@admin_user_id_1925, '管理員', 'adminJAVA1925@example.com', '0999999999', '管理員的家'),
                                                                               (@citizen777, '市民', 'citizen777@example.com', '0988888888', '市民的家'),
                                                                               (@user1925, '一般人', 'user1925@library.com', '0977777777', '一般人的家');




-- 3. 插入 publisher 資料
INSERT INTO `publisher` (`pub_name`) VALUES
                                         ('東立'),
                                         ('尖端'),
                                         ('傑克魔豆'),
                                         ('皇冠文化'),
                                         ('獨步文化'),
                                         ('木馬文化'),
                                         ('青文'),
                                         ('蓋亞'),
                                         ('大田出版社'),
                                         ('VOGUE出版社'),
                                         ('城邦出版集團'),
                                         ('碁峰'),
                                         ('墨刻'),
                                         ('台灣角川');

SET @東立 = (SELECT id FROM `publisher` WHERE pub_name = '東立');
SET @尖端 = (SELECT id FROM `publisher` WHERE pub_name = '尖端');
SET @傑克魔豆 = (SELECT id FROM `publisher` WHERE pub_name = '傑克魔豆');
SET @皇冠文化 = (SELECT id FROM `publisher` WHERE pub_name = '皇冠文化');
SET @獨步文化 = (SELECT id FROM `publisher` WHERE pub_name = '獨步文化');
SET @木馬文化 = (SELECT id FROM `publisher` WHERE pub_name = '木馬文化');
SET @青文 = (SELECT id FROM `publisher` WHERE pub_name = '青文');
SET @蓋亞 = (SELECT id FROM `publisher` WHERE pub_name = '蓋亞');
SET @大田出版社 = (SELECT id FROM `publisher` WHERE pub_name = '大田出版社');
SET @VOGUE出版社 = (SELECT id FROM `publisher` WHERE pub_name = 'VOGUE出版社');
SET @城邦出版集團 = (SELECT id FROM `publisher` WHERE pub_name = '城邦出版集團');
SET @碁峰 = (SELECT id FROM `publisher` WHERE pub_name = '碁峰');
SET @墨刻 = (SELECT id FROM `publisher` WHERE pub_name = '墨刻');


-- 4. 插入 category 資料 (第一層)
INSERT INTO `category` (`category_title`) VALUES
                                              ('漫畫'),
                                              ('輕小說'),
                                              ('文學'),
                                              ('雜誌'),
                                              ('實用書');

SET @漫畫 = (SELECT id FROM `category` WHERE category_title = '漫畫');
SET @輕小說 = (SELECT id FROM `category` WHERE category_title = '輕小說');
SET @文學 = (SELECT id FROM `category` WHERE category_title = '文學');
SET @雜誌 = (SELECT id FROM `category` WHERE category_title = '雜誌');
SET @實用書 = (SELECT id FROM `category` WHERE category_title = '實用書');


-- 5. 插入 category_sub 資料 (第二層)
INSERT INTO `category_sub` (`category_id`, `category_sub_title`) VALUES
                                                                     (@漫畫, '少年漫畫'),
                                                                     (@漫畫, '少女漫畫'),
                                                                     (@輕小說, '日本輕小說'),
                                                                     (@輕小說, '華語輕小說'),
                                                                     (@文學, '日本文學'),
                                                                     (@文學, '歐美文學'),
                                                                     (@雜誌, '時尚'),
                                                                     (@雜誌, '科技'),
                                                                     (@實用書, '程式設計'),
                                                                     (@實用書, '生活旅遊');

SET @少年漫畫 = (SELECT id FROM `category_sub` WHERE category_sub_title = '少年漫畫');;
SET @少女漫畫 = (SELECT id FROM `category_sub` WHERE category_sub_title = '少女漫畫');
SET @日本輕小說 = (SELECT id FROM `category_sub` WHERE category_sub_title = '日本輕小說');
SET @華語輕小說 = (SELECT id FROM `category_sub` WHERE category_sub_title = '華語輕小說');
SET @日本文學 = (SELECT id FROM `category_sub` WHERE category_sub_title = '日本文學');
SET @歐美文學 = (SELECT id FROM `category_sub` WHERE category_sub_title = '歐美文學');
SET @時尚 = (SELECT id FROM `category_sub` WHERE category_sub_title = '時尚');
SET @科技 = (SELECT id FROM `category_sub` WHERE category_sub_title = '科技');
SET @程式設計 = (SELECT id FROM `category_sub` WHERE category_sub_title = '程式設計');
SET @生活旅遊 = (SELECT id FROM `category_sub` WHERE category_sub_title = '生活旅遊');


-- 6. 插入 author 資料
INSERT INTO `author` (`name`) VALUES
                                  ('荒川弘'), -- 1
                                  ('空知英秋'), -- 2
                                  ('村田雄介'), -- 3
                                  ('ONE'), -- 4
                                  ('Robico'), -- 5
                                  ('矢澤愛'), -- 6
                                  ('奈須きのこ'), -- 7
                                  ('沖方丁'), -- 8
                                  ('護玄'), -- 9
                                  ('御我'), -- 10
                                  ('乙一'), -- 11
                                  ('石田衣良'), -- 12
                                  ('J.K. 羅琳'), -- 13
                                  ('C.S. 路易斯'), -- 14
                                  ('vogue 編輯部'), -- 15
                                  ('PChome 雜誌編輯群'), -- 16
                                  ('林信良'), -- 17
                                  ('墨刻編輯部'); -- 21

SET @荒川弘 = (SELECT id FROM `author` WHERE name = '荒川弘');
SET @空知英秋 = (SELECT id FROM `author` WHERE name = '空知英秋');
SET @村田雄介 = (SELECT id FROM `author` WHERE name = '村田雄介');
SET @ONE = (SELECT id FROM `author` WHERE name = 'ONE');
SET @Robico = (SELECT id FROM `author` WHERE name = 'Robico');
SET @矢澤愛 = (SELECT id FROM `author` WHERE name = '矢澤愛');
SET @奈須きのこ = (SELECT id FROM `author` WHERE name = '奈須きのこ');
SET @沖方丁 = (SELECT id FROM `author` WHERE name = '沖方丁');
SET @護玄 = (SELECT id FROM `author` WHERE name = '護玄');
SET @御我 = (SELECT id FROM `author` WHERE name = '御我');
SET @乙一 = (SELECT id FROM `author` WHERE name = '乙一');
SET @石田衣良 = (SELECT id FROM `author` WHERE name = '石田衣良');
SET @羅琳 = (SELECT id FROM `author` WHERE name = 'J.K. 羅琳');
SET @路易斯 = (SELECT id FROM `author` WHERE name = 'C.S. 路易斯');
SET @vogue編輯部 = (SELECT id FROM `author` WHERE name = 'vogue 編輯部');
SET @PChome雜誌編輯群 = (SELECT id FROM `author` WHERE name = 'PChome 雜誌編輯群');
SET @林信良 = (SELECT id FROM `author` WHERE name = '林信良');
SET @墨刻編輯部 = (SELECT id FROM `author` WHERE name = '墨刻編輯部');


-- 7. 插入 tag 資料
INSERT INTO `tag` (`title`) VALUES
                                ('科幻'),
                                ('熱血'),
                                ('搞笑'),
                                ('恐怖'),
                                ('推理'),
                                ('懸疑'),
                                ('已完結'),
                                ('動畫化'),
                                ('新番'),
                                ('經典'),
                                ('異世界'),
                                ('戀愛'),
                                ('青春'),
                                ('溫馨'),
                                ('冒險'),
                                ('奇幻'),
                                ('魔法'),
                                ('戰鬥'),
                                ('胃痛'),
                                ('東北亞'),
                                ('美洲'),
                                ('亞洲'),
                                ('國外旅遊'),
                                ('年度推薦'),
                                ('標籤001'),
                                ('標籤002'),
                                ('標籤003'),
                                ('標籤004');

SET @科幻 = (SELECT id FROM `tag` WHERE title = '科幻');
SET @熱血 = (SELECT id FROM `tag` WHERE title = '熱血');
SET @搞笑 = (SELECT id FROM `tag` WHERE title = '搞笑');
SET @恐怖 = (SELECT id FROM `tag` WHERE title = '恐怖');
SET @推理 = (SELECT id FROM `tag` WHERE title = '推理');
SET @懸疑 = (SELECT id FROM `tag` WHERE title = '懸疑');
SET @已完結 = (SELECT id FROM `tag` WHERE title = '已完結');
SET @動畫化 = (SELECT id FROM `tag` WHERE title = '動畫化');
SET @新番 = (SELECT id FROM `tag` WHERE title = '新番');
SET @經典 = (SELECT id FROM `tag` WHERE title = '經典');
SET @異世界 = (SELECT id FROM `tag` WHERE title = '異世界');
SET @戀愛 = (SELECT id FROM `tag` WHERE title = '戀愛');
SET @青春 = (SELECT id FROM `tag` WHERE title = '青春');
SET @溫馨 = (SELECT id FROM `tag` WHERE title = '溫馨');
SET @冒險 = (SELECT id FROM `tag` WHERE title = '冒險');
SET @奇幻 = (SELECT id FROM `tag` WHERE title = '奇幻');
SET @魔法 = (SELECT id FROM `tag` WHERE title = '魔法');
SET @戰鬥 = (SELECT id FROM `tag` WHERE title = '戰鬥');
SET @胃痛 = (SELECT id FROM `tag` WHERE title = '胃痛');
SET @東北亞 = (SELECT id FROM `tag` WHERE title = '東北亞');
SET @美洲 = (SELECT id FROM `tag` WHERE title = '美洲');
SET @亞洲 = (SELECT id FROM `tag` WHERE title = '亞洲');
SET @國外旅遊 = (SELECT id FROM `tag` WHERE title = '國外旅遊');
SET @年度推薦 = (SELECT id FROM `tag` WHERE title = '年度推薦');
SET @標籤001 = (SELECT id FROM `tag` WHERE title = '標籤001');
SET @標籤002 = (SELECT id FROM `tag` WHERE title = '標籤002');
SET @標籤003 = (SELECT id FROM `tag` WHERE title = '標籤003');
SET @標籤004 = (SELECT id FROM `tag` WHERE title = '標籤004');

-- 8.0 插入 series 資料
INSERT INTO `series` (`title`) VALUES
    ('鋼之鍊金術師'),
    ('池袋西口公園');
SET @鋼之鍊金術師 = (SELECT id FROM `series` WHERE title = '鋼之鍊金術師');
SET @池袋西口公園 = (SELECT id FROM `series` WHERE title = '池袋西口公園');


-- 8. 插入 book 資料
INSERT INTO `book` (`title`,`series_id`, `category_sub_id`, `publish_year`, `publisher_id`, `image_url`, `ISBN`, `representative`)
VALUES
    ('鋼之鍊金術師 (1)', @鋼之鍊金術師, @少年漫畫, 2002, @東立,  'https://taiwan-image.bookwalker.com.tw/product/247998/247998_1.jpg', '9789861146096', 0),
    ('鋼之鍊金術師 (24)', @鋼之鍊金術師, @少年漫畫, 2010, @東立,  'https://taiwan-image.bookwalker.com.tw/product/249302/249302_1.jpg', '9780747532740', 0),
    ('鋼之鍊金術師 (25)', @鋼之鍊金術師, @少年漫畫, 2010, @東立,  'https://taiwan-image.bookwalker.com.tw/product/249303/249303_1.jpg', '9780747532741', 0),
    ('鋼之鍊金術師 (26)', @鋼之鍊金術師, @少年漫畫, 2010, @東立,  'https://taiwan-image.bookwalker.com.tw/product/249304/249304_1.jpg', '9780747532742', 0),
    ('鋼之鍊金術師 (27)', @鋼之鍊金術師, @少年漫畫, 2011, @東立,  'https://taiwan-image.bookwalker.com.tw/product/249305/249305_1.jpg', '9780747532743', 1),
    ('銀之匙 (1)', null, @少年漫畫, 2012, @東立,  'https://taiwan-image.bookwalker.com.tw/product/65237/65237_1.jpg', '9789861090726', 1),
    ('銀之匙 (15)', null, @少年漫畫, 2020, @東立,  'https://taiwan-image.bookwalker.com.tw/product/107018/107018_1.jpg', '9789572650233', 1),
    ('銀魂 (1)', null, @少年漫畫, 2004, @東立,  'https://taiwan-image.bookwalker.com.tw/product/10295/10295_1.jpg', '9784088736235', 1),
    ('銀魂 (77)', null, @少年漫畫, 2019, @東立,  'https://taiwan-image.bookwalker.com.tw/product/81143/81143_1.jpg', '9789572637463', 1),
    ('ONE-PUNCH MAN 一拳超人 (1)', null, @少年漫畫, 2014, @東立,  'https://taiwan-image.bookwalker.com.tw/product/237990/237990_1.jpg', '9789864319305', 1),
    ('ONE-PUNCH MAN 一拳超人 (32)', null, @少年漫畫, 2025, @東立,  'https://taiwan-image.bookwalker.com.tw/product/252745/252745_1.jpg', '9786260236847', 1),
    ('路人超能100 (1)', null, @少年漫畫, 2019, @青文,  'https://taiwan-image.bookwalker.com.tw/product/28443/28443_1.jpg', '9789863560029', 1),
    ('路人超能100 (16)', null, @少年漫畫, 2019, @青文,  'https://taiwan-image.bookwalker.com.tw/product/59372/59372_1.jpg', '9789863568841', 1),
    ('鄰座的怪同學 (1)', null, @少女漫畫, 2009, @東立,  'https://taiwan-image.bookwalker.com.tw/product/4769/4769_1.jpg', '9789861045672', 1),
    ('鄰座的怪同學 (13)', null, @少女漫畫, 2014, @東立,  'https://taiwan-image.bookwalker.com.tw/product/4781/4781_1.jpg', '9789863482239', 1),
    ('NANA (1)', null, @少女漫畫, 2001, @尖端,  'https://taiwan-image.bookwalker.com.tw/product/13507/13507_1.jpg', '9787572908712', 1),
    ('NANA (21)', null, @少女漫畫, 2009, @尖端,  'https://taiwan-image.bookwalker.com.tw/product/13528/13528_1.jpg', '9786263164819', 1),
    ('空之境界 (上)', null, @日本輕小說, 2005, @傑克魔豆,  'https://upload.wikimedia.org/wikipedia/zh/0/0e/Kara_no_Kyoukai.jpg', '9867459474', 1),
    ('空之境界 (下)', null, @日本輕小說, 2005, @傑克魔豆,  'https://m.media-amazon.com/images/I/41o5FBiRfcL._UF1000,1000_QL80_.jpg', '9867459512', 1),
    ('殼中少女01：壓縮', null, @日本輕小說, 2006, @尖端,  'https://upload.wikimedia.org/wikipedia/zh/4/49/%E5%A3%B3%E4%B8%AD%E5%B0%91%E5%A5%B30_.jpg', '9571031526', 1),
    ('殼中少女03：排氣', null, @日本輕小說, 2006, @尖端,  'https://tw.linovelib.com/files/article/image/0/250/250s.jpg', '957103214X', 1),
    ('特殊傳說 新版vol.1 不存在的學園！', null, @華語輕小說, 2012, @蓋亞,  'https://img.pchome.com.tw/cs/items/DJBR4UD900IL86H/000001_1744882197.jpg', '9789866157936', 1),
    ('特殊傳說 新版vol.10 那之後...', null, @華語輕小說, 2013, @蓋亞,  'https://s.eslite.com/b2b/newItem/ebook_init/main1_127783.jpg', '9789863190745', 1),
    ('吾命騎士 vol.1 騎士基礎理論', null, @華語輕小說, 2007, @蓋亞,  'https://s.eslite.com/b2b/newItem/ebook_init/main1_127783.jpg', '9789868352902', 1),
    ('吾命騎士 vol.8 終結魔王(下)', null, @華語輕小說, 2011, @蓋亞,  'https://s.eslite.com/b2b/newItem/ebook_init/main1_127783.jpg', '9789866219689', 1),
    ('GOTH斷掌事件', null, @日本文學, 2002, @皇冠文化,  'https://www.mottainaihonpo.com/kaitori/contents/cat01/img/otsuichi-osusume-img/book_04.jpg', '4048733907', 1),
    ('ZOO (經典回歸版)', null, @日本文學, 2021, @皇冠文化,  'https://cdn.kobo.com/book-images/53afdd7a-1f60-44a9-b9b9-4d618f8aa770/1200/1200/False/zoo-141.jpg', '9789865580704', 1),
    ('池袋西口公園 1', @池袋西口公園, @日本文學, 2004, @木馬文化,  'https://taiwan-image.bookwalker.com.tw/product/117673/117673_1.jpg', '9867475232', 0),
    ('電子之星：池袋西口公園 4', @池袋西口公園, @日本文學, 2008, @木馬文化,  'https://taiwan-image.bookwalker.com.tw/product/117676/117676_1.jpg', '9789867475800', 0),
    ('G少年冬戰爭：池袋西口公園 7', @池袋西口公園, @日本文學, 2010, @木馬文化,  'https://taiwan-image.bookwalker.com.tw/product/117679/117679_1.jpg', '9789863592907', 1),
    ('哈利波特 (1) 神秘的魔法石', null, @歐美文學, 1997, @皇冠文化,  'https://upload.wikimedia.org/wikipedia/zh/3/3c/Hp1tw.jpg', '9789573317241', 1),
    ('哈利波特 (7) 死神的聖物', null, @歐美文學, 2007, @皇冠文化,  'https://upload.wikimedia.org/wikipedia/zh/6/6a/Hp7tw.jpeg', '9789573323570', 1),
    ('納尼亞傳奇 (1) 獅子．女巫．魔衣櫥', null, @歐美文學, 2005, @大田出版社,  'https://s.eslite.com/Upload/Product/200901/o/633683243286727500.jpg', '9789574558902', 1),
    ('納尼亞傳奇 (7) 最後一戰', null, @歐美文學, 2021, @大田出版社,  'https://upload.wikimedia.org/wikipedia/zh/5/56/%E6%9C%80%E5%BE%8C%E7%9A%84%E6%88%B0%E5%BD%B9.jpg', '9574559084', 1),
    ('VOGUE JAPAN 5月號/2025', null, @時尚, 2021, @VOGUE出版社,  'https://s2.eslite.com/unsafe/fit-in/x900/s.eslite.com/b2b/newItem/2025/03/25/2476_172500915_768_mainCoverImage1.jpg', '4910177270556', 1),
    ('VOGUE JAPAN 6月號/2025', null, @時尚, 2021, @VOGUE出版社,  'https://s2.eslite.com/unsafe/fit-in/x900/s.eslite.com/b2b/newItem/2025/04/25/2476_170951128_975_mainCoverImage1.jpg', '4910177270655', 1),
    ('PC home 電腦家庭 02月號/2023 第325期', null, @科技, 2023, @城邦出版集團,  'https://img.pchome.com.tw/cs/items/DJBNAJD900FZ45E/000001_1675357852.jpg', '3121561461325', 1),
    ('PC home 電腦家庭 06月號/2025 第353期', null, @科技, 2025, @城邦出版集團,  'https://img.pchome.com.tw/cs/items/DJBQ2ZD900IVNX1/000001_1748794115.jpg', '3121561461353', 1),
    ('Java SE 8 技術手冊', null, @程式設計, 2014, @碁峰,  'https://s2.eslite.com/unsafe/fit-in/x900/s.eslite.com/upload/product/o/2680883616000/361120.jpg', '9789863471714', 1),
    ('JavaScript 技術手冊', null, @程式設計, 2019, @碁峰,  'https://s2.eslite.com/unsafe/fit-in/x900/s.eslite.com/Upload/Product/201911/o/637092307890993750.jpg', '9789865023188', 1),
    ('Java SE 17 技術手冊', null, @程式設計, 2022, @碁峰,  'https://s2.eslite.com/unsafe/fit-in/x900/s.eslite.com/upload/product/o/2682168405002/20220505032447675584.jpg', '9786263241435', 1),
    ('出發！日本自助旅行', null, @生活旅遊, 2023, @墨刻,  'https://taiwan-image.bookwalker.com.tw/product/182909/zoom_big_182909.jpg', '9789862899588', 1),
    ('京都・大阪・神戶攻略完全制霸2025', null, @生活旅遊, 2024, @墨刻,  'https://taiwan-image.bookwalker.com.tw/product/231036/231036_1.jpg', '9786263980754', 1);

-- 更新上述2個資料的representative欄位
-- UPDATE `book` SET representative = 0 WHERE title IN ('池袋西口公園 1', '電子之星：池袋西口公園 4');

SET @`鋼之鍊金術師 (1)` = (SELECT id FROM `book` WHERE title = '鋼之鍊金術師 (1)');
SET @`鋼之鍊金術師 (24)` = (SELECT id FROM `book` WHERE title = '鋼之鍊金術師 (24)');
SET @`鋼之鍊金術師 (25)` = (SELECT id FROM `book` WHERE title = '鋼之鍊金術師 (25)');
SET @`鋼之鍊金術師 (26)` = (SELECT id FROM `book` WHERE title = '鋼之鍊金術師 (26)');
SET @`鋼之鍊金術師 (27)` = (SELECT id FROM `book` WHERE title = '鋼之鍊金術師 (27)');
SET @`銀之匙 (1)` = (SELECT id FROM `book` WHERE title = '銀之匙 (1)');
SET @`銀之匙 (15)` = (SELECT id FROM `book` WHERE title = '銀之匙 (15)');
SET @`銀魂 (1)` = (SELECT id FROM `book` WHERE title = '銀魂 (1)');
SET @`銀魂 (77)` = (SELECT id FROM `book` WHERE title = '銀魂 (77)');
SET @`ONE-PUNCH MAN 一拳超人 (1)` = (SELECT id FROM `book` WHERE title = 'ONE-PUNCH MAN 一拳超人 (1)');
SET @`ONE-PUNCH MAN 一拳超人 (32)` = (SELECT id FROM `book` WHERE title = 'ONE-PUNCH MAN 一拳超人 (32)');
SET @`路人超能100 (1)` = (SELECT id FROM `book` WHERE title = '路人超能100 (1)');
SET @`路人超能100 (16)` = (SELECT id FROM `book` WHERE title = '路人超能100 (16)');
SET @`鄰座的怪同學 (1)` = (SELECT id FROM `book` WHERE title = '鄰座的怪同學 (1)');
SET @`鄰座的怪同學 (13)` = (SELECT id FROM `book` WHERE title = '鄰座的怪同學 (13)');
SET @`NANA (1)` = (SELECT id FROM `book` WHERE title = 'NANA (1)');
SET @`NANA (21)` = (SELECT id FROM `book` WHERE title = 'NANA (21)');
SET @`空之境界 (上)` = (SELECT id FROM `book` WHERE title = '空之境界 (上)');
SET @`空之境界 (下)` = (SELECT id FROM `book` WHERE title = '空之境界 (下)');
SET @`殼中少女01：壓縮` = (SELECT id FROM `book` WHERE title = '殼中少女01：壓縮');
SET @`殼中少女03：排氣` = (SELECT id FROM `book` WHERE title = '殼中少女03：排氣');
SET @`特殊傳說 新版vol.1 不存在的學園！` = (SELECT id FROM `book` WHERE title = '特殊傳說 新版vol.1 不存在的學園！');
SET @`特殊傳說 新版vol.10 那之後...` = (SELECT id FROM `book` WHERE title = '特殊傳說 新版vol.10 那之後...');
SET @`吾命騎士 vol.1 騎士基礎理論` = (SELECT id FROM `book` WHERE title = '吾命騎士 vol.1 騎士基礎理論');
SET @`吾命騎士 vol.8 終結魔王(下)` = (SELECT id FROM `book` WHERE title = '吾命騎士 vol.8 終結魔王(下)');
SET @`GOTH斷掌事件` = (SELECT id FROM `book` WHERE title = 'GOTH斷掌事件');
SET @`ZOO (經典回歸版)` = (SELECT id FROM `book` WHERE title = 'ZOO (經典回歸版)');
SET @`池袋西口公園 1` = (SELECT id FROM `book` WHERE title = '池袋西口公園 1');
SET @`電子之星：池袋西口公園 4` = (SELECT id FROM `book` WHERE title = '電子之星：池袋西口公園 4');
SET @`G少年冬戰爭：池袋西口公園 7` = (SELECT id FROM `book` WHERE title = 'G少年冬戰爭：池袋西口公園 7');
SET @`哈利波特 (1) 神秘的魔法石` = (SELECT id FROM `book` WHERE title = '哈利波特 (1) 神秘的魔法石');
SET @`哈利波特 (7) 死神的聖物` = (SELECT id FROM `book` WHERE title = '哈利波特 (7) 死神的聖物');
SET @`納尼亞傳奇 (1) 獅子．女巫．魔衣櫥` = (SELECT id FROM `book` WHERE title = '納尼亞傳奇 (1) 獅子．女巫．魔衣櫥');
SET @`納尼亞傳奇 (7) 最後一戰` = (SELECT id FROM `book` WHERE title = '納尼亞傳奇 (7) 最後一戰');
SET @`VOGUE JAPAN 5月號/2025` = (SELECT id FROM `book` WHERE title = 'VOGUE JAPAN 5月號/2025');
SET @`VOGUE JAPAN 6月號/2025` = (SELECT id FROM `book` WHERE title = 'VOGUE JAPAN 6月號/2025');
SET @`PC home 電腦家庭 02月號/2023 第325期` = (SELECT id FROM `book` WHERE title = 'PC home 電腦家庭 02月號/2023 第325期');
SET @`PC home 電腦家庭 06月號/2025 第353期` = (SELECT id FROM `book` WHERE title = 'PC home 電腦家庭 06月號/2025 第353期');
SET @`Java SE 8 技術手冊` = (SELECT id FROM `book` WHERE title = 'Java SE 8 技術手冊');
SET @`JavaScript 技術手冊` = (SELECT id FROM `book` WHERE title = 'JavaScript 技術手冊');
SET @`Java SE 17 技術手冊` = (SELECT id FROM `book` WHERE title = 'Java SE 17 技術手冊');
SET @`出發！日本自助旅行` = (SELECT id FROM `book` WHERE title = '出發！日本自助旅行');
SET @`京都・大阪・神戶攻略完全制霸2025` = (SELECT id FROM `book` WHERE title = '京都・大阪・神戶攻略完全制霸2025');


-- 9. 插入 book_author 資料 (中間表)
INSERT INTO `book_author` (`book_id`, `author_id`) VALUES
                                                       (@`鋼之鍊金術師 (1)`, @荒川弘),
                                                       (@`鋼之鍊金術師 (24)`, @荒川弘),
                                                       (@`鋼之鍊金術師 (25)`, @荒川弘),
                                                       (@`鋼之鍊金術師 (26)`, @荒川弘),
                                                       (@`鋼之鍊金術師 (27)`, @荒川弘),
                                                       (@`銀之匙 (1)`, @荒川弘),
                                                       (@`銀之匙 (15)`, @荒川弘),
                                                       (@`銀魂 (1)`, @空知英秋),
                                                       (@`銀魂 (77)`, @空知英秋),
                                                       (@`ONE-PUNCH MAN 一拳超人 (1)`, @村田雄介),
                                                       (@`ONE-PUNCH MAN 一拳超人 (1)`, @ONE),
                                                       (@`ONE-PUNCH MAN 一拳超人 (32)`, @村田雄介),
                                                       (@`ONE-PUNCH MAN 一拳超人 (32)`, @ONE),
                                                       (@`路人超能100 (1)`, @ONE),
                                                       (@`路人超能100 (16)`, @ONE),
                                                       (@`鄰座的怪同學 (1)`, @Robico),
                                                       (@`鄰座的怪同學 (13)`, @Robico),
                                                       (@`NANA (1)`, @矢澤愛),
                                                       (@`NANA (21)`, @矢澤愛),
                                                       (@`空之境界 (上)`, @奈須きのこ),
                                                       (@`空之境界 (下)`, @奈須きのこ),
                                                       (@殼中少女01：壓縮, @沖方丁),
                                                       (@殼中少女03：排氣, @沖方丁),
                                                       (@`特殊傳說 新版vol.1 不存在的學園！`, @護玄),
                                                       (@`特殊傳說 新版vol.10 那之後...`, @護玄),
                                                       (@`吾命騎士 vol.1 騎士基礎理論`, @御我),
                                                       (@`吾命騎士 vol.8 終結魔王(下)`, @御我),
                                                       (@`GOTH斷掌事件`, @乙一),
                                                       (@`ZOO (經典回歸版)`, @乙一),
                                                       (@`池袋西口公園 1`, @石田衣良),
                                                       (@`電子之星：池袋西口公園 4`, @石田衣良),
                                                       (@`G少年冬戰爭：池袋西口公園 7`, @石田衣良),
                                                       (@`哈利波特 (1) 神秘的魔法石`, @羅琳),
                                                       (@`哈利波特 (7) 死神的聖物`, @羅琳),
                                                       (@`納尼亞傳奇 (1) 獅子．女巫．魔衣櫥`, @路易斯),
                                                       (@`納尼亞傳奇 (7) 最後一戰`, @路易斯),
                                                       (@`VOGUE JAPAN 5月號/2025`, @vogue編輯部),
                                                       (@`VOGUE JAPAN 6月號/2025`, @vogue編輯部),
                                                       (@`PC home 電腦家庭 02月號/2023 第325期`, @PChome雜誌編輯群),
                                                       (@`PC home 電腦家庭 06月號/2025 第353期`, @PChome雜誌編輯群),
                                                       (@`Java SE 8 技術手冊`, @林信良),
                                                       (@`JavaScript 技術手冊`, @林信良),
                                                       (@`Java SE 17 技術手冊`, @林信良),
                                                       (@出發！日本自助旅行, @墨刻編輯部),
                                                       (@京都・大阪・神戶攻略完全制霸2025, @墨刻編輯部);


-- 10. 插入 book_tag 資料 (中間表)
INSERT INTO `book_tag` (`book_id`, `tag_id`) VALUES
                                                 (@`鋼之鍊金術師 (1)`, @標籤001),
                                                 (@`銀之匙 (1)`, @標籤001),
                                                 (@`銀魂 (1)`, @標籤001),
                                                 (@`ONE-PUNCH MAN 一拳超人 (1)`, @標籤001),
                                                 (@`路人超能100 (1)`, @標籤001),
                                                 (@`鄰座的怪同學 (1)`, @標籤001),
                                                 (@`NANA (1)`, @標籤001),
                                                 (@`空之境界 (上)`, @標籤001),
                                                 (@殼中少女01：壓縮, @標籤001),
                                                 (@`特殊傳說 新版vol.1 不存在的學園！`, @標籤001),
                                                 (@`吾命騎士 vol.1 騎士基礎理論`, @標籤001),
                                                 (@`GOTH斷掌事件`, @推理),
                                                 (@`ZOO (經典回歸版)`, @推理),
                                                 (@`池袋西口公園 1`, @推理),
                                                 (@`電子之星：池袋西口公園 4`, @推理),
                                                 (@`G少年冬戰爭：池袋西口公園 7`, @推理),
                                                 (@`哈利波特 (1) 神秘的魔法石`, @奇幻),
                                                 (@`哈利波特 (7) 死神的聖物`, @奇幻),
                                                 (@`納尼亞傳奇 (1) 獅子．女巫．魔衣櫥`, @奇幻),
                                                 (@`納尼亞傳奇 (7) 最後一戰`, @奇幻),
                                                 (@`VOGUE JAPAN 5月號/2025`, @標籤002),
                                                 (@`PC home 電腦家庭 02月號/2023 第325期`, @標籤002),
                                                 (@`Java SE 8 技術手冊`, @標籤002),
                                                 (@出發！日本自助旅行, @標籤002),
                                                 (@`鋼之鍊金術師 (26)`, @標籤003);
INSERT INTO `book_tag` (`book_id`, `tag_id`) VALUES
                                                 (@`鋼之鍊金術師 (1)`, @熱血),
                                                 (@`鋼之鍊金術師 (24)`, @熱血),
                                                 (@`鋼之鍊金術師 (25)`, @熱血),
                                                 (@`鋼之鍊金術師 (26)`, @熱血),
                                                 (@`鋼之鍊金術師 (27)`, @熱血),
                                                 (@`銀之匙 (1)`, @青春),
                                                 (@`銀之匙 (15)`, @青春),
                                                 (@`銀魂 (1)`, @搞笑),
                                                 (@`銀魂 (77)`, @搞笑);
INSERT INTO `book_tag` (`book_id`, `tag_id`) VALUES
                                                 (@`鋼之鍊金術師 (1)`, @動畫化),
                                                 (@`鋼之鍊金術師 (24)`, @動畫化),
                                                 (@`鋼之鍊金術師 (25)`, @動畫化),
                                                 (@`鋼之鍊金術師 (26)`, @動畫化),
                                                 (@`鋼之鍊金術師 (27)`, @動畫化),
                                                 (@`銀之匙 (1)`, @動畫化),
                                                 (@`銀之匙 (15)`, @動畫化),
                                                 (@`銀魂 (1)`, @動畫化),
                                                 (@`銀魂 (77)`, @動畫化),
                                                 (@`空之境界 (上)`, @動畫化),
                                                 (@`空之境界 (下)`, @動畫化),
                                                 (@殼中少女01：壓縮, @動畫化),
                                                 (@殼中少女03：排氣, @動畫化),
                                                 (@`池袋西口公園 1`, @動畫化),
                                                 (@`電子之星：池袋西口公園 4`, @動畫化),
                                                 (@`G少年冬戰爭：池袋西口公園 7`, @動畫化);

-- 11. 插入 book_copy 資料
INSERT INTO `book_copy` (`book_id`, `unique_code`, `status`) VALUES
                                                                 (@`鋼之鍊金術師 (1)`, 'HR001A', 'A'),
                                                                 (@`鋼之鍊金術師 (1)`, 'HR001B', 'A'),
                                                                 (@`鋼之鍊金術師 (24)`, 'HR024A', 'A'),
                                                                 (@`鋼之鍊金術師 (24)`, 'HR024B', 'A'),
                                                                 (@`鋼之鍊金術師 (25)`, 'HR025A', 'A'),
                                                                 (@`鋼之鍊金術師 (25)`, 'HR025b', 'A'),
                                                                 (@`鋼之鍊金術師 (26)`, 'HR026A', 'A'),
                                                                 (@`鋼之鍊金術師 (26)`, 'HR026B', 'A'),
                                                                 (@`鋼之鍊金術師 (27)`, 'HR027A', 'A'),
                                                                 (@`鋼之鍊金術師 (27)`, 'HR027B', 'A'),
                                                                 (@`銀之匙 (1)`, 'SP001A', 'A'),
                                                                 (@`銀之匙 (1)`, 'SP001B', 'A'),
                                                                 (@`銀之匙 (15)`, 'SP015A', 'A'),
                                                                 (@`銀之匙 (15)`, 'SP015B', 'A'),
                                                                 (@`銀魂 (1)`, 'GT001A', 'A'),
                                                                 (@`銀魂 (1)`, 'GT001B', 'A'),
                                                                 (@`銀魂 (77)`, 'GT077A', 'A'),
                                                                 (@`銀魂 (77)`, 'GT077B', 'A'),
                                                                 (@`ONE-PUNCH MAN 一拳超人 (1)`, 'OP001A', 'A'),
                                                                 (@`ONE-PUNCH MAN 一拳超人 (1)`, 'OP001B', 'A'),
                                                                 (@`ONE-PUNCH MAN 一拳超人 (32)`, 'OP032A', 'A'),
                                                                 (@`ONE-PUNCH MAN 一拳超人 (32)`, 'OP032B', 'A'),
                                                                 (@`路人超能100 (1)`, 'MOB001A', 'A'),
                                                                 (@`路人超能100 (1)`, 'MOB001B', 'A'),
                                                                 (@`路人超能100 (16)`, 'MOB016A', 'A'),
                                                                 (@`路人超能100 (16)`, 'MOB016B', 'A'),
                                                                 (@`鄰座的怪同學 (1)`, 'TH001A', 'A'),
                                                                 (@`鄰座的怪同學 (1)`, 'TH001B', 'A'),
                                                                 (@`鄰座的怪同學 (13)`, 'TH013A', 'A'),
                                                                 (@`鄰座的怪同學 (13)`, 'TH013B', 'A'),
                                                                 (@`NANA (1)`, 'NANA001A', 'A'),
                                                                 (@`NANA (1)`, 'NANA001B', 'A'),
                                                                 (@`NANA (21)`, 'NANA021A', 'A'),
                                                                 (@`NANA (21)`, 'NANA021B', 'A'),
                                                                 (@`空之境界 (上)`, 'KK001A', 'A'),
                                                                 (@`空之境界 (上)`, 'KK001B', 'A'),
                                                                 (@`空之境界 (上)`, 'KK001C', 'A'),
                                                                 (@`空之境界 (下)`, 'KK002A', 'A'),
                                                                 (@`空之境界 (下)`, 'KK002B', 'A'),
                                                                 (@`空之境界 (下)`, 'KK002C', 'A'),
                                                                 (@殼中少女01：壓縮, 'KS001A', 'A'),
                                                                 (@殼中少女03：排氣, 'KS003A', 'A'),
                                                                 (@`特殊傳說 新版vol.1 不存在的學園！`, 'TS001A', 'A'),
                                                                 (@`特殊傳說 新版vol.10 那之後...`, 'TS010A', 'A'),
                                                                 (@`吾命騎士 vol.1 騎士基礎理論`, 'WN001A', 'A'),
                                                                 (@`吾命騎士 vol.8 終結魔王(下)`, 'WN008A', 'A'),
                                                                 (@`GOTH斷掌事件`, 'GOTH001A', 'A'),
                                                                 (@`ZOO (經典回歸版)`, 'ZO001A', 'A'),
                                                                 (@`池袋西口公園 1`, 'IK001A', 'A'),
                                                                 (@`電子之星：池袋西口公園 4`, 'IK004A', 'A'),
                                                                 (@`G少年冬戰爭：池袋西口公園 7`, 'IK007A', 'A'),
                                                                 (@`哈利波特 (1) 神秘的魔法石`, 'HP001A', 'A'),
                                                                 (@`哈利波特 (7) 死神的聖物`, 'HP007A', 'A'),
                                                                 (@`納尼亞傳奇 (1) 獅子．女巫．魔衣櫥`, 'NA001A', 'A'),
                                                                 (@`納尼亞傳奇 (7) 最後一戰`, 'NA007A', 'A'),
                                                                 (@`VOGUE JAPAN 5月號/2025`, 'VJ202505A', 'A'),
                                                                 (@`VOGUE JAPAN 6月號/2025`, 'VJ202506A', 'A'),
                                                                 (@`PC home 電腦家庭 02月號/2023 第325期`, 'PCH325A', 'A'),
                                                                 (@`PC home 電腦家庭 06月號/2025 第353期`, 'PCH353A', 'A'),
                                                                 (@`Java SE 8 技術手冊`, 'JV008A', 'A'),
                                                                 (@`Java SE 8 技術手冊`, 'JV008B', 'A'),
                                                                 (@`Java SE 8 技術手冊`, 'JV008C', 'A'),
                                                                 (@`JavaScript 技術手冊`, 'JS001A', 'A'),
                                                                 (@`JavaScript 技術手冊`, 'JS001B', 'A'),
                                                                 (@`JavaScript 技術手冊`, 'JS001C', 'A'),
                                                                 (@`Java SE 17 技術手冊`, 'JV017A', 'A'),
                                                                 (@`Java SE 17 技術手冊`, 'JV017B', 'A'),
                                                                 (@`Java SE 17 技術手冊`, 'JV017C', 'A'),
                                                                 (@出發！日本自助旅行, 'JP001A', 'A'),
                                                                 (@京都・大阪・神戶攻略完全制霸2025, 'JP002A', 'A');
SELECT * FROM `book_copy` WHERE status = 'L';

-- 顯示每本ㄖbook_copy 每一筆資料，join book 以顯示書名
SELECT bc.id, b.title, bc.unique_code, bc.status
FROM `book_copy` bc
JOIN `book` b ON bc.book_id = b.id;
SELECT * FROM book;

-- 借閱用設定
SET @鄰座1A = (SELECT id FROM `book_copy` WHERE unique_code = 'TH001A');
SET @銀魂1A = (SELECT id FROM `book_copy` WHERE unique_code = 'GT001A');
SET @銀魂77A = (SELECT id FROM `book_copy` WHERE unique_code = 'GT077A');
SET @JAVA8A = (SELECT id FROM `book_copy` WHERE unique_code = 'JV008A');
SET @JSA = (SELECT id FROM `book_copy` WHERE unique_code = 'JS001A');
SET @JAVA17A = (SELECT id FROM `book_copy` WHERE unique_code = 'JV017A');

SET @一拳32A = (SELECT id FROM `book_copy` WHERE unique_code = 'OP032A');
SET @鋼鍊1A = (SELECT id FROM `book_copy` WHERE unique_code = 'HR001A');
SET @鋼鍊1B = (SELECT id FROM `book_copy` WHERE unique_code = 'HR001B');
-- 預約用設定
SET @空境上A = (SELECT id FROM `book_copy` WHERE unique_code = 'KK001A');
SET @空境下A = (SELECT id FROM `book_copy` WHERE unique_code = 'KK002A');
SET @鋼鍊27A = (SELECT id FROM `book_copy` WHERE unique_code = 'HR027A');

