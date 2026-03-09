package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 借阅记录实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("borrow_record")
public class BorrowRecord {

	/**
	 * 记录ID。
	 */
	@TableId(value = "borrow_id", type = IdType.AUTO)
	private Long borrowId;

	/**
	 * 用户ID。
	 */
	private Long userId;

	/**
	 * 图书ID。
	 */
	private Long bookId;

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
