package com.example.backend.service.impl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.BookPageQueryDTO;
import com.example.backend.entity.Book;
import com.example.backend.entity.BookCategory;
import com.example.backend.entity.BookLocation;
import com.example.backend.entity.User;
import com.example.backend.entity.UserPreference;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.BookCategoryMapper;
import com.example.backend.mapper.BookLocationMapper;
import com.example.backend.mapper.BookMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserPreferenceMapper;
import com.example.backend.service.BookService;
import com.example.backend.vo.BookDetailVO;
import com.example.backend.vo.BookPageVO;
import com.example.backend.vo.PageResult;

import lombok.RequiredArgsConstructor;

/**
 * 图书服务实现类。
 */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

	/**
	 * 默认页码。
	 */
	private static final long DEFAULT_CURRENT = 1L;

	/**
	 * 默认推荐数量。
	 */
	private static final int DEFAULT_RECOMMEND_LIMIT = 4;

	/**
	 * 最大推荐数量。
	 */
	private static final int MAX_RECOMMEND_LIMIT = 8;

	/**
	 * 图书上架状态。
	 */
	private static final int BOOK_ON_SHELF_STATUS = 1;

	/**
	 * 用户正常状态。
	 */
	private static final int NORMAL_USER_STATUS = 1;

	/**
	 * 默认每页条数。
	 */
	private static final long DEFAULT_SIZE = 10L;

	/**
	 * 每页最大条数。
	 */
	private static final long MAX_SIZE = 50L;

	private final BookMapper bookMapper;
	private final BookCategoryMapper bookCategoryMapper;
	private final BookLocationMapper bookLocationMapper;
	private final UserPreferenceMapper userPreferenceMapper;
	private final UserMapper userMapper;

	/**
	 * 分页查询图书信息。
	 *
	 * @param queryDTO 查询参数
	 * @return 图书分页结果
	 */
	@Override
	public PageResult<BookPageVO> pageBooks(BookPageQueryDTO queryDTO) {
		long current = normalizeCurrent(queryDTO);
		long size = normalizeSize(queryDTO);
		Page<Book> page = new Page<>(current, size);
		LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();

		if (StringUtils.hasText(queryDTO.getBookName())) {
			queryWrapper.like(Book::getBookName, queryDTO.getBookName());
		}
		if (StringUtils.hasText(queryDTO.getAuthor())) {
			queryWrapper.like(Book::getAuthor, queryDTO.getAuthor());
		}
		if (queryDTO.getCategoryId() != null) {
			queryWrapper.eq(Book::getCategoryId, queryDTO.getCategoryId());
		}
		if (queryDTO.getStatus() != null) {
			queryWrapper.eq(Book::getStatus, queryDTO.getStatus());
		}

		// 优先按创建时间倒序排列，保证最新图书优先展示。
		queryWrapper.orderByDesc(Book::getCreateTime)
			.orderByDesc(Book::getBookId);

		Page<Book> resultPage = bookMapper.selectPage(page, queryWrapper);
		List<Book> books = resultPage.getRecords() == null ? List.of() : resultPage.getRecords();

		// 批量查询分类名称，避免在组装列表时产生 N+1 查询。
		Map<Long, String> categoryNameMap = resolveCategoryNameMap(books);
		Map<Long, BookLocation> locationMap = resolveLatestLocationMap(books);
		List<BookPageVO> records = books.stream()
			.map((book) -> buildBookPageVO(
				book,
				categoryNameMap.get(book.getCategoryId()),
				locationMap.get(book.getBookId())
			))
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
	 * 批量查询图书分类名称映射。
	 *
	 * @param books 图书列表
	 * @return 分类ID到分类名称的映射
	 */
	private Map<Long, String> resolveCategoryNameMap(List<Book> books) {
		if (books == null || books.isEmpty()) {
			return Map.of();
		}

		List<Long> categoryIds = books.stream()
			.map(Book::getCategoryId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		if (categoryIds.isEmpty()) {
			return Map.of();
		}

		return bookCategoryMapper.selectByIds(categoryIds).stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(
				BookCategory::getCategoryId,
				BookCategory::getCategoryName,
				(existing, ignored) -> existing
				));
	}

	/**
	 * 批量查询图书最新位置信息映射。
	 *
	 * @param books 图书列表
	 * @return 图书ID到位置信息的映射
	 */
	private Map<Long, BookLocation> resolveLatestLocationMap(List<Book> books) {
		if (books == null || books.isEmpty()) {
			return Map.of();
		}

		List<Long> bookIds = books.stream()
			.map(Book::getBookId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		if (bookIds.isEmpty()) {
			return Map.of();
		}

		List<BookLocation> locations = bookLocationMapper.selectList(new LambdaQueryWrapper<BookLocation>()
			.in(BookLocation::getBookId, bookIds)
			.orderByDesc(BookLocation::getUpdateTime)
			.orderByDesc(BookLocation::getLocationId));
		if (locations == null || locations.isEmpty()) {
			return Map.of();
		}

		Map<Long, BookLocation> result = new java.util.HashMap<>();
		for (BookLocation location : locations) {
			if (location == null || location.getBookId() == null) {
				continue;
			}
			result.putIfAbsent(location.getBookId(), location);
		}
		return result;
	}

	/**
	 * 构建图书分页返回对象。
	 *
	 * @param book 图书实体
	 * @param categoryName 分类名称
	 * @param location 位置信息
	 * @return 图书分页返回对象
	 */
	private BookPageVO buildBookPageVO(Book book, String categoryName, BookLocation location) {
		BookPageVO bookPageVO = new BookPageVO();
		if (book != null) {
			BeanUtils.copyProperties(book, bookPageVO);
		}
		bookPageVO.setCategoryName(categoryName);
		if (location != null) {
			bookPageVO.setFloor(location.getFloor());
			bookPageVO.setArea(location.getArea());
			bookPageVO.setShelfNo(location.getShelfNo());
			bookPageVO.setLayer(location.getLayer());
		}
		return bookPageVO;
	}

	/**
	 * 查询图书详情信息。
	 *
	 * @param bookId 图书ID
	 * @return 图书详情
	 */
	@Override
	public BookDetailVO getBookDetail(Long bookId) {
		if (bookId == null || bookId <= 0) {
			throw new BusinessException("图书ID不合法");
		}

		Book book = bookMapper.selectById(bookId);
		if (book == null) {
			throw new BusinessException("图书不存在");
		}

		String categoryName = resolveCategoryName(book.getCategoryId());
		BookLocation latestLocation = resolveLatestLocation(bookId);
		return buildBookDetailVO(book, categoryName, latestLocation);
	}

	/**
	 * 查询当前用户猜你喜欢图书。
	 *
	 * @param userId 用户ID
	 * @param limit 返回条数
	 * @return 推荐图书列表
	 */
	@Override
	public List<BookPageVO> listRecommendBooks(Long userId, Integer limit) {
		int normalizedLimit = normalizeRecommendLimit(limit);
		if (userId != null && userId > 0) {
			requireActiveUser(userId);
		}

		UserPreference preference = findUserPreference(userId);
		List<String> preferFields = splitPreferenceItems(preference == null ? null : preference.getPreferFields());
		List<String> preferScenes = splitPreferenceItems(preference == null ? null : preference.getPreferScenes());
		Integer preferDifficulty = preference == null ? null : preference.getPreferDifficulty();
		boolean hasPersonalPreference = (preferFields != null && !preferFields.isEmpty())
			|| preferDifficulty != null
			|| (preferScenes != null && !preferScenes.isEmpty());

		List<Book> books = bookMapper.selectList(new LambdaQueryWrapper<Book>()
			.eq(Book::getStatus, BOOK_ON_SHELF_STATUS)
			.gt(Book::getAvailableCount, 0));
		if (books == null || books.isEmpty()) {
			return List.of();
		}

		List<Book> recommendedBooks = books.stream()
			// 用户有偏好时优先按偏好匹配，无偏好时直接按总借阅次数兜底
			.sorted(Comparator
				.comparingInt((Book book) -> calculateRecommendScore(book, preferFields, preferDifficulty, preferScenes, hasPersonalPreference))
				.reversed()
				.thenComparing((Book book) -> defaultInt(book.getBorrowCount()), Comparator.reverseOrder())
				.thenComparing(this::resolveBookReferenceDate, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(Book::getBookId, Comparator.nullsLast(Comparator.reverseOrder())))
			.limit(normalizedLimit)
			.toList();

		Map<Long, String> categoryNameMap = resolveCategoryNameMap(recommendedBooks);
		Map<Long, BookLocation> locationMap = resolveLatestLocationMap(recommendedBooks);
		return recommendedBooks.stream()
			.map((book) -> buildBookPageVO(
				book,
				categoryNameMap.get(book.getCategoryId()),
				locationMap.get(book.getBookId())
			))
			.toList();
	}

	/**
	 * 查询分类名称。
	 *
	 * @param categoryId 分类ID
	 * @return 分类名称
	 */
	private String resolveCategoryName(Long categoryId) {
		if (categoryId == null) {
			return null;
		}

		BookCategory bookCategory = bookCategoryMapper.selectById(categoryId);
		return bookCategory == null ? null : bookCategory.getCategoryName();
	}

	/**
	 * 查询图书最新位置信息。
	 *
	 * @param bookId 图书ID
	 * @return 图书位置信息
	 */
	private BookLocation resolveLatestLocation(Long bookId) {
		if (bookId == null) {
			return null;
		}

		return bookLocationMapper.selectOne(new LambdaQueryWrapper<BookLocation>()
			.eq(BookLocation::getBookId, bookId)
			.orderByDesc(BookLocation::getUpdateTime)
			.orderByDesc(BookLocation::getLocationId)
			.last("limit 1"));
	}

	/**
	 * 查询用户偏好实体。
	 *
	 * @param userId 用户ID
	 * @return 偏好实体
	 */
	private UserPreference findUserPreference(Long userId) {
		if (userId == null || userId <= 0) {
			return null;
		}

		return userPreferenceMapper.selectOne(new LambdaQueryWrapper<UserPreference>()
			.eq(UserPreference::getUserId, userId)
			.last("limit 1"));
	}

	/**
	 * 校验并返回可用用户。
	 *
	 * @param userId 用户ID
	 * @return 用户实体
	 */
	private User requireActiveUser(Long userId) {
		User user = userMapper.selectById(userId);
		if (user == null) {
			throw new BusinessException("用户不存在");
		}
		if (!Objects.equals(user.getStatus(), NORMAL_USER_STATUS)) {
			throw new BusinessException("当前账号已被禁用");
		}
		return user;
	}

	/**
	 * 计算图书推荐得分。
	 *
	 * @param book 图书实体
	 * @param preferFields 偏好领域
	 * @param preferDifficulty 偏好难度
	 * @param preferScenes 偏好场景
	 * @param hasPersonalPreference 是否存在个性化偏好
	 * @return 推荐得分
	 */
	private int calculateRecommendScore(
		Book book,
		List<String> preferFields,
		Integer preferDifficulty,
		List<String> preferScenes,
		boolean hasPersonalPreference
	) {
		if (!hasPersonalPreference) {
			return 0;
		}

		int score = 0;
		if (preferFields != null && !preferFields.isEmpty() && preferFields.contains(book.getSubField())) {
			score += 45;
		}
		if (preferDifficulty != null && Objects.equals(preferDifficulty, book.getDifficultyLevel())) {
			score += 30;
		}
		if (preferScenes != null && !preferScenes.isEmpty()) {
			Set<String> bookScenes = Set.copyOf(splitPreferenceItems(book.getSuitableScene()));
			if (preferScenes.stream().anyMatch(bookScenes::contains)) {
				score += 35;
			}
		}
		return score;
	}

	/**
	 * 解析图书参考日期。
	 *
	 * @param book 图书实体
	 * @return 参考日期
	 */
	private LocalDate resolveBookReferenceDate(Book book) {
		if (book == null) {
			return null;
		}
		if (book.getPublishDate() != null) {
			return book.getPublishDate();
		}
		return book.getCreateTime() == null ? null : book.getCreateTime().toLocalDate();
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

	/**
	 * 获取整数默认值。
	 *
	 * @param value 原始值
	 * @return 规范化值
	 */
	private int defaultInt(Integer value) {
		return value == null ? 0 : value;
	}

	/**
	 * 规范化推荐数量。
	 *
	 * @param limit 原始数量
	 * @return 规范化后的数量
	 */
	private int normalizeRecommendLimit(Integer limit) {
		if (limit == null || limit <= 0) {
			return DEFAULT_RECOMMEND_LIMIT;
		}
		return Math.min(limit, MAX_RECOMMEND_LIMIT);
	}

	/**
	 * 构建图书详情返回对象。
	 *
	 * @param book 图书实体
	 * @param categoryName 分类名称
	 * @param location 位置信息
	 * @return 图书详情返回对象
	 */
	private BookDetailVO buildBookDetailVO(Book book, String categoryName, BookLocation location) {
		BookDetailVO bookDetailVO = new BookDetailVO();
		BeanUtils.copyProperties(book, bookDetailVO);
		bookDetailVO.setCategoryName(categoryName);
		if (location != null) {
			bookDetailVO.setFloor(location.getFloor());
			bookDetailVO.setArea(location.getArea());
			bookDetailVO.setShelfNo(location.getShelfNo());
			bookDetailVO.setLayer(location.getLayer());
		}
		return bookDetailVO;
	}

	/**
	 * 规范化当前页码。
	 *
	 * @param queryDTO 查询参数
	 * @return 当前页码
	 */
	private long normalizeCurrent(BookPageQueryDTO queryDTO) {
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
	private long normalizeSize(BookPageQueryDTO queryDTO) {
		if (queryDTO == null || queryDTO.getSize() == null || queryDTO.getSize() <= 0) {
			return DEFAULT_SIZE;
		}
		if (queryDTO.getSize() > MAX_SIZE) {
			return MAX_SIZE;
		}
		return queryDTO.getSize();
	}
}
