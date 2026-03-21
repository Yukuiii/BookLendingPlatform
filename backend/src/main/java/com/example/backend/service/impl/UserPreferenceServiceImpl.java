package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.dto.UpdateUserPreferenceDTO;
import com.example.backend.entity.Book;
import com.example.backend.entity.User;
import com.example.backend.entity.UserPreference;
import com.example.backend.enums.BookStatusEnum;
import com.example.backend.enums.UserStatusEnum;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.BookMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserPreferenceMapper;
import com.example.backend.service.UserPreferenceService;
import com.example.backend.vo.UserPreferenceOptionVO;
import com.example.backend.vo.UserPreferenceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 用户偏好服务实现类。
 */
@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

	/**
	 * 最大偏好项数量。
	 */
	private static final int MAX_PREFERENCE_ITEM_COUNT = 10;

	private final UserPreferenceMapper userPreferenceMapper;
	private final UserMapper userMapper;
	private final BookMapper bookMapper;

	/**
	 * 查询当前用户个性化设置。
	 *
	 * @param userId 用户ID
	 * @return 用户偏好
	 */
	@Override
	public UserPreferenceVO getCurrentUserPreference(Long userId) {
		User user = requireActiveUser(userId);
		UserPreference preference = findUserPreference(user.getNameId());
		return buildUserPreferenceVO(preference, user.getNameId());
	}

	/**
	 * 修改当前用户个性化设置。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 修改参数
	 * @return 修改后的用户偏好
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public UserPreferenceVO updateCurrentUserPreference(Long userId, UpdateUserPreferenceDTO requestDTO) {
		User user = requireActiveUser(userId);
		validatePreferenceRequest(requestDTO);

		List<String> preferFields = normalizePreferenceItems(requestDTO.getPreferFields(), "偏好领域");
		List<String> preferScenes = normalizePreferenceItems(requestDTO.getPreferScenes(), "偏好场景");
		LocalDateTime now = LocalDateTime.now();

		UserPreference preference = findUserPreference(user.getNameId());
		if (preference == null) {
			preference = new UserPreference();
			preference.setUserId(user.getNameId());
			preference.setCreateTime(now);
		}

		// 多选项统一落成逗号分隔字符串，保证表结构简单且兼容当前数据模型。
		preference.setPreferFields(joinPreferenceItems(preferFields));
		preference.setPreferDifficulty(requestDTO.getPreferDifficulty());
		preference.setPreferScenes(joinPreferenceItems(preferScenes));
		// 前端已移除隐藏推荐开关，这里统一回落为关闭，避免旧库中的历史值继续影响推荐结果。
		preference.setRecommendNewBook(0);
		preference.setRecommendHotBook(0);
		preference.setUpdateTime(now);

		if (preference.getPreferenceId() == null) {
			userPreferenceMapper.insert(preference);
		} else {
			userPreferenceMapper.updateById(preference);
		}
		return buildUserPreferenceVO(preference, user.getNameId());
	}

	/**
	 * 查询当前用户个性化设置可选项。
	 *
	 * @param userId 用户ID
	 * @return 可选项
	 */
	@Override
	public UserPreferenceOptionVO getCurrentUserPreferenceOptions(Long userId) {
		requireActiveUser(userId);

		List<Book> books = bookMapper.selectList(new LambdaQueryWrapper<Book>()
			.eq(Book::getStatus, BookStatusEnum.ON_SHELF.getCode())
			.orderByDesc(Book::getBorrowCount)
			.orderByDesc(Book::getCreateTime));

		Set<String> fieldOptions = new LinkedHashSet<>();
		Set<String> sceneOptions = new LinkedHashSet<>();
		for (Book book : books) {
			if (book == null) {
				continue;
			}
			if (StringUtils.hasText(book.getSubField())) {
				fieldOptions.add(book.getSubField().trim());
			}
			sceneOptions.addAll(splitPreferenceItems(book.getSuitableScene()));
		}

		List<String> sortedFieldOptions = fieldOptions.stream().sorted(Comparator.naturalOrder()).toList();
		List<String> sortedSceneOptions = sceneOptions.stream().sorted(Comparator.naturalOrder()).toList();
		return new UserPreferenceOptionVO(sortedFieldOptions, sortedSceneOptions);
	}

	/**
	 * 查询用户偏好实体。
	 *
	 * @param userId 用户ID
	 * @return 用户偏好实体
	 */
	private UserPreference findUserPreference(Long userId) {
		if (userId == null) {
			return null;
		}

		return userPreferenceMapper.selectOne(new LambdaQueryWrapper<UserPreference>()
			.eq(UserPreference::getUserId, userId)
			.last("limit 1"));
	}

	/**
	 * 校验并返回启用用户。
	 *
	 * @param userId 用户ID
	 * @return 用户实体
	 */
	private User requireActiveUser(Long userId) {
		if (userId == null || userId <= 0) {
			throw new BusinessException("请先登录后再设置个性化偏好");
		}

		User user = userMapper.selectById(userId);
		if (user == null) {
			throw new BusinessException("用户不存在");
		}
		if (!Objects.equals(user.getStatus(), UserStatusEnum.NORMAL.getCode())) {
			throw new BusinessException("当前账号已被禁用");
		}
		return user;
	}

	/**
	 * 校验偏好修改参数。
	 *
	 * @param requestDTO 修改参数
	 */
	private void validatePreferenceRequest(UpdateUserPreferenceDTO requestDTO) {
		if (requestDTO == null) {
			throw new BusinessException("个性化设置参数不能为空");
		}
		if (requestDTO.getPreferDifficulty() != null
			&& requestDTO.getPreferDifficulty() != 1
			&& requestDTO.getPreferDifficulty() != 2
			&& requestDTO.getPreferDifficulty() != 3) {
			throw new BusinessException("偏好难度不合法");
		}
	}

	/**
	 * 规范化偏好项列表。
	 *
	 * @param items 原始列表
	 * @param label 字段标签
	 * @return 规范化后的列表
	 */
	private List<String> normalizePreferenceItems(List<String> items, String label) {
		if (items == null || items.isEmpty()) {
			return List.of();
		}

		List<String> normalizedItems = items.stream()
			.filter(StringUtils::hasText)
			.map(String::trim)
			.distinct()
			.toList();
		if (normalizedItems.size() > MAX_PREFERENCE_ITEM_COUNT) {
			throw new BusinessException(String.format("%s最多选择 %d 项", label, MAX_PREFERENCE_ITEM_COUNT));
		}
		return normalizedItems;
	}

	/**
	 * 组装用户偏好返回对象。
	 *
	 * @param preference 偏好实体
	 * @param userId 用户ID
	 * @return 用户偏好返回对象
	 */
	private UserPreferenceVO buildUserPreferenceVO(UserPreference preference, Long userId) {
		UserPreferenceVO vo = new UserPreferenceVO();
		vo.setUserId(userId);
		vo.setPreferFields(List.of());
		vo.setPreferScenes(List.of());
		if (preference == null) {
			return vo;
		}

		vo.setPreferenceId(preference.getPreferenceId());
		vo.setUserId(preference.getUserId());
		vo.setPreferFields(splitPreferenceItems(preference.getPreferFields()));
		vo.setPreferDifficulty(preference.getPreferDifficulty());
		vo.setPreferScenes(splitPreferenceItems(preference.getPreferScenes()));
		vo.setCreateTime(preference.getCreateTime());
		vo.setUpdateTime(preference.getUpdateTime());
		return vo;
	}

	/**
	 * 将偏好列表拼接为数据库字符串。
	 *
	 * @param items 偏好列表
	 * @return 拼接结果
	 */
	private String joinPreferenceItems(List<String> items) {
		return items == null || items.isEmpty() ? null : String.join(",", items);
	}

	/**
	 * 拆分数据库中的偏好字符串。
	 *
	 * @param rawValue 原始字符串
	 * @return 偏好列表
	 */
	private List<String> splitPreferenceItems(String rawValue) {
		if (!StringUtils.hasText(rawValue)) {
			return List.of();
		}

		return java.util.Arrays.stream(rawValue.split("[,，]"))
			.map(String::trim)
			.filter(StringUtils::hasText)
			.distinct()
			.toList();
	}

}
