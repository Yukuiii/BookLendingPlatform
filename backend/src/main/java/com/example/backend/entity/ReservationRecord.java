package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 预约记录实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("reservation_record")
public class ReservationRecord {

	/**
	 * 记录ID。
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 用户ID。
	 */
	private Long userId;

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * 预约日期。
	 */
	private LocalDateTime reservationDate;

	/**
	 * 预约失效日期。
	 */
	private LocalDateTime expireDate;

	/**
	 * 状态：1预约中，2已借阅，3已取消，4已过期。
	 */
	private Integer status;

	/**
	 * 队列位置。
	 */
	private Integer queuePosition;

	/**
	 * 创建时间。
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间。
	 */
	private LocalDateTime updateTime;
}
