package com.example.backend.dto;

import lombok.Data;

/**
 * 图书分页查询参数对象。
 */
@Data
public class BookPageQueryDTO {

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
	 * 作者。
	 */
	private String author;

	/**
	 * 分类ID。
	 */
	private Long categoryId;

	/**
	 * 图书状态。
	 */
	private Integer status;
}
