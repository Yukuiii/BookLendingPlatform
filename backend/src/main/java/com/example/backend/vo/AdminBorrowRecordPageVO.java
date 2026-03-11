package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端借阅记录分页返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminBorrowRecordPageVO {

	/**
	 * 借阅记录ID。
	 */
	private Long borrowId;

	/**
	 * 用户ID。
	 */
	private Long userId;

	/**
	 * 用户名。
	 */
	private String username;

	/**
	 * 真实姓名。
	 */
	private String realName;

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * 书名。
	 */
	private String bookName;

	/**
	 * ISBN号。
	 */
	private String isbn;

	/**
	 * 借阅时间。
	 */
	private LocalDateTime borrowDate;

	/**
	 * 应还时间。
	 */
	private LocalDateTime dueDate;

	/**
	 * 归还时间。
	 */
	private LocalDateTime returnDate;

	/**
	 * 状态：1借阅中，2已归还，3超期，4审核中。
	 */
	private Integer status;

	/**
	 * 超期天数。
	 */
	private Integer overdueDays;

	/**
	 * 罚款金额。
	 */
	private BigDecimal fineAmount;
}
