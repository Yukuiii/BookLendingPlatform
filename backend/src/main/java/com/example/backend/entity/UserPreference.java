package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户偏好实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_preference")
public class UserPreference {

	/**
	 * 记录ID。
	 */
	@TableId(value = "preference_id", type = IdType.AUTO)
	private Long preferenceId;

	/**
	 * 用户ID。
	 */
	private Long userId;

	/**
	 * 偏好领域，多个用逗号分隔。
	 */
	private String preferFields;

	/**
	 * 偏好难度。
	 */
	private Integer preferDifficulty;

	/**
	 * 偏好场景。
	 */
	private String preferScenes;

	/**
	 * 优先推荐新书：0否，1是。
	 */
	private Integer recommendNewBook;

	/**
	 * 优先推荐热门书：0否，1是。
	 */
	private Integer recommendHotBook;

	/**
	 * 创建时间。
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间。
	 */
	private LocalDateTime updateTime;
}
