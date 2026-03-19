package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图书预约记录实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("book_reservation")
public class BookReservation {

	/**
	 * 预约记录ID。
	 */
	@TableId(value = "reservation_id", type = IdType.AUTO)
	private Long reservationId;

	/**
	 * 用户ID。
	 */
	private Long userId;

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * 队列序号。
	 */
	private Integer queueNo;

	/**
	 * 状态：1排队中，2已完成，3已失效。
	 */
	private Integer status;

	/**
	 * 兑现后的借阅记录ID。
	 */
	private Long borrowId;

	/**
	 * 创建时间。
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间。
	 */
	private LocalDateTime updateTime;
}
