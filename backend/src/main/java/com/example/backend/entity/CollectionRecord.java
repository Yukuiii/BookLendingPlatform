package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏记录实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("collection_record")
public class CollectionRecord {

	/**
	 * 记录ID。
	 */
	@TableId(value = "collection_id", type = IdType.AUTO)
	private Long collectionId;

	/**
	 * 用户ID。
	 */
	private Long userId;

	/**
	 * 图书ID。
	 */
	private Long bookId;

	/**
	 * 收藏日期。
	 */
	private LocalDateTime collectionDate;

	/**
	 * 创建时间。
	 */
	private LocalDateTime createTime;
}
