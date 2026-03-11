package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 我的借阅记录分页返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordPageVO {

	/**
	 * 借阅记录ID。
	 */
	private Long borrowId;

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
	 * 分类ID。
	 */
	private Long categoryId;

	/**
	 * 分类名称。
	 */
	private String categoryName;

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
	 * 封面图片URL。
	 */
	private String coverUrl;

	/**
	 * 是否已评论。
	 */
	private Boolean commented;

	/**
	 * 评论ID。
	 */
	private Long commentId;

	/**
	 * 评论评分。
	 */
	private Integer commentRating;

	/**
	 * 借阅日期。
	 */
	private LocalDateTime borrowDate;

	/**
	 * 应还日期。
	 */
	private LocalDateTime dueDate;

	/**
	 * 实际归还日期。
	 */
	private LocalDateTime returnDate;

	/**
	 * 续借次数。
	 */
	private Integer renewCount;

	/**
	 * 状态：1借阅中，2已归还，3超期。
	 */
	private Integer status;

	/**
	 * 超期天数。
	 */
	private Integer overdueDays;

	/**
	 * 罚款金额。
	 */
	private BigDecimal fineAmount;

	/**
	 * 创建时间。
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间。
	 */
	private LocalDateTime updateTime;
}
