package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户收藏分类实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("collection_category")
public class CollectionCategory {

	/**
	 * 收藏分类ID。
	 */
	@TableId(value = "collection_category_id", type = IdType.AUTO)
	private Long collectionCategoryId;

	/**
	 * 用户ID。
	 */
	private Long userId;

	/**
	 * 分类名称。
	 */
	private String categoryName;

	/**
	 * 排序。
	 */
	private Integer sortOrder;

	/**
	 * 是否默认分类：0否，1是。
	 */
	private Integer isDefault;

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
