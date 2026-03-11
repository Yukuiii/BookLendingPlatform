package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户偏好可选项返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceOptionVO {

	/**
	 * 偏好领域可选项。
	 */
	private List<String> fieldOptions;

	/**
	 * 偏好场景可选项。
	 */
	private List<String> sceneOptions;
}
