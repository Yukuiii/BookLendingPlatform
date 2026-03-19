package com.example.backend.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端统计数据 VO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatisticsVO {

	/**
	 * 总借阅次数。
	 */
	private Long totalBorrowCount;

	/**
	 * 活跃用户数。
	 */
	private Long activeUserCount;

	/**
	 * 超期未还图书数。
	 */
	private Long overdueBookCount;

	/**
	 * 归还图书数量。
	 */
	private Long returnedBookCount;

	/**
	 * 借阅趋势对应年份。
	 */
	private Integer trendYear;

	/**
	 * 月度借阅趋势数据。
	 */
	private List<AdminMonthlyTrendVO> monthlyBorrowTrends;

	/**
	 * 热门图书排行数据。
	 */
	private List<AdminHotBookVO> hotBookRanking;

	/**
	 * 图书借阅分类分析数据。
	 */
	private List<AdminCategoryBorrowVO> categoryBorrowAnalysis;
}
