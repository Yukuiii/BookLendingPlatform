package com.example.backend.dto;

import lombok.Data;

/**
 * 收藏图书请求参数。
 */
@Data
public class CollectBookRequestDTO {

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * 收藏分类ID。
	 */
	private Long collectionCategoryId;
}

