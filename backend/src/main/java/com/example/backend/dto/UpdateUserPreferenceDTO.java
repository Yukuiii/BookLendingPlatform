package com.example.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * 修改用户偏好请求参数。
 */
@Data
public class UpdateUserPreferenceDTO {

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
}
