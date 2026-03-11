package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图书详情评论返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCommentVO {

	/**
	 * 评论ID。
	 */
	private Long commentId;

	/**
	 * 评论用户ID。
	 */
	private Long userId;

	/**
	 * 用户名。
	 */
	private String username;

	/**
	 * 真实姓名。
	 */
	private String realName;

	/**
	 * 评论内容。
	 */
	private String content;

	/**
	 * 评论评分。
	 */
	private Integer rating;

	/**
	 * 点赞数量。
	 */
	private Integer likeCount;

	/**
	 * 评论时间。
	 */
	private LocalDateTime createTime;
}
