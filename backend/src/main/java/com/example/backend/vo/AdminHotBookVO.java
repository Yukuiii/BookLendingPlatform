package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端热门图书排行数据 VO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminHotBookVO {

	/**
	 * 图书名称。
	 */
	private String bookName;

	/**
	 * 借阅次数。
	 */
	private Long borrowCount;
}
