package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("`comment`")
public class Comment {

	/**
	 * 评论ID。
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 发表评论的用户ID。
	 */
	private Long userId;

	/**
	 * 关联借阅记录ID。
	 */
	private Long borrowId;

	/**
	 * 被评论的图书ID。
	 */
	private Long bookId;

	/**
	 * 评论正文。
	 */
	private String content;

	/**
	 * 评分，1-5分。
	 */
	private Integer rating;

	/**
	 * 点赞数量。
	 */
	private Integer likeCount;

	/**
	 * 状态：0隐藏，1显示，2审核中。
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
