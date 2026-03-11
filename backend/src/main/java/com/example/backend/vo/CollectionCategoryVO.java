package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏分类返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionCategoryVO {

	/**
	 * 收藏分类ID。
	 */
	private Long collectionCategoryId;

	/**
	 * 分类名称。
	 */
	private String categoryName;

	/**
	 * 排序。
	 */
	private Integer sortOrder;

	/**
	 * 是否默认分类。
	 */
	private Integer isDefault;

	/**
	 * 状态。
	 */
	private Integer status;

	/**
	 * 收藏数量。
	 */
	private Long collectionCount;
}

