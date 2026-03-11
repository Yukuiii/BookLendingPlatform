package com.example.backend.controller;

import com.example.backend.dto.ChangePasswordDTO;
import com.example.backend.dto.UpdateUserProfileDTO;
import com.example.backend.service.UserService;
import com.example.backend.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

	private final UserService userService;

	/**
	 * 查询当前用户个人信息。
	 *
	 * @param userId 当前用户ID
	 * @return 个人信息
	 */
	@GetMapping("/me")
	public UserProfileVO getCurrentUserProfile(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
		return userService.getCurrentUserProfile(userId);
	}

	/**
	 * 修改当前用户个人信息。
	 *
	 * @param userId 当前用户ID
	 * @param requestDTO 修改请求参数
	 * @return 修改后的个人信息
	 */
	@PutMapping("/me")
	public UserProfileVO updateCurrentUserProfile(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@RequestBody UpdateUserProfileDTO requestDTO
	) {
		return userService.updateCurrentUserProfile(userId, requestDTO);
	}

	/**
	 * 修改当前用户密码。
	 *
	 * @param userId 当前用户ID
	 * @param requestDTO 修改密码请求参数
	 */
	@PutMapping("/me/password")
	public void changeCurrentUserPassword(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@RequestBody ChangePasswordDTO requestDTO
	) {
		userService.changeCurrentUserPassword(userId, requestDTO);
	}
}

