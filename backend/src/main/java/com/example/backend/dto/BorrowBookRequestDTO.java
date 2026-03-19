package com.example.backend.dto;

import lombok.Data;

/**
 * 图书借阅或预约请求参数。
 */
@Data
public class BorrowBookRequestDTO {

	/**
	 * 图书ID。
	 */
	private Long bookId;
}
