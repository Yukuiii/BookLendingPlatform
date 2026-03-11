package com.example.backend.service;

import com.example.backend.dto.UpdateUserPreferenceDTO;
import com.example.backend.vo.UserPreferenceOptionVO;
import com.example.backend.vo.UserPreferenceVO;

/**
 * 用户偏好服务接口。
 */
public interface UserPreferenceService {

	/**
	 * 查询当前用户个性化设置。
	 *
	 * @param userId 用户ID
	 * @return 用户偏好
	 */
	UserPreferenceVO getCurrentUserPreference(Long userId);

	/**
	 * 修改当前用户个性化设置。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 修改参数
	 * @return 修改后的用户偏好
	 */
	UserPreferenceVO updateCurrentUserPreference(Long userId, UpdateUserPreferenceDTO requestDTO);

	/**
	 * 查询个性化设置可选项。
	 *
	 * @param userId 用户ID
	 * @return 可选项
	 */
	UserPreferenceOptionVO getCurrentUserPreferenceOptions(Long userId);
}
