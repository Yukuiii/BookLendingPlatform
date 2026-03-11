package com.example.backend.dto;

import lombok.Data;

/**
 * 新建收藏分类请求参数。
 */
@Data
public class CreateCollectionCategoryDTO {

	/**
	 * 分类名称。
	 */
	private String categoryName;
}

