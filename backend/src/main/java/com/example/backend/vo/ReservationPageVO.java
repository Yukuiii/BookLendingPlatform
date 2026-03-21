package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 我的预约记录分页返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationPageVO {

	/**
	 * 预约记录ID。
	 */
	private Long reservationId;

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * ISBN号。
	 */
	private String isbn;

	/**
	 * 书名。
	 */
	private String bookName;

	/**
	 * 作者。
	 */
	private String author;

	/**
	 * 出版社。
	 */
	private String publisher;

	/**
	 * 出版日期。
	 */
	private LocalDate publishDate;

	/**
	 * 分类名称。
	 */
	private String categoryName;

	/**
	 * 封面图片URL。
	 */
	private String coverUrl;

	/**
	 * 当前排队位置。
	 */
	private Integer queuePosition;

	/**
	 * 预约状态：1排队中，2已完成，3已失效。
	 */
	private Integer status;

	/**
	 * 兑现后的借阅记录ID。
	 */
	private Long borrowId;

	/**
	 * 预约时间。
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间。
	 */
	private LocalDateTime updateTime;
}
