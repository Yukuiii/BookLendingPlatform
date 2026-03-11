package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 我的评论分页返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentPageVO {

	/**
	 * 评论ID。
	 */
	private Long commentId;

	/**
	 * 借阅记录ID。
	 */
	private Long borrowId;

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * ISBN号。
	 */
	private String isbn;

	/**
	 * 书名。
	 */
	private String bookName;

	/**
	 * 作者。
	 */
	private String author;

	/**
	 * 出版社。
	 */
	private String publisher;

	/**
	 * 分类ID。
	 */
	private Long categoryId;

	/**
	 * 分类名称。
	 */
	private String categoryName;

	/**
	 * 封面地址。
	 */
	private String coverUrl;

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
