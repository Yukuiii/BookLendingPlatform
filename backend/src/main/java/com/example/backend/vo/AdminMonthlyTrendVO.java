package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端月度借阅趋势数据 VO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminMonthlyTrendVO {

	/**
	 * 月份标签。
	 */
	private String monthLabel;

	/**
	 * 借阅次数。
	 */
	private Long borrowCount;

	/**
	 * 归还次数。
	 */
	private Long returnCount;
}
