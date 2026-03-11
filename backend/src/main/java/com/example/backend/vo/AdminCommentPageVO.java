package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理端评论分页返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentPageVO {

	/**
	 * 评论ID。
	 */
	private Long commentId;

	/**
	 * 用户ID。
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
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * 书名。
	 */
	private String bookName;

	/**
	 * ISBN号。
	 */
	private String isbn;

	/**
	 * 评论内容。
	 */
	private String content;

	/**
	 * 评分。
	 */
	private Integer rating;

	/**
	 * 点赞数量。
	 */
	private Integer likeCount;

	/**
	 * 评论状态：0隐藏，1显示，2审核中。
	 */
	private Integer status;

	/**
	 * 创建时间。
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间。
	 */
	private LocalDateTime updateTime;
}
