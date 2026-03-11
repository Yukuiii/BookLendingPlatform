package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户偏好返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceVO {

	/**
	 * 记录ID。
	 */
	private Long preferenceId;

	/**
	 * 用户ID。
	 */
	private Long userId;

	/**
	 * 偏好领域列表。
	 */
	private List<String> preferFields;

	/**
	 * 偏好难度。
	 */
	private Integer preferDifficulty;

	/**
	 * 偏好场景列表。
	 */
	private List<String> preferScenes;

	/**
	 * 是否优先推荐新书：0否，1是。
	 */
	private Integer recommendNewBook;

	/**
	 * 是否优先推荐热门书：0否，1是。
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
