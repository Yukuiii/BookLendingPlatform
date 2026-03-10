package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 归还图书结果返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnBookVO {

	/**
	 * 借阅记录ID。
	 */
	private Long borrowId;

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * 归还时间。
	 */
	private LocalDateTime returnDate;

	/**
	 * 超期天数。
	 */
	private Integer overdueDays;

	/**
	 * 罚款金额。
	 */
	private BigDecimal fineAmount;

	/**
	 * 当前状态。
	 */
	private Integer status;
}

