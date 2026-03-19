CREATE TABLE IF NOT EXISTS `book_reservation` (
  `reservation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `queue_no` INT NOT NULL COMMENT '队列序号',
  `status` TINYINT NOT NULL COMMENT '状态：1排队中，2已完成，3已失效',
  `borrow_id` BIGINT DEFAULT NULL COMMENT '兑现后的借阅记录ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`reservation_id`),
  KEY `idx_book_reservation_book_status_queue` (`book_id`, `status`, `queue_no`),
  KEY `idx_book_reservation_user_status` (`user_id`, `status`),
  CONSTRAINT `fk_book_reservation_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`name_id`),
  CONSTRAINT `fk_book_reservation_book_id` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书预约记录表';
