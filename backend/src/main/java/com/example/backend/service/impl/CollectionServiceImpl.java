package com.example.backend.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.CollectBookRequestDTO;
import com.example.backend.dto.CollectionPageQueryDTO;
import com.example.backend.dto.CreateCollectionCategoryDTO;
import com.example.backend.dto.UpdateCollectionRecordCategoryDTO;
import com.example.backend.entity.Book;
import com.example.backend.entity.BookCategory;
import com.example.backend.entity.BookLocation;
import com.example.backend.entity.CollectionCategory;
import com.example.backend.entity.CollectionRecord;
import com.example.backend.entity.User;
import com.example.backend.enums.UserStatusEnum;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.BookCategoryMapper;
import com.example.backend.mapper.BookLocationMapper;
import com.example.backend.mapper.BookMapper;
import com.example.backend.mapper.CollectionCategoryMapper;
import com.example.backend.mapper.CollectionRecordMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.CollectionService;
import com.example.backend.vo.CollectionCategoryVO;
import com.example.backend.vo.CollectionPageVO;
import com.example.backend.vo.CollectionResultVO;
import com.example.backend.vo.PageResult;

import lombok.RequiredArgsConstructor;

/**
 * 收藏服务实现类。
 */
@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

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
	 * 收藏分类启用状态。
	 */
	private static final int CATEGORY_ENABLED_STATUS = 1;

	/**
	 * 默认收藏分类名称。
	 */
	private static final String DEFAULT_CATEGORY_NAME = "默认收藏";

	private final CollectionCategoryMapper collectionCategoryMapper;
	private final CollectionRecordMapper collectionRecordMapper;
	private final BookMapper bookMapper;
	private final BookCategoryMapper bookCategoryMapper;
	private final BookLocationMapper bookLocationMapper;
	private final UserMapper userMapper;

	/**
	 * 查询当前用户收藏分类列表。
	 *
	 * @param userId 用户ID
	 * @return 收藏分类列表
	 */
	@Override
	public List<CollectionCategoryVO> listMyCollectionCategories(Long userId) {
		requireActiveUser(userId);
		ensureDefaultCategory(userId);

		List<CollectionCategory> categories = collectionCategoryMapper.selectList(new LambdaQueryWrapper<CollectionCategory>()
			.eq(CollectionCategory::getUserId, userId)
			.eq(CollectionCategory::getStatus, CATEGORY_ENABLED_STATUS)
			.orderByDesc(CollectionCategory::getIsDefault)
			.orderByAsc(CollectionCategory::getSortOrder)
			.orderByAsc(CollectionCategory::getCollectionCategoryId));
		Map<Long, Long> countMap = resolveCollectionCountMap(userId);
		return (categories == null ? List.<CollectionCategory>of() : categories).stream()
			.map((category) -> buildCollectionCategoryVO(category, countMap.getOrDefault(category.getCollectionCategoryId(), 0L)))
			.toList();
	}

	/**
	 * 新建收藏分类。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 新建分类参数
	 * @return 新建后的分类
	 */
	@Override
	public CollectionCategoryVO createCollectionCategory(Long userId, CreateCollectionCategoryDTO requestDTO) {
		requireActiveUser(userId);
		validateCreateCategoryRequest(requestDTO);

		String categoryName = normalizeText(requestDTO.getCategoryName());
		CollectionCategory existingCategory = collectionCategoryMapper.selectOne(new LambdaQueryWrapper<CollectionCategory>()
			.eq(CollectionCategory::getUserId, userId)
			.eq(CollectionCategory::getCategoryName, categoryName)
			.last("limit 1"));
		if (existingCategory != null) {
			throw new BusinessException("收藏分类名称已存在");
		}

		CollectionCategory collectionCategory = new CollectionCategory();
		collectionCategory.setUserId(userId);
		collectionCategory.setCategoryName(categoryName);
		collectionCategory.setSortOrder(resolveNextSortOrder(userId));
		collectionCategory.setIsDefault(0);
		collectionCategory.setStatus(CATEGORY_ENABLED_STATUS);
		collectionCategory.setCreateTime(LocalDateTime.now());
		collectionCategory.setUpdateTime(LocalDateTime.now());
		collectionCategoryMapper.insert(collectionCategory);
		return buildCollectionCategoryVO(collectionCategory, 0L);
	}

	/**
	 * 删除收藏分类。
	 *
	 * @param userId 用户ID
	 * @param collectionCategoryId 收藏分类ID
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeCollectionCategory(Long userId, Long collectionCategoryId) {
		requireActiveUser(userId);
		CollectionCategory targetCategory = requireOwnedCategory(userId, collectionCategoryId);
		if (Objects.equals(targetCategory.getIsDefault(), 1)) {
			throw new BusinessException("默认收藏分类不允许删除");
		}

		CollectionCategory defaultCategory = ensureDefaultCategory(userId);
		LocalDateTime now = LocalDateTime.now();

		// 删除自定义分类前，先把该分类下的收藏统一迁移到默认收藏，避免收藏记录丢失。
		collectionRecordMapper.update(
			null,
			new LambdaUpdateWrapper<CollectionRecord>()
				.eq(CollectionRecord::getUserId, userId)
				.eq(CollectionRecord::getCollectionCategoryId, collectionCategoryId)
				.set(CollectionRecord::getCollectionCategoryId, defaultCategory.getCollectionCategoryId())
				.set(CollectionRecord::getUpdateTime, now)
		);

		collectionCategoryMapper.deleteById(collectionCategoryId);
	}

	/**
	 * 收藏图书。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 收藏参数
	 * @return 收藏结果
	 */
	@Override
	public CollectionResultVO collectBook(Long userId, CollectBookRequestDTO requestDTO) {
		requireActiveUser(userId);
		Long bookId = requestDTO == null ? null : requestDTO.getBookId();
		if (bookId == null || bookId <= 0) {
			throw new BusinessException("图书ID不合法");
		}
		if (bookMapper.selectById(bookId) == null) {
			throw new BusinessException("图书不存在");
		}

		CollectionRecord existingRecord = collectionRecordMapper.selectOne(new LambdaQueryWrapper<CollectionRecord>()
			.eq(CollectionRecord::getUserId, userId)
			.eq(CollectionRecord::getBookId, bookId)
			.last("limit 1"));
		CollectionCategory targetCategory = resolveTargetCategory(userId, requestDTO == null ? null : requestDTO.getCollectionCategoryId());
		LocalDateTime now = LocalDateTime.now();

		if (existingRecord != null) {
			if (requestDTO == null || requestDTO.getCollectionCategoryId() == null) {
				CollectionCategory currentCategory = resolveTargetCategory(userId, existingRecord.getCollectionCategoryId());
				return buildCollectionResultVO(existingRecord, currentCategory);
			}

			// 已收藏图书再次收藏时，仅更新所属分类，避免重复创建收藏记录。
			existingRecord.setCollectionCategoryId(targetCategory.getCollectionCategoryId());
			existingRecord.setUpdateTime(now);
			collectionRecordMapper.updateById(existingRecord);
			return buildCollectionResultVO(existingRecord, targetCategory);
		}

		CollectionRecord collectionRecord = new CollectionRecord();
		collectionRecord.setUserId(userId);
		collectionRecord.setBookId(bookId);
		collectionRecord.setCollectionCategoryId(targetCategory.getCollectionCategoryId());
		collectionRecord.setCollectionDate(now);
		collectionRecord.setCreateTime(now);
		collectionRecord.setUpdateTime(now);
		collectionRecordMapper.insert(collectionRecord);
		return buildCollectionResultVO(collectionRecord, targetCategory);
	}

	/**
	 * 分页查询我的收藏。
	 *
	 * @param userId 用户ID
	 * @param queryDTO 查询参数
	 * @return 收藏分页结果
	 */
	@Override
	public PageResult<CollectionPageVO> pageMyCollections(Long userId, CollectionPageQueryDTO queryDTO) {
		requireActiveUser(userId);

		Page<CollectionRecord> page = new Page<>(normalizeCurrent(queryDTO), normalizeSize(queryDTO));
		LambdaQueryWrapper<CollectionRecord> queryWrapper = new LambdaQueryWrapper<CollectionRecord>()
			.eq(CollectionRecord::getUserId, userId);
		if (queryDTO != null && queryDTO.getCollectionCategoryId() != null) {
			queryWrapper.eq(CollectionRecord::getCollectionCategoryId, queryDTO.getCollectionCategoryId());
		}

		queryWrapper.orderByDesc(CollectionRecord::getCollectionDate)
			.orderByDesc(CollectionRecord::getCollectionId);

		Page<CollectionRecord> resultPage = collectionRecordMapper.selectPage(page, queryWrapper);
		List<CollectionRecord> records = resultPage.getRecords() == null ? List.of() : resultPage.getRecords();
		Map<Long, Book> bookMap = resolveBookMap(records);
		Map<Long, String> bookCategoryNameMap = resolveBookCategoryNameMap(bookMap);
		Map<Long, CollectionCategory> collectionCategoryMap = resolveCollectionCategoryMap(userId);
		Map<Long, BookLocation> locationMap = resolveLocationMap(bookMap);

		List<CollectionPageVO> pageRecords = records.stream()
			.map((record) -> buildCollectionPageVO(record, bookMap, bookCategoryNameMap, collectionCategoryMap, locationMap))
			.toList();
		return new PageResult<>(
			pageRecords,
			resultPage.getTotal(),
			resultPage.getCurrent(),
			resultPage.getSize(),
			resultPage.getPages()
		);
	}

	/**
	 * 取消收藏。
	 *
	 * @param userId 用户ID
	 * @param collectionId 收藏记录ID
	 */
	@Override
	public void removeCollection(Long userId, Long collectionId) {
		requireActiveUser(userId);
		CollectionRecord collectionRecord = requireOwnedCollection(userId, collectionId);
		collectionRecordMapper.deleteById(collectionRecord.getCollectionId());
	}

	/**
	 * 修改收藏所属分类。
	 *
	 * @param userId 用户ID
	 * @param collectionId 收藏记录ID
	 * @param requestDTO 修改参数
	 * @return 更新后的收藏结果
	 */
	@Override
	public CollectionResultVO updateCollectionCategory(Long userId, Long collectionId, UpdateCollectionRecordCategoryDTO requestDTO) {
		requireActiveUser(userId);
		CollectionRecord collectionRecord = requireOwnedCollection(userId, collectionId);
		CollectionCategory targetCategory = resolveTargetCategory(userId, requestDTO == null ? null : requestDTO.getCollectionCategoryId());

		collectionRecord.setCollectionCategoryId(targetCategory.getCollectionCategoryId());
		collectionRecord.setUpdateTime(LocalDateTime.now());
		collectionRecordMapper.updateById(collectionRecord);
		return buildCollectionResultVO(collectionRecord, targetCategory);
	}

	/**
	 * 校验新增收藏分类参数。
	 *
	 * @param requestDTO 新建分类参数
	 */
	private void validateCreateCategoryRequest(CreateCollectionCategoryDTO requestDTO) {
		if (requestDTO == null) {
			throw new BusinessException("新增分类参数不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getCategoryName())) {
			throw new BusinessException("收藏分类名称不能为空");
		}
		if (normalizeText(requestDTO.getCategoryName()).length() > 50) {
			throw new BusinessException("收藏分类名称长度不能超过 50 位");
		}
	}

	/**
	 * 查询用户可用状态。
	 *
	 * @param userId 用户ID
	 * @return 用户实体
	 */
	private User requireActiveUser(Long userId) {
		if (userId == null || userId <= 0) {
			throw new BusinessException("用户ID不合法");
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
	 * 查询并校验归属当前用户的收藏记录。
	 *
	 * @param userId 用户ID
	 * @param collectionId 收藏记录ID
	 * @return 收藏记录
	 */
	private CollectionRecord requireOwnedCollection(Long userId, Long collectionId) {
		if (collectionId == null || collectionId <= 0) {
			throw new BusinessException("收藏记录ID不合法");
		}
		CollectionRecord collectionRecord = collectionRecordMapper.selectById(collectionId);
		if (collectionRecord == null) {
			throw new BusinessException("收藏记录不存在");
		}
		if (!Objects.equals(collectionRecord.getUserId(), userId)) {
			throw new BusinessException("无权操作该收藏记录");
		}
		return collectionRecord;
	}

	/**
	 * 查询并校验归属当前用户的收藏分类。
	 *
	 * @param userId 用户ID
	 * @param collectionCategoryId 收藏分类ID
	 * @return 收藏分类
	 */
	private CollectionCategory requireOwnedCategory(Long userId, Long collectionCategoryId) {
		if (collectionCategoryId == null || collectionCategoryId <= 0) {
			throw new BusinessException("收藏分类ID不合法");
		}
		CollectionCategory collectionCategory = collectionCategoryMapper.selectById(collectionCategoryId);
		if (collectionCategory == null || !Objects.equals(collectionCategory.getUserId(), userId)) {
			throw new BusinessException("收藏分类不存在");
		}
		if (!Objects.equals(collectionCategory.getStatus(), CATEGORY_ENABLED_STATUS)) {
			throw new BusinessException("收藏分类已不可用");
		}
		return collectionCategory;
	}

	/**
	 * 解析目标收藏分类。
	 *
	 * @param userId 用户ID
	 * @param categoryId 收藏分类ID
	 * @return 收藏分类
	 */
	private CollectionCategory resolveTargetCategory(Long userId, Long categoryId) {
		if (categoryId == null) {
			return ensureDefaultCategory(userId);
		}
		CollectionCategory collectionCategory = collectionCategoryMapper.selectById(categoryId);
		if (collectionCategory == null || !Objects.equals(collectionCategory.getUserId(), userId)) {
			throw new BusinessException("收藏分类不存在");
		}
		if (!Objects.equals(collectionCategory.getStatus(), CATEGORY_ENABLED_STATUS)) {
			throw new BusinessException("收藏分类已不可用");
		}
		return collectionCategory;
	}

	/**
	 * 确保默认收藏分类存在。
	 *
	 * @param userId 用户ID
	 * @return 默认分类
	 */
	private CollectionCategory ensureDefaultCategory(Long userId) {
		CollectionCategory defaultCategory = collectionCategoryMapper.selectOne(new LambdaQueryWrapper<CollectionCategory>()
			.eq(CollectionCategory::getUserId, userId)
			.eq(CollectionCategory::getIsDefault, 1)
			.eq(CollectionCategory::getStatus, CATEGORY_ENABLED_STATUS)
			.last("limit 1"));
		if (defaultCategory != null) {
			return defaultCategory;
		}

		CollectionCategory collectionCategory = new CollectionCategory();
		collectionCategory.setUserId(userId);
		collectionCategory.setCategoryName(DEFAULT_CATEGORY_NAME);
		collectionCategory.setSortOrder(0);
		collectionCategory.setIsDefault(1);
		collectionCategory.setStatus(CATEGORY_ENABLED_STATUS);
		collectionCategory.setCreateTime(LocalDateTime.now());
		collectionCategory.setUpdateTime(LocalDateTime.now());
		collectionCategoryMapper.insert(collectionCategory);
		return collectionCategory;
	}

	/**
	 * 计算用户收藏分类下的收藏数量。
	 *
	 * @param userId 用户ID
	 * @return 收藏数量映射
	 */
	private Map<Long, Long> resolveCollectionCountMap(Long userId) {
		List<CollectionRecord> records = collectionRecordMapper.selectList(new LambdaQueryWrapper<CollectionRecord>()
			.eq(CollectionRecord::getUserId, userId));
		if (records == null || records.isEmpty()) {
			return Map.of();
		}
		return records.stream()
			.filter((record) -> record.getCollectionCategoryId() != null)
			.collect(Collectors.groupingBy(CollectionRecord::getCollectionCategoryId, Collectors.counting()));
	}

	/**
	 * 查询下一排序值。
	 *
	 * @param userId 用户ID
	 * @return 排序值
	 */
	private Integer resolveNextSortOrder(Long userId) {
		CollectionCategory lastCategory = collectionCategoryMapper.selectOne(new LambdaQueryWrapper<CollectionCategory>()
			.eq(CollectionCategory::getUserId, userId)
			.orderByDesc(CollectionCategory::getSortOrder)
			.orderByDesc(CollectionCategory::getCollectionCategoryId)
			.last("limit 1"));
		return lastCategory == null || lastCategory.getSortOrder() == null ? 10 : lastCategory.getSortOrder() + 10;
	}

	/**
	 * 批量查询图书映射。
	 *
	 * @param records 收藏记录列表
	 * @return 图书映射
	 */
	private Map<Long, Book> resolveBookMap(List<CollectionRecord> records) {
		if (records == null || records.isEmpty()) {
			return Map.of();
		}

		List<Long> bookIds = records.stream()
			.map(CollectionRecord::getBookId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		if (bookIds.isEmpty()) {
			return Map.of();
		}

		return bookMapper.selectByIds(bookIds).stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(Book::getBookId, (book) -> book, (existing, ignored) -> existing));
	}

	/**
	 * 批量查询图书分类名称映射。
	 *
	 * @param bookMap 图书映射
	 * @return 图书分类名称映射
	 */
	private Map<Long, String> resolveBookCategoryNameMap(Map<Long, Book> bookMap) {
		if (bookMap == null || bookMap.isEmpty()) {
			return Map.of();
		}

		List<Long> categoryIds = bookMap.values().stream()
			.map(Book::getCategoryId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		if (categoryIds.isEmpty()) {
			return Map.of();
		}

		return bookCategoryMapper.selectByIds(categoryIds).stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(BookCategory::getCategoryId, BookCategory::getCategoryName, (existing, ignored) -> existing));
	}

	/**
	 * 查询收藏分类映射。
	 *
	 * @param userId 用户ID
	 * @return 收藏分类映射
	 */
	private Map<Long, CollectionCategory> resolveCollectionCategoryMap(Long userId) {
		List<CollectionCategory> categories = collectionCategoryMapper.selectList(new LambdaQueryWrapper<CollectionCategory>()
			.eq(CollectionCategory::getUserId, userId));
		if (categories == null || categories.isEmpty()) {
			return Map.of();
		}
		return categories.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(CollectionCategory::getCollectionCategoryId, (category) -> category, (existing, ignored) -> existing));
	}

	/**
	 * 查询图书位置映射。
	 *
	 * @param bookMap 图书映射
	 * @return 图书位置映射
	 */
	private Map<Long, BookLocation> resolveLocationMap(Map<Long, Book> bookMap) {
		if (bookMap == null || bookMap.isEmpty()) {
			return Map.of();
		}

		List<Long> bookIds = bookMap.keySet().stream().toList();
		List<BookLocation> locations = bookLocationMapper.selectList(new LambdaQueryWrapper<BookLocation>()
			.in(BookLocation::getBookId, bookIds)
			.orderByDesc(BookLocation::getUpdateTime)
			.orderByDesc(BookLocation::getLocationId));
		if (locations == null || locations.isEmpty()) {
			return Map.of();
		}

		Map<Long, BookLocation> locationMap = new HashMap<>();
		for (BookLocation location : locations) {
			if (location == null || location.getBookId() == null) {
				continue;
			}
			locationMap.putIfAbsent(location.getBookId(), location);
		}
		return locationMap;
	}

	/**
	 * 构建收藏分类返回对象。
	 *
	 * @param category 收藏分类
	 * @param collectionCount 收藏数量
	 * @return 收藏分类返回对象
	 */
	private CollectionCategoryVO buildCollectionCategoryVO(CollectionCategory category, Long collectionCount) {
		CollectionCategoryVO collectionCategoryVO = new CollectionCategoryVO();
		if (category == null) {
			return collectionCategoryVO;
		}
		collectionCategoryVO.setCollectionCategoryId(category.getCollectionCategoryId());
		collectionCategoryVO.setCategoryName(category.getCategoryName());
		collectionCategoryVO.setSortOrder(category.getSortOrder());
		collectionCategoryVO.setIsDefault(category.getIsDefault());
		collectionCategoryVO.setStatus(category.getStatus());
		collectionCategoryVO.setCollectionCount(collectionCount);
		return collectionCategoryVO;
	}

	/**
	 * 构建收藏结果返回对象。
	 *
	 * @param collectionRecord 收藏记录
	 * @param category 收藏分类
	 * @return 收藏结果返回对象
	 */
	private CollectionResultVO buildCollectionResultVO(CollectionRecord collectionRecord, CollectionCategory category) {
		return new CollectionResultVO(
			collectionRecord.getCollectionId(),
			collectionRecord.getBookId(),
			category.getCollectionCategoryId(),
			category.getCategoryName()
		);
	}

	/**
	 * 构建收藏分页返回对象。
	 *
	 * @param record 收藏记录
	 * @param bookMap 图书映射
	 * @param bookCategoryNameMap 图书分类名称映射
	 * @param collectionCategoryMap 收藏分类映射
	 * @param locationMap 图书位置映射
	 * @return 收藏分页返回对象
	 */
	private CollectionPageVO buildCollectionPageVO(
		CollectionRecord record,
		Map<Long, Book> bookMap,
		Map<Long, String> bookCategoryNameMap,
		Map<Long, CollectionCategory> collectionCategoryMap,
		Map<Long, BookLocation> locationMap
	) {
		CollectionPageVO collectionPageVO = new CollectionPageVO();
		if (record == null) {
			return collectionPageVO;
		}

		collectionPageVO.setCollectionId(record.getCollectionId());
		collectionPageVO.setBookId(record.getBookId());
		collectionPageVO.setCollectionCategoryId(record.getCollectionCategoryId());
		collectionPageVO.setCollectionDate(record.getCollectionDate());

		CollectionCategory collectionCategory = collectionCategoryMap.get(record.getCollectionCategoryId());
		if (collectionCategory != null) {
			collectionPageVO.setCollectionCategoryName(collectionCategory.getCategoryName());
		}

		Book book = bookMap.get(record.getBookId());
		if (book != null) {
			collectionPageVO.setIsbn(book.getIsbn());
			collectionPageVO.setBookName(book.getBookName());
			collectionPageVO.setAuthor(book.getAuthor());
			collectionPageVO.setPublisher(book.getPublisher());
			collectionPageVO.setCategoryName(bookCategoryNameMap.get(book.getCategoryId()));
			collectionPageVO.setSuitableScene(book.getSuitableScene());
			collectionPageVO.setCoverUrl(book.getCoverUrl());
			collectionPageVO.setStatus(book.getStatus());
		}

		BookLocation location = locationMap.get(record.getBookId());
		if (location != null) {
			collectionPageVO.setFloor(location.getFloor());
			collectionPageVO.setArea(location.getArea());
			collectionPageVO.setShelfNo(location.getShelfNo());
			collectionPageVO.setLayer(location.getLayer());
		}
		return collectionPageVO;
	}

	/**
	 * 规范化文本。
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
	private long normalizeCurrent(CollectionPageQueryDTO queryDTO) {
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
	private long normalizeSize(CollectionPageQueryDTO queryDTO) {
		if (queryDTO == null || queryDTO.getSize() == null || queryDTO.getSize() <= 0) {
			return DEFAULT_SIZE;
		}
		if (queryDTO.getSize() > MAX_SIZE) {
			return MAX_SIZE;
		}
		return queryDTO.getSize();
	}
}
