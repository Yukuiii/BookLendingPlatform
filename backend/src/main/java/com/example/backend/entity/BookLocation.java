package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图书位置实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("book_location")
public class BookLocation {

	/**
	 * 位置ID。
	 */
	@TableId(value = "location_id", type = IdType.AUTO)
	private Long locationId;

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

	/**
	 * RFID标签码。
	 */
	private String rfidCode;

	/**
	 * 创建时间。
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间。
	 */
	private LocalDateTime updateTime;
}
