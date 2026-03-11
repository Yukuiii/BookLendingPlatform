package com.example.backend.dto;

import lombok.Data;

/**
 * 创建评论请求参数。
 */
@Data
public class CreateCommentDTO {

	/**
	 * 借阅记录ID。
	 */
	private Long borrowId;

	/**
	 * 评论正文。
	 */
	private String content;

	/**
	 * 评分，1-5分。
	 */
	private Integer rating;
}
