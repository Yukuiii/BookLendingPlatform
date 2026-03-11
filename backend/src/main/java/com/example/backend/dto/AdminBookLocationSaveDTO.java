package com.example.backend.dto;

import lombok.Data;

/**
 * 管理端保存图书位置请求参数。
 */
@Data
public class AdminBookLocationSaveDTO {

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * 楼层。
	 */
	private Integer floor;

	/**
	 * 区域。
	 */
	private String area;

	/**
	 * 书架号。
	 */
	private String shelfNo;

	/**
	 * 层数。
	 */
	private Integer layer;
}
