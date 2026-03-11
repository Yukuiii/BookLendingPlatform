package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.AdminUserPageQueryDTO;
import com.example.backend.dto.AdminUserUpdateDTO;
import com.example.backend.dto.ChangePasswordDTO;
import com.example.backend.dto.UpdateUserProfileDTO;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.UserService;
import com.example.backend.util.PasswordUtils;
import com.example.backend.vo.AdminUserPageVO;
import com.example.backend.vo.PageResult;
import com.example.backend.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 用户服务实现类。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	/**
	 * 默认页码。
	 */
	private static final long DEFAULT_CURRENT = 1L;

	/**
	 * 默认每页条数。
	 */
	private static final long DEFAULT_SIZE = 10L;

	/**
	 * 每页最大条数。
	 */
	private static final long MAX_SIZE = 50L;

	/**
	 * 正常状态。
	 */
	private static final int NORMAL_STATUS = 1;

	/**
	 * 支持的管理员类型。
	 */
	private static final Set<Integer> ADMIN_USER_TYPES = Set.of(2, 3);

	/**
	 * 支持的用户类型。
	 */
	private static final Set<Integer> SUPPORTED_USER_TYPES = Set.of(1, 2, 3);

	/**
	 * 手机号正则。
	 */
	private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");

	/**
	 * 邮箱正则。
	 */
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

	private final UserMapper userMapper;

	/**
	 * 查询当前用户个人信息。
	 *
	 * @param userId 用户ID
	 * @return 个人信息
	 */
	@Override
	public UserProfileVO getCurrentUserProfile(Long userId) {
		return buildUserProfileVO(requireExistingUser(userId));
	}

	/**
	 * 修改当前用户个人信息。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 修改请求参数
	 * @return 修改后的个人信息
	 */
	@Override
	public UserProfileVO updateCurrentUserProfile(Long userId, UpdateUserProfileDTO requestDTO) {
		User user = requireActiveUser(userId);
		validateUpdateProfileRequest(requestDTO);

		String email = normalizeText(requestDTO.getEmail());
		String phone = normalizeText(requestDTO.getPhone());
		String realName = normalizeText(requestDTO.getRealName());
		String major = normalizeText(requestDTO.getMajor());

		ensureEmailAvailable(email, user.getNameId());
		ensurePhoneAvailable(phone, user.getNameId());

		user.setRealName(realName);
		user.setMajor(StringUtils.hasText(major) ? major : null);
		user.setEmail(email);
		user.setPhone(phone);
		userMapper.updateById(user);
		return buildUserProfileVO(userMapper.selectById(userId));
	}

	/**
	 * 修改当前用户密码。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 修改密码请求参数
	 */
	@Override
	public void changeCurrentUserPassword(Long userId, ChangePasswordDTO requestDTO) {
		User user = requireActiveUser(userId);
		validateChangePasswordRequest(requestDTO);

		if (!PasswordUtils.matches(requestDTO.getOldPassword(), user.getPassword())) {
			throw new BusinessException("原密码不正确");
		}
		if (Objects.equals(requestDTO.getOldPassword(), requestDTO.getNewPassword())) {
			throw new BusinessException("新密码不能与原密码相同");
		}

		user.setPassword(PasswordUtils.encode(requestDTO.getNewPassword()));
		userMapper.updateById(user);
	}

	/**
	 * 管理端分页查询用户。
	 *
	 * @param adminUserId 管理员ID
	 * @param queryDTO 查询参数
	 * @return 用户分页结果
	 */
	@Override
	public PageResult<AdminUserPageVO> pageAdminUsers(Long adminUserId, AdminUserPageQueryDTO queryDTO) {
		requireAdminUser(adminUserId);

		Page<User> page = new Page<>(normalizeCurrent(queryDTO), normalizeSize(queryDTO));
		LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
		if (queryDTO != null && StringUtils.hasText(queryDTO.getUsername())) {
			queryWrapper.like(User::getUsername, normalizeText(queryDTO.getUsername()));
		}
		if (queryDTO != null && StringUtils.hasText(queryDTO.getRealName())) {
			queryWrapper.like(User::getRealName, normalizeText(queryDTO.getRealName()));
		}
		if (queryDTO != null && queryDTO.getUserType() != null) {
			queryWrapper.eq(User::getUserType, queryDTO.getUserType());
		}
		if (queryDTO != null && queryDTO.getStatus() != null) {
			queryWrapper.eq(User::getStatus, queryDTO.getStatus());
		}

		queryWrapper.orderByDesc(User::getCreateTime)
			.orderByDesc(User::getNameId);

		Page<User> resultPage = userMapper.selectPage(page, queryWrapper);
		List<AdminUserPageVO> records = (resultPage.getRecords() == null ? List.<User>of() : resultPage.getRecords()).stream()
			.map(this::buildAdminUserPageVO)
			.toList();
		return new PageResult<>(
			records,
			resultPage.getTotal(),
			resultPage.getCurrent(),
			resultPage.getSize(),
			resultPage.getPages()
		);
	}

	/**
	 * 管理端修改用户。
	 *
	 * @param adminUserId 管理员ID
	 * @param targetUserId 目标用户ID
	 * @param requestDTO 修改参数
	 * @return 修改后的用户信息
	 */
	@Override
	public AdminUserPageVO updateAdminUser(Long adminUserId, Long targetUserId, AdminUserUpdateDTO requestDTO) {
		User adminUser = requireAdminUser(adminUserId);
		User targetUser = requireExistingUser(targetUserId);
		validateAdminUserUpdateRequest(requestDTO);

		String email = normalizeText(requestDTO.getEmail());
		String phone = normalizeText(requestDTO.getPhone());
		String realName = normalizeText(requestDTO.getRealName());
		String major = normalizeText(requestDTO.getMajor());

		ensureEmailAvailable(email, targetUserId);
		ensurePhoneAvailable(phone, targetUserId);
		if (Objects.equals(adminUser.getNameId(), targetUserId) && Objects.equals(requestDTO.getStatus(), 0)) {
			throw new BusinessException("不能禁用当前登录管理员账号");
		}

		targetUser.setRealName(realName);
		targetUser.setMajor(StringUtils.hasText(major) ? major : null);
		targetUser.setEmail(email);
		targetUser.setPhone(phone);
		targetUser.setUserType(requestDTO.getUserType());
		targetUser.setMaxBorrowCount(requestDTO.getMaxBorrowCount());
		targetUser.setStatus(requestDTO.getStatus());
		userMapper.updateById(targetUser);
		return buildAdminUserPageVO(userMapper.selectById(targetUserId));
	}

	/**
	 * 校验个人信息修改参数。
	 *
	 * @param requestDTO 修改请求参数
	 */
	private void validateUpdateProfileRequest(UpdateUserProfileDTO requestDTO) {
		if (requestDTO == null) {
			throw new BusinessException("修改参数不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getRealName())) {
			throw new BusinessException("真实姓名不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getEmail())) {
			throw new BusinessException("邮箱不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getPhone())) {
			throw new BusinessException("手机号不能为空");
		}
		if (normalizeText(requestDTO.getRealName()).length() > 50) {
			throw new BusinessException("真实姓名长度不能超过 50 位");
		}
		if (StringUtils.hasText(requestDTO.getMajor()) && normalizeText(requestDTO.getMajor()).length() > 50) {
			throw new BusinessException("专业长度不能超过 50 位");
		}
		validateEmail(normalizeText(requestDTO.getEmail()));
		validatePhone(normalizeText(requestDTO.getPhone()));
	}

	/**
	 * 校验修改密码参数。
	 *
	 * @param requestDTO 修改密码请求参数
	 */
	private void validateChangePasswordRequest(ChangePasswordDTO requestDTO) {
		if (requestDTO == null) {
			throw new BusinessException("修改密码参数不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getOldPassword())) {
			throw new BusinessException("原密码不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getNewPassword())) {
			throw new BusinessException("新密码不能为空");
		}
		if (requestDTO.getNewPassword().length() < 6) {
			throw new BusinessException("新密码长度不能少于 6 位");
		}
	}

	/**
	 * 校验管理端修改用户参数。
	 *
	 * @param requestDTO 修改参数
	 */
	private void validateAdminUserUpdateRequest(AdminUserUpdateDTO requestDTO) {
		if (requestDTO == null) {
			throw new BusinessException("修改参数不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getRealName())) {
			throw new BusinessException("真实姓名不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getEmail())) {
			throw new BusinessException("邮箱不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getPhone())) {
			throw new BusinessException("手机号不能为空");
		}
		if (requestDTO.getUserType() == null || !SUPPORTED_USER_TYPES.contains(requestDTO.getUserType())) {
			throw new BusinessException("用户类型不合法");
		}
		if (requestDTO.getStatus() == null || !(requestDTO.getStatus() == 0 || requestDTO.getStatus() == 1)) {
			throw new BusinessException("用户状态不合法");
		}
		if (requestDTO.getMaxBorrowCount() == null || requestDTO.getMaxBorrowCount() <= 0) {
			throw new BusinessException("最大借阅数量必须大于 0");
		}
		validateUpdateProfileRequest(convertToProfileRequest(requestDTO));
	}

	/**
	 * 将管理端修改用户参数转换为个人信息参数。
	 *
	 * @param requestDTO 管理端修改参数
	 * @return 个人信息参数
	 */
	private UpdateUserProfileDTO convertToProfileRequest(AdminUserUpdateDTO requestDTO) {
		UpdateUserProfileDTO updateUserProfileDTO = new UpdateUserProfileDTO();
		updateUserProfileDTO.setRealName(requestDTO.getRealName());
		updateUserProfileDTO.setMajor(requestDTO.getMajor());
		updateUserProfileDTO.setEmail(requestDTO.getEmail());
		updateUserProfileDTO.setPhone(requestDTO.getPhone());
		return updateUserProfileDTO;
	}

	/**
	 * 校验邮箱格式。
	 *
	 * @param email 邮箱
	 */
	private void validateEmail(String email) {
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			throw new BusinessException("邮箱格式不正确");
		}
	}

	/**
	 * 校验手机号格式。
	 *
	 * @param phone 手机号
	 */
	private void validatePhone(String phone) {
		if (!PHONE_PATTERN.matcher(phone).matches()) {
			throw new BusinessException("手机号格式不正确");
		}
	}

	/**
	 * 校验邮箱唯一性。
	 *
	 * @param email 邮箱
	 * @param excludeUserId 排除用户ID
	 */
	private void ensureEmailAvailable(String email, Long excludeUserId) {
		User existingUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
			.eq(User::getEmail, email)
			.ne(excludeUserId != null, User::getNameId, excludeUserId)
			.last("limit 1"));
		if (existingUser != null) {
			throw new BusinessException("邮箱已被注册");
		}
	}

	/**
	 * 校验手机号唯一性。
	 *
	 * @param phone 手机号
	 * @param excludeUserId 排除用户ID
	 */
	private void ensurePhoneAvailable(String phone, Long excludeUserId) {
		User existingUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
			.eq(User::getPhone, phone)
			.ne(excludeUserId != null, User::getNameId, excludeUserId)
			.last("limit 1"));
		if (existingUser != null) {
			throw new BusinessException("手机号已被注册");
		}
	}

	/**
	 * 查询存在的用户。
	 *
	 * @param userId 用户ID
	 * @return 用户实体
	 */
	private User requireExistingUser(Long userId) {
		if (userId == null || userId <= 0) {
			throw new BusinessException("用户ID不合法");
		}
		User user = userMapper.selectById(userId);
		if (user == null) {
			throw new BusinessException("用户不存在");
		}
		return user;
	}

	/**
	 * 查询可用用户。
	 *
	 * @param userId 用户ID
	 * @return 用户实体
	 */
	private User requireActiveUser(Long userId) {
		User user = requireExistingUser(userId);
		if (!Objects.equals(user.getStatus(), NORMAL_STATUS)) {
			throw new BusinessException("当前账号已被禁用");
		}
		return user;
	}

	/**
	 * 校验管理员身份。
	 *
	 * @param userId 用户ID
	 * @return 管理员实体
	 */
	private User requireAdminUser(Long userId) {
		User user = requireActiveUser(userId);
		if (!ADMIN_USER_TYPES.contains(user.getUserType())) {
			throw new BusinessException("当前用户无管理权限");
		}
		return user;
	}

	/**
	 * 构建个人信息返回对象。
	 *
	 * @param user 用户实体
	 * @return 个人信息返回对象
	 */
	private UserProfileVO buildUserProfileVO(User user) {
		UserProfileVO userProfileVO = new UserProfileVO();
		if (user == null) {
			return userProfileVO;
		}
		BeanUtils.copyProperties(user, userProfileVO);
		userProfileVO.setUserId(user.getNameId());
		return userProfileVO;
	}

	/**
	 * 构建管理端用户分页返回对象。
	 *
	 * @param user 用户实体
	 * @return 用户分页返回对象
	 */
	private AdminUserPageVO buildAdminUserPageVO(User user) {
		AdminUserPageVO adminUserPageVO = new AdminUserPageVO();
		if (user == null) {
			return adminUserPageVO;
		}
		BeanUtils.copyProperties(user, adminUserPageVO);
		adminUserPageVO.setUserId(user.getNameId());
		return adminUserPageVO;
	}

	/**
	 * 规范化文本内容。
	 *
	 * @param value 原始文本
	 * @return 规范化后的文本
	 */
	private String normalizeText(String value) {
		return value == null ? null : value.trim();
	}

	/**
	 * 规范化当前页码。
	 *
	 * @param queryDTO 查询参数
	 * @return 当前页码
	 */
	private long normalizeCurrent(AdminUserPageQueryDTO queryDTO) {
		if (queryDTO == null || queryDTO.getCurrent() == null || queryDTO.getCurrent() <= 0) {
			return DEFAULT_CURRENT;
		}
		return queryDTO.getCurrent();
	}

	/**
	 * 规范化每页条数。
	 *
	 * @param queryDTO 查询参数
	 * @return 每页条数
	 */
	private long normalizeSize(AdminUserPageQueryDTO queryDTO) {
		if (queryDTO == null || queryDTO.getSize() == null || queryDTO.getSize() <= 0) {
			return DEFAULT_SIZE;
		}
		if (queryDTO.getSize() > MAX_SIZE) {
			return MAX_SIZE;
		}
		return queryDTO.getSize();
	}
}

