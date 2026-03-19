package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端图书分类借阅分析数据 VO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCategoryBorrowVO {

	/**
	 * 分类名称。
	 */
	private String categoryName;

	/**
	 * 借阅次数。
	 */
	private Long borrowCount;
}
