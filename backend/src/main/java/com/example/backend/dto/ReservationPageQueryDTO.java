package com.example.backend.dto;

import lombok.Data;

/**
 * 我的预约记录分页查询参数。
 */
@Data
public class ReservationPageQueryDTO {

	/**
	 * 当前页码。
	 */
	private Long current;

	/**
	 * 每页条数。
	 */
	private Long size;

	/**
	 * 预约状态：1排队中，2已完成，3已失效。
	 */
	private Integer status;
}
