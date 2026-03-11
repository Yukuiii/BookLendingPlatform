package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 借阅结果返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowResultVO {

	/**
	 * 借阅记录ID。
	 */
	private Long borrowId;

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * 申请/借阅时间。
	 */
	private LocalDateTime borrowDate;

	/**
	 * 应还日期。
	 */
	private LocalDateTime dueDate;

	/**
	 * 状态：1借阅中，4审核中。
	 */
	private Integer status;
}
