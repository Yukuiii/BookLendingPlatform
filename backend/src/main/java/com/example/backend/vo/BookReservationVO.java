package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图书预约结果返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookReservationVO {

	/**
	 * 预约记录ID。
	 */
	private Long reservationId;

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * 当前排队位置。
	 */
	private Integer queuePosition;

	/**
	 * 状态：1排队中。
	 */
	private Integer status;
}
