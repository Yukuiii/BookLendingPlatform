package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图书实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("book")
public class Book {

	/**
	 * 图书ID。
	 */
	@TableId(value = "book_id", type = IdType.AUTO)
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
	 * 出版日期。
	 */
	private LocalDate publishDate;

	/**
	 * 分类ID。
	 */
	private Long categoryId;

	/**
	 * 计算机子领域。
	 */
	private String subField;

	/**
	 * 难度等级：1入门，2进阶，3专家。
	 */
	private Integer difficultyLevel;

	/**
	 * 适用场景。
	 */
	private String suitableScene;

	/**
	 * 封面图片URL。
	 */
	private String coverUrl;

	/**
	 * 图书简介。
	 */
	private String description;

	/**
	 * 目录。
	 */
	private String catalog;

	/**
	 * 作者简介。
	 */
	private String authorIntro;

	/**
	 * 适用人群。
	 */
	private String targetAudience;

	/**
	 * 馆藏总数。
	 */
	private Integer totalCount;

	/**
	 * 可借数量。
	 */
	private Integer availableCount;

	/**
	 * 借阅次数。
	 */
	private Integer borrowCount;

	/**
	 * 状态：0下架，1上架。
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
