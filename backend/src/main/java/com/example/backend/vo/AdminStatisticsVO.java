package com.example.backend.vo;

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
}
