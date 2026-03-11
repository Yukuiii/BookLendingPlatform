package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 图书分页列表返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookPageVO {

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
	 * 出版日期。
	 */
	private LocalDate publishDate;

	/**
	 * 分类ID。
	 */
	private Long categoryId;

	/**
	 * 分类名称。
	 */
	private String categoryName;

	/**
	 * 楼层。
	 */
	private Integer floor;

	/**
	 * 区域。
	 */
	private String area;

	/**
	 * 书架号。
	 */
	private String shelfNo;

	/**
	 * 层数。
	 */
	private Integer layer;

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
	 * 图书平均评分。
	 */
	private BigDecimal averageRating;

	/**
	 * 评分人数。
	 */
	private Integer ratingCount;

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
