package com.example.backend.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 管理端保存图书请求参数。
 */
@Data
public class AdminBookSaveDTO {

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
	 * 子领域。
	 */
	private String subField;

	/**
	 * 难度等级。
	 */
	private Integer difficultyLevel;

	/**
	 * 适用场景。
	 */
	private String suitableScene;

	/**
	 * 封面URL。
	 */
	private String coverUrl;

	/**
	 * 简介。
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
	 * 状态。
	 */
	private Integer status;
}

