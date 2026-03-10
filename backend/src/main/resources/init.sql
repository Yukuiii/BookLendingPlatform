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

CREATE TABLE IF NOT EXISTS `reservation_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `reservation_date` DATETIME NOT NULL COMMENT '预约日期',
  `expire_date` DATETIME NOT NULL COMMENT '预约失效日期',
  `status` TINYINT NOT NULL COMMENT '状态：1预约中，2已借阅，3已取消，4已过期',
  `queue_position` INT DEFAULT NULL COMMENT '队列位置',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_reservation_record_user_id` (`user_id`),
  KEY `idx_reservation_record_book_id` (`book_id`),
  CONSTRAINT `fk_reservation_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`name_id`),
  CONSTRAINT `fk_reservation_record_book_id` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约记录表';

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

CREATE TABLE IF NOT EXISTS `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` BIGINT NOT NULL COMMENT '发表评论的用户ID',
  `book_id` BIGINT NOT NULL COMMENT '被评论的图书ID',
  `content` TEXT NOT NULL COMMENT '评论正文',
  `rating` TINYINT NOT NULL COMMENT '评分，1-5分',
  `like_count` INT DEFAULT 0 COMMENT '点赞数量',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0隐藏，1显示，2审核中',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_user_id` (`user_id`),
  KEY `idx_comment_book_id` (`book_id`),
  CONSTRAINT `fk_comment_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`name_id`),
  CONSTRAINT `fk_comment_book_id` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
