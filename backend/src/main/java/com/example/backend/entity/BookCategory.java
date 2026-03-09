package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图书分类实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("book_category")
public class BookCategory {

	/**
	 * 分类ID。
	 */
	@TableId(value = "category_id", type = IdType.AUTO)
	private Long categoryId;

	/**
	 * 分类名称。
	 */
	private String categoryName;

	/**
	 * 父分类ID。
	 */
	private Long parentId;

	/**
	 * 排序。
	 */
	private Integer sortOrder;

	/**
	 * 状态：0禁用，1启用。
	 */
	private Integer status;

	/**
	 * 创建时间。
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间。
	 */
	private LocalDateTime updateTime;
}
