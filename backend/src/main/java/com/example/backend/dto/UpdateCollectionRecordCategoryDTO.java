package com.example.backend.dto;

import lombok.Data;

/**
 * 修改收藏所属分类请求参数。
 */
@Data
public class UpdateCollectionRecordCategoryDTO {

	/**
	 * 收藏分类ID。
	 */
	private Long collectionCategoryId;
}

