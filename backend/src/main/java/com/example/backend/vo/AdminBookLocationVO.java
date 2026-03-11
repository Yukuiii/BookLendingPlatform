package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理端图书位置返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookLocationVO {

	/**
	 * 位置ID。
	 */
	private Long locationId;

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * 书名。
	 */
	private String bookName;

	/**
	 * ISBN号。
	 */
	private String isbn;

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

	/**
	 * RFID码。
	 */
	private String rfidCode;

	/**
	 * 更新时间。
	 */
	private LocalDateTime updateTime;
}

