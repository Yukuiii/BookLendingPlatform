package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 我的收藏分页返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionPageVO {

	/**
	 * 收藏记录ID。
	 */
	private Long collectionId;

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * 收藏分类ID。
	 */
	private Long collectionCategoryId;

	/**
	 * 收藏分类名称。
	 */
	private String collectionCategoryName;

	/**
	 * 收藏时间。
	 */
	private LocalDateTime collectionDate;

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
	 * 图书分类名称。
	 */
	private String categoryName;

	/**
	 * 适用场景。
	 */
	private String suitableScene;

	/**
	 * 封面地址。
	 */
	private String coverUrl;

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
	 * 图书状态。
	 */
	private Integer status;
}

