package com.example.backend.dto;

import lombok.Data;

/**
 * 管理端借阅记录分页查询参数。
 */
@Data
public class AdminBorrowRecordPageQueryDTO {

	/**
	 * 当前页码。
	 */
	private Long current;

	/**
	 * 每页条数。
	 */
	private Long size;

	/**
	 * 用户名。
	 */
	private String username;

	/**
	 * 书名。
	 */
	private String bookName;

	/**
	 * 借阅状态。
	 */
	private Integer status;
}

