package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.RegisterRequestDTO;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.AuthService;
import com.example.backend.util.PasswordUtils;
import com.example.backend.vo.AuthUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 认证服务实现类。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	/**
	 * 正常状态。
	 */
	private static final int NORMAL_STATUS = 1;

	/**
	 * 默认最大借阅数量。
	 */
	private static final int DEFAULT_MAX_BORROW_COUNT = 5;

	/**
	 * 支持的用户类型集合。
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

	/**
	 * 身份证号正则。
	 */
	private static final Pattern IDENTITY_CARD_PATTERN = Pattern.compile("(^\\d{15}$)|(^\\d{17}[0-9Xx]$)");

	private final UserMapper userMapper;

	/**
	 * 处理登录校验逻辑。
	 *
	 * @param loginRequestDTO 登录请求参数
	 * @return 登录用户信息
	 */
	@Override
	public AuthUserVO login(LoginRequestDTO loginRequestDTO) {
		validateLoginRequest(loginRequestDTO);

		String username = normalizeText(loginRequestDTO.getUsername());
		User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
			.eq(User::getUsername, username)
			.last("limit 1"));

		if (user == null) {
			throw new BusinessException("用户名或密码错误");
		}

		// 登录时同时校验角色，避免同名账户在错误角色下误登录。
		if (!Objects.equals(user.getUserType(), loginRequestDTO.getUserType())) {
			throw new BusinessException("用户名、密码或角色不匹配");
		}

		// 账号被禁用时直接阻止登录，避免无效账号继续访问系统。
		if (!Objects.equals(user.getStatus(), NORMAL_STATUS)) {
			throw new BusinessException("当前账号已被禁用，请联系管理员");
		}

		if (!PasswordUtils.matches(loginRequestDTO.getPassword(), user.getPassword())) {
			throw new BusinessException("用户名或密码错误");
		}

		return buildAuthUserVO(user);
	}

	/**
	 * 处理用户注册逻辑。
	 *
	 * @param registerRequestDTO 注册请求参数
	 * @return 注册用户信息
	 */
	@Override
	public AuthUserVO register(RegisterRequestDTO registerRequestDTO) {
		validateRegisterRequest(registerRequestDTO);

		String username = normalizeText(registerRequestDTO.getUsername());
		String realName = normalizeText(registerRequestDTO.getRealName());
		String phone = normalizeText(registerRequestDTO.getPhone());
		String identityCard = normalizeText(registerRequestDTO.getIdentityCard());
		String email = normalizeText(registerRequestDTO.getEmail());

		// 注册前先做唯一性检查，优先给前端返回可理解的业务提示。
		ensureUsernameAvailable(username);
		ensureEmailAvailable(email);
		ensurePhoneAvailable(phone);

		User user = new User();
		user.setUsername(username);
		user.setRealName(realName);
		user.setPassword(PasswordUtils.encode(registerRequestDTO.getPassword()));
		user.setPhone(phone);
		user.setIdentityCard(identityCard);
		user.setEmail(email);
		user.setUserType(registerRequestDTO.getUserType());
		user.setStatus(NORMAL_STATUS);
		user.setMaxBorrowCount(DEFAULT_MAX_BORROW_COUNT);

		try {
			userMapper.insert(user);
		} catch (DuplicateKeyException exception) {
			throw new BusinessException("注册信息已存在，请检查用户名、邮箱或手机号");
		}

		return buildAuthUserVO(user);
	}

	/**
	 * 校验登录请求参数。
	 *
	 * @param loginRequestDTO 登录请求参数
	 */
	private void validateLoginRequest(LoginRequestDTO loginRequestDTO) {
		if (loginRequestDTO == null) {
			throw new BusinessException("登录参数不能为空");
		}
		validateRequiredText(loginRequestDTO.getUsername(), "用户名不能为空");
		validateRequiredText(loginRequestDTO.getPassword(), "密码不能为空");
		validateUserType(loginRequestDTO.getUserType());
	}

	/**
	 * 校验注册请求参数。
	 *
	 * @param registerRequestDTO 注册请求参数
	 */
	private void validateRegisterRequest(RegisterRequestDTO registerRequestDTO) {
		if (registerRequestDTO == null) {
			throw new BusinessException("注册参数不能为空");
		}
		validateRequiredText(registerRequestDTO.getUsername(), "用户名不能为空");
		validateRequiredText(registerRequestDTO.getRealName(), "真实姓名不能为空");
		validateRequiredText(registerRequestDTO.getPassword(), "密码不能为空");
		validateRequiredText(registerRequestDTO.getPhone(), "手机号不能为空");
		validateRequiredText(registerRequestDTO.getIdentityCard(), "身份证号不能为空");
		validateRequiredText(registerRequestDTO.getEmail(), "邮箱不能为空");
		validateUserType(registerRequestDTO.getUserType());

		if (normalizeText(registerRequestDTO.getUsername()).length() < 4) {
			throw new BusinessException("用户名长度不能少于 4 位");
		}
		if (registerRequestDTO.getPassword().length() < 6) {
			throw new BusinessException("密码长度不能少于 6 位");
		}
		if (!PHONE_PATTERN.matcher(normalizeText(registerRequestDTO.getPhone())).matches()) {
			throw new BusinessException("手机号格式不正确");
		}
		if (!EMAIL_PATTERN.matcher(normalizeText(registerRequestDTO.getEmail())).matches()) {
			throw new BusinessException("邮箱格式不正确");
		}
		if (!IDENTITY_CARD_PATTERN.matcher(normalizeText(registerRequestDTO.getIdentityCard())).matches()) {
			throw new BusinessException("身份证号格式不正确");
		}
	}

	/**
	 * 校验用户类型是否支持。
	 *
	 * @param userType 用户类型
	 */
	private void validateUserType(Integer userType) {
		if (userType == null || !SUPPORTED_USER_TYPES.contains(userType)) {
			throw new BusinessException("用户类型不合法");
		}
	}

	/**
	 * 校验文本参数是否为空。
	 *
	 * @param value 参数值
	 * @param message 错误提示
	 */
	private void validateRequiredText(String value, String message) {
		if (!StringUtils.hasText(value)) {
			throw new BusinessException(message);
		}
	}

	/**
	 * 校验用户名是否可用。
	 *
	 * @param username 用户名
	 */
	private void ensureUsernameAvailable(String username) {
		Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
		if (count != null && count > 0) {
			throw new BusinessException("用户名已存在");
		}
	}

	/**
	 * 校验邮箱是否可用。
	 *
	 * @param email 邮箱
	 */
	private void ensureEmailAvailable(String email) {
		Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
		if (count != null && count > 0) {
			throw new BusinessException("邮箱已被注册");
		}
	}

	/**
	 * 校验手机号是否可用。
	 *
	 * @param phone 手机号
	 */
	private void ensurePhoneAvailable(String phone) {
		Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
		if (count != null && count > 0) {
			throw new BusinessException("手机号已被注册");
		}
	}

	/**
	 * 构建认证用户返回对象。
	 *
	 * @param user 用户实体
	 * @return 认证用户信息
	 */
	private AuthUserVO buildAuthUserVO(User user) {
		AuthUserVO authUserVO = new AuthUserVO();
		authUserVO.setUserId(user.getNameId());
		authUserVO.setUsername(user.getUsername());
		authUserVO.setRealName(user.getRealName());
		authUserVO.setEmail(user.getEmail());
		authUserVO.setPhone(user.getPhone());
		authUserVO.setUserType(user.getUserType());
		return authUserVO;
	}

	/**
	 * 规范化文本内容。
	 *
	 * @param value 原始文本
	 * @return 去除首尾空白后的文本
	 */
	private String normalizeText(String value) {
		return value == null ? null : value.trim();
	}
}
