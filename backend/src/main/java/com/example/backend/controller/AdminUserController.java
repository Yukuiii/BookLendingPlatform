package com.example.backend.controller;

import com.example.backend.dto.AdminUserPageQueryDTO;
import com.example.backend.dto.AdminUserUpdateDTO;
import com.example.backend.service.UserService;
import com.example.backend.vo.AdminUserPageVO;
import com.example.backend.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端用户接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

	private final UserService userService;

	/**
	 * 管理端分页查询用户。
	 *
	 * @param userId 当前管理员ID
	 * @param queryDTO 查询参数
	 * @return 用户分页结果
	 */
	@GetMapping("/page")
	public PageResult<AdminUserPageVO> pageAdminUsers(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@ModelAttribute AdminUserPageQueryDTO queryDTO
	) {
		return userService.pageAdminUsers(userId, queryDTO);
	}

	/**
	 * 管理端修改用户。
	 *
	 * @param userId 当前管理员ID
	 * @param targetUserId 目标用户ID
	 * @param requestDTO 修改参数
	 * @return 修改后的用户信息
	 */
	@PutMapping("/{targetUserId}")
	public AdminUserPageVO updateAdminUser(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@PathVariable Long targetUserId,
		@RequestBody AdminUserUpdateDTO requestDTO
	) {
		return userService.updateAdminUser(userId, targetUserId, requestDTO);
	}
}

