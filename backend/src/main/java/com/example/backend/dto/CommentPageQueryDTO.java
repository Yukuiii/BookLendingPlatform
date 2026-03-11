package com.example.backend.dto;

import lombok.Data;

/**
 * 我的评论分页查询参数。
 */
@Data
public class CommentPageQueryDTO {

	/**
	 * 当前页码。
	 */
	private Long current;

	/**
	 * 每页条数。
	 */
	private Long size;
}
