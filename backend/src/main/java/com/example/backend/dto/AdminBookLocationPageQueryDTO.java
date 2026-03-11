package com.example.backend.dto;

import lombok.Data;

/**
 * 管理端图书位置分页查询参数。
 */
@Data
public class AdminBookLocationPageQueryDTO {

	/**
	 * 当前页码。
	 */
	private Long current;

	/**
	 * 每页条数。
	 */
	private Long size;

	/**
	 * 书名。
	 */
	private String bookName;

	/**
	 * ISBN号。
	 */
	private String isbn;
}

