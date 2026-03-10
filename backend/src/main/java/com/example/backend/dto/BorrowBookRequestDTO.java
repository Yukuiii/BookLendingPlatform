package com.example.backend.dto;

import lombok.Data;

/**
 * 立即借阅请求参数。
 */
@Data
public class BorrowBookRequestDTO {

	/**
	 * 图书ID。
	 */
	private Long bookId;
}

