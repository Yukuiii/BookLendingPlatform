package com.example.backend.service;

import com.example.backend.dto.AdminUserPageQueryDTO;
import com.example.backend.dto.AdminUserUpdateDTO;
import com.example.backend.dto.ChangePasswordDTO;
import com.example.backend.dto.UpdateUserProfileDTO;
import com.example.backend.vo.AdminUserPageVO;
import com.example.backend.vo.PageResult;
import com.example.backend.vo.UserProfileVO;

/**
 * 用户服务接口。
 */
public interface UserService {

	/**
	 * 查询当前用户个人信息。
	 *
	 * @param userId 用户ID
	 * @return 个人信息
	 */
	UserProfileVO getCurrentUserProfile(Long userId);

	/**
	 * 修改当前用户个人信息。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 修改请求参数
	 * @return 修改后的个人信息
	 */
	UserProfileVO updateCurrentUserProfile(Long userId, UpdateUserProfileDTO requestDTO);

	/**
	 * 修改当前用户密码。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 修改密码请求参数
	 */
	void changeCurrentUserPassword(Long userId, ChangePasswordDTO requestDTO);

	/**
	 * 管理端分页查询用户。
	 *
	 * @param adminUserId 管理员ID
	 * @param queryDTO 查询参数
	 * @return 用户分页结果
	 */
	PageResult<AdminUserPageVO> pageAdminUsers(Long adminUserId, AdminUserPageQueryDTO queryDTO);

	/**
	 * 管理端修改用户。
	 *
	 * @param adminUserId 管理员ID
	 * @param targetUserId 目标用户ID
	 * @param requestDTO 修改参数
	 * @return 修改后的用户信息
	 */
	AdminUserPageVO updateAdminUser(Long adminUserId, Long targetUserId, AdminUserUpdateDTO requestDTO);
}

