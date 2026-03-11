package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 续借图书结果返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenewBookVO {

	/**
	 * 借阅记录ID。
	 */
	private Long borrowId;

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * 新的应还时间。
	 */
	private LocalDateTime dueDate;

	/**
	 * 续借次数。
	 */
	private Integer renewCount;

	/**
	 * 当前状态。
	 */
	private Integer status;
}
