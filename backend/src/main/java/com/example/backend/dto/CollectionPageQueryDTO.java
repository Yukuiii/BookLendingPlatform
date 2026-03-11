package com.example.backend.dto;

import lombok.Data;

/**
 * 我的收藏分页查询参数。
 */
@Data
public class CollectionPageQueryDTO {

	/**
	 * 当前页码。
	 */
	private Long current;

	/**
	 * 每页条数。
	 */
	private Long size;

	/**
	 * 收藏分类ID。
	 */
	private Long collectionCategoryId;
}

