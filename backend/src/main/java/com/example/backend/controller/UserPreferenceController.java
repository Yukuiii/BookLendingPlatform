package com.example.backend.controller;

import com.example.backend.dto.UpdateUserPreferenceDTO;
import com.example.backend.service.UserPreferenceService;
import com.example.backend.vo.UserPreferenceOptionVO;
import com.example.backend.vo.UserPreferenceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户偏好接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user-preferences")
public class UserPreferenceController {

	private final UserPreferenceService userPreferenceService;

	/**
	 * 查询当前用户个性化设置。
	 *
	 * @param userId 当前用户ID
	 * @return 用户偏好
	 */
	@GetMapping("/me")
	public UserPreferenceVO getCurrentUserPreference(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
		return userPreferenceService.getCurrentUserPreference(userId);
	}

	/**
	 * 修改当前用户个性化设置。
	 *
	 * @param userId 当前用户ID
	 * @param requestDTO 修改参数
	 * @return 修改后的用户偏好
	 */
	@PutMapping("/me")
	public UserPreferenceVO updateCurrentUserPreference(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@RequestBody UpdateUserPreferenceDTO requestDTO
	) {
		return userPreferenceService.updateCurrentUserPreference(userId, requestDTO);
	}

	/**
	 * 查询当前用户个性化设置可选项。
	 *
	 * @param userId 当前用户ID
	 * @return 可选项
	 */
	@GetMapping("/options")
	public UserPreferenceOptionVO getCurrentUserPreferenceOptions(
		@RequestHeader(value = "X-User-Id", required = false) Long userId
	) {
		return userPreferenceService.getCurrentUserPreferenceOptions(userId);
	}
}
