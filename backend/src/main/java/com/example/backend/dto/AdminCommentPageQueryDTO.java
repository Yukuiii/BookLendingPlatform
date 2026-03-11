package com.example.backend.dto;

import lombok.Data;

/**
 * 管理端评论分页查询参数。
 */
@Data
public class AdminCommentPageQueryDTO {

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
	 * 评论状态。
	 */
	private Integer status;
}
