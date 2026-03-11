package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏图书结果返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionResultVO {

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
}

