package com.example.backend.dto;

import lombok.Data;

/**
 * 我的借阅记录分页查询参数。
 */
@Data
public class BorrowRecordPageQueryDTO {

	/**
	 * 当前页码。
	 */
	private Long current;

	/**
	 * 每页条数。
	 */
	private Long size;

	/**
	 * 借阅状态：1借阅中，2已归还，3超期。
	 */
	private Integer status;
}

