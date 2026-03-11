CREATE DATABASE IF NOT EXISTS `book_lending_platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `book_lending_platform`;

CREATE TABLE IF NOT EXISTS `user` (
  `name_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（加密后存储）',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `identity_card` VARCHAR(20) NOT NULL COMMENT '身份证号',
  `major` VARCHAR(50) DEFAULT NULL COMMENT '专业',
  `user_type` TINYINT NOT NULL COMMENT '用户类型：1用户，2图书管理员，3系统管理员',
  `max_borrow_count` INT DEFAULT 5 COMMENT '最大借阅数量',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`name_id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email` (`email`),
  UNIQUE KEY `uk_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 初始化默认用户账号（默认密码均为 123456）
INSERT INTO `user` (
  `username`,
  `password`,
  `real_name`,
  `email`,
  `phone`,
  `identity_card`,
  `major`,
  `user_type`,
  `max_borrow_count`,
  `status`
) VALUES
  (
    'reader',
    '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
    '张三',
    'reader@library.local',
    '13800000001',
    '110101199001010011',
    '计算机科学',
    1,
    5,
    1
  ),
  (
    'librarian',
    '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
    '李四',
    'librarian@library.local',
    '13800000002',
    '110101199001010022',
    '图书情报',
    2,
    10,
    1
  ),
  (
    'admin',
    '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
    '王五',
    'admin@library.local',
    '13800000003',
    '110101199001010033',
    '信息管理',
    3,
    10,
    1
  );

CREATE TABLE IF NOT EXISTS `book` (
  `book_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图书ID',
  `isbn` VARCHAR(20) NOT NULL COMMENT 'ISBN号',
  `book_name` VARCHAR(200) NOT NULL COMMENT '书名',
  `author` VARCHAR(100) NOT NULL COMMENT '作者',
  `publisher` VARCHAR(100) NOT NULL COMMENT '出版社',
  `publish_date` DATE DEFAULT NULL COMMENT '出版日期',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `sub_field` VARCHAR(50) DEFAULT NULL COMMENT '计算机子领域',
  `difficulty_level` TINYINT NOT NULL COMMENT '难度等级：1入门，2进阶，3专家',
  `suitable_scene` VARCHAR(100) DEFAULT NULL COMMENT '适用场景',
  `cover_url` VARCHAR(200) DEFAULT NULL COMMENT '封面图片URL',
  `description` TEXT COMMENT '图书简介',
  `catalog` TEXT COMMENT '目录',
  `author_intro` TEXT COMMENT '作者简介',
  `target_audience` VARCHAR(200) DEFAULT NULL COMMENT '适用人群',
  `total_count` INT DEFAULT 0 COMMENT '馆藏总数',
  `available_count` INT DEFAULT 0 COMMENT '可借数量',
  `borrow_count` INT DEFAULT 0 COMMENT '借阅次数',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0下架，1上架',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`book_id`),
  UNIQUE KEY `uk_book_isbn` (`isbn`),
  KEY `idx_book_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书表';

-- 初始化图书数据（仅用于空库初始化，重复执行可能因 ISBN 唯一键报错）
INSERT INTO `book` (
  `isbn`,
  `book_name`,
  `author`,
  `publisher`,
  `publish_date`,
  `category_id`,
  `sub_field`,
  `difficulty_level`,
  `suitable_scene`,
  `cover_url`,
  `description`,
  `catalog`,
  `author_intro`,
  `target_audience`,
  `total_count`,
  `available_count`,
  `borrow_count`,
  `status`
) VALUES
  (
    '9787111128069',
    '深入理解计算机系统（第3版）',
    'Randal E. Bryant / David R. O’Hallaron',
    '机械工业出版社',
    '2016-04-01',
    101,
    '系统',
    3,
    '课程学习, 面试提升',
    'https://images.unsplash.com/photo-1508169351866-777fc0047ac5?auto=format&fit=crop&w=480&q=80',
    '经典系统教材，覆盖程序表示、处理器体系结构、存储层次与并发。',
    '第1章 计算机系统漫游\n第2章 信息的表示和处理\n第3章 程序的机器级表示\n第4章 处理器体系结构\n第5章 优化程序性能\n第6章 存储器层次结构\n第7章 链接\n第8章 异常控制流\n第9章 虚拟内存\n第10章 系统级I/O\n第11章 网络编程\n第12章 并发编程',
    'Randal E. Bryant 与 David R. O’Hallaron 均为卡内基梅隆大学教授，长期从事系统与并行计算教学与研究。',
    '计算机相关专业学生, 系统开发者',
    12,
    9,
    628,
    1
  ),
  (
    '9787111213826',
    '代码大全（第2版）',
    'Steve McConnell',
    '电子工业出版社',
    '2006-06-01',
    102,
    '软件工程',
    2,
    '工程实践, 代码质量',
    'https://images.unsplash.com/photo-1503543791519-1694c6b20779?auto=format&fit=crop&w=480&q=80',
    '全面的软件构建指南，涵盖编码规范、设计与调试实践。',
    '第1部 软件构建基础\n第2部 设计\n第3部 变量\n第4部 语句\n第5部 代码改善\n第6部 系统考虑\n第7部 软件工艺\n附录',
    'Steve McConnell 是软件工程领域作者与讲师，专注于软件构建与工程效率提升。',
    '有一定编码经验的开发者',
    8,
    3,
    412,
    1
  ),
  (
    '9787115474063',
    '图解 HTTP',
    '上野宣',
    '人民邮电出版社',
    '2014-05-01',
    103,
    '网络',
    1,
    '入门学习',
    'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=480&q=80',
    '用插图解释 HTTP/HTTPS、Cookies、缓存与安全机制。',
    '第1章 了解 Web 及网络基础\n第2章 简单的 HTTP 协议\n第3章 HTTP 报文内的 HTTP 信息\n第4章 返回结果的 HTTP 状态码\n第5章 与 HTTP 协作的 Web 服务器\n第6章 HTTP 首部\n第7章 确保 Web 安全的 HTTPS\n第8章 确认访问用户身份的认证',
    '上野宣为日本技术作者，长期撰写网络与 Web 开发相关书籍。',
    '网络与 Web 开发入门者',
    15,
    12,
    355,
    1
  ),
  (
    '9787111629245',
    'Java 核心技术 卷 I（第11版）',
    'Cay S. Horstmann',
    '机械工业出版社',
    '2020-01-01',
    104,
    'Java',
    2,
    '课程学习, 日常开发',
    'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?auto=format&fit=crop&w=480&q=80',
    '面向 Java SE 的核心概念与 API 实战。',
    '第1章 Java 程序设计概述\n第2章 Java 程序设计环境\n第3章 Java 基础程序设计结构\n第4章 对象与类\n第5章 继承\n第6章 接口、lambda 与内部类\n第7章 异常、断言与日志\n第8章 泛型\n第9章 集合\n第10章 图形程序设计\n第11章 事件处理\n第12章 Swing',
    'Cay S. Horstmann 长期从事 Java 教学与工程实践，著有多部经典 Java 书籍。',
    'Java 开发者, 计算机相关专业学生',
    10,
    6,
    271,
    1
  ),
  (
    '9787115428028',
    'Spring 实战（第5版）',
    'Craig Walls',
    '人民邮电出版社',
    '2019-03-01',
    104,
    'Java',
    2,
    'Web 开发, 后端实战',
    'https://images.unsplash.com/photo-1527135472885-9908bbe7d61f?auto=format&fit=crop&w=480&q=80',
    '以项目驱动讲解 Spring 生态核心能力。',
    '第1章 Spring 入门\n第2章 开发 Web 应用\n第3章 数据持久化\n第4章 安全\n第5章 运行与部署\n第6章 REST API\n第7章 消息与集成',
    'Craig Walls 是 Spring 社区资深作者与讲师，长期关注 Spring 生态实践。',
    '后端开发者',
    9,
    5,
    189,
    1
  ),
  (
    '9787111636649',
    'MySQL 必知必会',
    'Ben Forta',
    '人民邮电出版社',
    '2019-08-01',
    105,
    '数据库',
    1,
    'SQL 入门',
    'https://images.unsplash.com/photo-1601221018729-15aec0cf430f?auto=format&fit=crop&w=480&q=80',
    '从 SELECT 到 JOIN 与子查询，快速掌握 SQL 基础。',
    '第1章 了解 SQL\n第2章 检索数据\n第3章 排序与过滤\n第4章 数据汇总\n第5章 分组\n第6章 子查询\n第7章 联结\n第8章 组合查询\n第9章 插入/更新/删除\n第10章 视图\n第11章 存储过程\n第12章 事务',
    'Ben Forta 是技术顾问与讲师，擅长将数据库与 Web 技术以易懂方式讲解。',
    '数据库入门者, 后端开发者',
    18,
    14,
    306,
    1
  ),
  (
    '9787111558422',
    '高性能 MySQL（第3版）',
    'Baron Schwartz / Peter Zaitsev / Vadim Tkachenko',
    '电子工业出版社',
    '2017-09-01',
    105,
    '数据库',
    3,
    '性能优化, 生产排障',
    'https://images.unsplash.com/photo-1651261525945-5382b021bfeb?auto=format&fit=crop&w=480&q=80',
    '深入 MySQL 内部与性能优化方法。',
    '第1章 MySQL 架构与历史\n第2章 MySQL 基准测试\n第3章 服务器性能剖析\n第4章 Schema 与数据类型优化\n第5章 索引\n第6章 查询性能优化\n第7章 高级 MySQL 特性\n第8章 复制\n第9章 备份与恢复\n第10章 扩展 MySQL',
    'Baron Schwartz、Peter Zaitsev、Vadim Tkachenko 等为 MySQL 性能与运维领域资深专家。',
    '中高级后端开发者, DBA',
    6,
    2,
    147,
    1
  ),
  (
    '9787111126942',
    '算法（第4版）',
    'Robert Sedgewick / Kevin Wayne',
    '人民邮电出版社',
    '2012-10-01',
    106,
    '算法',
    2,
    '课程学习, 面试准备',
    'https://images.unsplash.com/photo-1689023542260-faa90df36bb4?auto=format&fit=crop&w=480&q=80',
    '经典算法与数据结构教材，配套大量示例与练习。',
    '第1章 基础\n第2章 排序\n第3章 查找\n第4章 图\n第5章 字符串\n第6章 上下文\n附录',
    'Robert Sedgewick 与 Kevin Wayne 来自普林斯顿大学，共同开发算法课程并编写本书。',
    '计算机相关专业学生, 面试准备者',
    11,
    8,
    498,
    1
  ),
  (
    '9787111652038',
    '设计数据密集型应用',
    'Martin Kleppmann',
    '中国电力出版社',
    '2018-11-01',
    107,
    '分布式',
    3,
    '架构设计, 系统演进',
    'https://images.unsplash.com/photo-1732304720450-0fe31c39f491?auto=format&fit=crop&w=480&q=80',
    '从存储、复制、分区到流处理，系统性讲解数据系统设计。',
    '第1章 可靠性、可伸缩性与可维护性\n第2章 数据模型与查询语言\n第3章 存储与检索\n第4章 编码与演化\n第5章 复制\n第6章 分区\n第7章 事务\n第8章 分布式系统的麻烦\n第9章 一致性与共识\n第10章 批处理\n第11章 流处理\n第12章 数据系统的未来',
    'Martin Kleppmann 是分布式系统与数据基础设施研究者，擅长用工程视角解读系统设计。',
    '架构师, 中高级后端开发者',
    7,
    4,
    233,
    1
  ),
  (
    '9787111605904',
    'Python 机器学习基础教程',
    'Sebastian Raschka / Vahid Mirjalili',
    '人民邮电出版社',
    '2019-01-01',
    108,
    '机器学习',
    2,
    '课程学习, 项目实践',
    'https://images.unsplash.com/photo-1570047435693-4a1d6bbc6220?auto=format&fit=crop&w=480&q=80',
    '结合 scikit-learn 的端到端机器学习入门与实践。',
    '第1章 机器学习概览\n第2章 训练简单的机器学习算法\n第3章 使用 scikit-learn\n第4章 特征工程\n第5章 模型评估与调参\n第6章 集成学习\n第7章 文本与情感分析\n第8章 深度学习入门',
    'Sebastian Raschka 与 Vahid Mirjalili 长期从事机器学习相关研究与教学，并编写多本畅销教程。',
    '数据分析入门者, 开发者',
    9,
    7,
    198,
    1
  );

CREATE TABLE IF NOT EXISTS `book_category` (
  `category_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`),
  KEY `idx_book_category_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- 初始化图书分类数据（仅用于空库初始化，重复执行会因主键冲突报错）
INSERT INTO `book_category` (
  `category_id`,
  `category_name`,
  `parent_id`,
  `sort_order`,
  `status`
) VALUES
  (101, '系统与架构', 0, 10, 1),
  (102, '软件工程', 0, 20, 1),
  (103, '网络与 Web', 0, 30, 1),
  (104, 'Java 开发', 0, 40, 1),
  (105, '数据库', 0, 50, 1),
  (106, '算法与数据结构', 0, 60, 1),
  (107, '分布式系统', 0, 70, 1),
  (108, '机器学习', 0, 80, 1);

CREATE TABLE IF NOT EXISTS `borrow_record` (
  `borrow_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `borrow_date` DATETIME NOT NULL COMMENT '借阅日期',
  `due_date` DATETIME NOT NULL COMMENT '应还日期',
  `return_date` DATETIME DEFAULT NULL COMMENT '实际归还日期',
  `renew_count` INT DEFAULT 0 COMMENT '续借次数',
  `status` TINYINT NOT NULL COMMENT '状态：1借阅中，2已归还，3超期',
  `overdue_days` INT DEFAULT 0 COMMENT '超期天数',
  `fine_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '罚款金额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`borrow_id`),
  KEY `idx_borrow_record_user_id` (`user_id`),
  KEY `idx_borrow_record_book_id` (`book_id`),
  CONSTRAINT `fk_borrow_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`name_id`),
  CONSTRAINT `fk_borrow_record_book_id` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

CREATE TABLE IF NOT EXISTS `collection_category` (
  `collection_category_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏分类ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认分类：0否，1是',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`collection_category_id`),
  UNIQUE KEY `uk_collection_category_user_name` (`user_id`, `category_name`),
  UNIQUE KEY `uk_collection_category_id_user_id` (`collection_category_id`, `user_id`),
  CONSTRAINT `fk_collection_category_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`name_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏分类表';

CREATE TABLE IF NOT EXISTS `collection_record` (
  `collection_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `collection_category_id` BIGINT DEFAULT NULL COMMENT '收藏分类ID',
  `collection_date` DATETIME NOT NULL COMMENT '收藏日期',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`collection_id`),
  UNIQUE KEY `uk_collection_record_user_book` (`user_id`, `book_id`),
  KEY `idx_collection_record_book_id` (`book_id`),
  KEY `idx_collection_record_category_user_id` (`collection_category_id`, `user_id`),
  CONSTRAINT `fk_collection_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`name_id`),
  CONSTRAINT `fk_collection_record_book_id` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`),
  CONSTRAINT `fk_collection_record_category_user_id` FOREIGN KEY (`collection_category_id`, `user_id`) REFERENCES `collection_category` (`collection_category_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏记录表';

CREATE TABLE IF NOT EXISTS `user_preference` (
  `preference_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `prefer_fields` VARCHAR(200) DEFAULT NULL COMMENT '偏好领域，多个用逗号分隔',
  `prefer_difficulty` TINYINT DEFAULT NULL COMMENT '偏好难度',
  `prefer_scenes` VARCHAR(200) DEFAULT NULL COMMENT '偏好场景',
  `recommend_new_book` TINYINT DEFAULT 0 COMMENT '优先推荐新书：0否，1是',
  `recommend_hot_book` TINYINT DEFAULT 0 COMMENT '优先推荐热门书：0否，1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`preference_id`),
  UNIQUE KEY `uk_user_preference_user_id` (`user_id`),
  CONSTRAINT `fk_user_preference_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`name_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好表';

CREATE TABLE IF NOT EXISTS `book_location` (
  `location_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '位置ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `floor` INT NOT NULL COMMENT '楼层',
  `area` VARCHAR(20) NOT NULL COMMENT '区域',
  `shelf_no` VARCHAR(20) NOT NULL COMMENT '书架号',
  `layer` INT NOT NULL COMMENT '层数',
  `rfid_code` VARCHAR(50) DEFAULT NULL COMMENT 'RFID标签码',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`location_id`),
  UNIQUE KEY `uk_book_location_rfid_code` (`rfid_code`),
  KEY `idx_book_location_book_id` (`book_id`),
  CONSTRAINT `fk_book_location_book_id` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书位置表';

-- 初始化图书位置信息（仅用于空库初始化）
INSERT INTO `book_location` (`book_id`, `floor`, `area`, `shelf_no`, `layer`, `rfid_code`)
SELECT `book_id`, 2, 'A', 'A-03', 2, 'RFID-000001' FROM `book` WHERE `isbn` = '9787111128069';
INSERT INTO `book_location` (`book_id`, `floor`, `area`, `shelf_no`, `layer`, `rfid_code`)
SELECT `book_id`, 3, 'B', 'B-12', 1, 'RFID-000002' FROM `book` WHERE `isbn` = '9787111213826';
INSERT INTO `book_location` (`book_id`, `floor`, `area`, `shelf_no`, `layer`, `rfid_code`)
SELECT `book_id`, 1, 'C', 'C-05', 3, 'RFID-000003' FROM `book` WHERE `isbn` = '9787115474063';
INSERT INTO `book_location` (`book_id`, `floor`, `area`, `shelf_no`, `layer`, `rfid_code`)
SELECT `book_id`, 2, 'D', 'D-08', 2, 'RFID-000004' FROM `book` WHERE `isbn` = '9787111629245';
INSERT INTO `book_location` (`book_id`, `floor`, `area`, `shelf_no`, `layer`, `rfid_code`)
SELECT `book_id`, 2, 'D', 'D-09', 1, 'RFID-000005' FROM `book` WHERE `isbn` = '9787115428028';
INSERT INTO `book_location` (`book_id`, `floor`, `area`, `shelf_no`, `layer`, `rfid_code`)
SELECT `book_id`, 2, 'E', 'E-02', 1, 'RFID-000006' FROM `book` WHERE `isbn` = '9787111636649';
INSERT INTO `book_location` (`book_id`, `floor`, `area`, `shelf_no`, `layer`, `rfid_code`)
SELECT `book_id`, 2, 'E', 'E-03', 2, 'RFID-000007' FROM `book` WHERE `isbn` = '9787111558422';
INSERT INTO `book_location` (`book_id`, `floor`, `area`, `shelf_no`, `layer`, `rfid_code`)
SELECT `book_id`, 3, 'F', 'F-01', 1, 'RFID-000008' FROM `book` WHERE `isbn` = '9787111126942';
INSERT INTO `book_location` (`book_id`, `floor`, `area`, `shelf_no`, `layer`, `rfid_code`)
SELECT `book_id`, 3, 'A', 'A-07', 1, 'RFID-000009' FROM `book` WHERE `isbn` = '9787111652038';
INSERT INTO `book_location` (`book_id`, `floor`, `area`, `shelf_no`, `layer`, `rfid_code`)
SELECT `book_id`, 4, 'G', 'G-04', 2, 'RFID-000010' FROM `book` WHERE `isbn` = '9787111605904';

CREATE TABLE IF NOT EXISTS `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` BIGINT NOT NULL COMMENT '发表评论的用户ID',
  `borrow_id` BIGINT NOT NULL COMMENT '关联借阅记录ID',
  `book_id` BIGINT NOT NULL COMMENT '被评论的图书ID',
  `content` TEXT NOT NULL COMMENT '评论正文',
  `rating` TINYINT NOT NULL COMMENT '评分，1-5分',
  `like_count` INT DEFAULT 0 COMMENT '点赞数量',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0隐藏，1显示，2审核中',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_user_id` (`user_id`),
  UNIQUE KEY `uk_comment_borrow_id` (`borrow_id`),
  KEY `idx_comment_book_id` (`book_id`),
  CONSTRAINT `fk_comment_borrow_id` FOREIGN KEY (`borrow_id`) REFERENCES `borrow_record` (`borrow_id`),
  CONSTRAINT `fk_comment_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`name_id`),
  CONSTRAINT `fk_comment_book_id` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
