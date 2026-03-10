package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.BorrowBookRequestDTO;
import com.example.backend.dto.BorrowRecordPageQueryDTO;
import com.example.backend.entity.Book;
import com.example.backend.entity.BookCategory;
import com.example.backend.entity.BookLocation;
import com.example.backend.entity.BorrowRecord;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.BookCategoryMapper;
import com.example.backend.mapper.BookLocationMapper;
import com.example.backend.mapper.BookMapper;
import com.example.backend.mapper.BorrowRecordMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.BorrowService;
import com.example.backend.vo.BorrowRecordPageVO;
import com.example.backend.vo.BorrowResultVO;
import com.example.backend.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 借阅服务实现类。
 */
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

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
	 * 默认借阅天数。
	 */
	private static final int DEFAULT_BORROW_DAYS = 30;

	/**
	 * 普通用户最大借阅数默认值。
	 */
	private static final int DEFAULT_MAX_BORROW_COUNT = 5;

	/**
	 * 图书上架状态。
	 */
	private static final int BOOK_ON_SHELF_STATUS = 1;

	/**
	 * 借阅中状态。
	 */
	private static final int BORROWING_STATUS = 1;

	/**
	 * 超期状态。
	 */
	private static final int OVERDUE_STATUS = 3;

	private final BorrowRecordMapper borrowRecordMapper;
	private final BookMapper bookMapper;
	private final UserMapper userMapper;
	private final BookCategoryMapper bookCategoryMapper;
	private final BookLocationMapper bookLocationMapper;

	/**
	 * 立即借阅图书。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 借阅请求参数
	 * @return 借阅结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BorrowResultVO borrowBook(Long userId, BorrowBookRequestDTO requestDTO) {
		Long bookId = requestDTO == null ? null : requestDTO.getBookId();
		if (userId == null || userId <= 0) {
			throw new BusinessException("请先登录再借阅图书");
		}
		if (bookId == null || bookId <= 0) {
			throw new BusinessException("图书ID不合法");
		}

		User user = userMapper.selectById(userId);
		if (user == null) {
			throw new BusinessException("用户不存在");
		}
		if (!Objects.equals(user.getStatus(), 1)) {
			throw new BusinessException("当前账号已被禁用，无法借阅");
		}

		int maxBorrowCount = user.getMaxBorrowCount() == null ? DEFAULT_MAX_BORROW_COUNT : user.getMaxBorrowCount();
		Long activeBorrowCount = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
			.eq(BorrowRecord::getUserId, userId)
			.in(BorrowRecord::getStatus, BORROWING_STATUS, OVERDUE_STATUS));
		if (activeBorrowCount != null && activeBorrowCount >= maxBorrowCount) {
			throw new BusinessException(String.format("已达到最大借阅数量（%d），请先归还部分图书", maxBorrowCount));
		}

		Book book = bookMapper.selectById(bookId);
		if (book == null) {
			throw new BusinessException("图书不存在");
		}
		if (!Objects.equals(book.getStatus(), BOOK_ON_SHELF_STATUS)) {
			throw new BusinessException("图书已下架，暂不可借阅");
		}
		if (book.getAvailableCount() == null || book.getAvailableCount() <= 0) {
			throw new BusinessException("图书库存不足，暂不可借阅");
		}

		// 禁止同一用户在未归还前重复借阅同一本书，避免占用库存产生困惑。
		Long alreadyBorrowed = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
			.eq(BorrowRecord::getUserId, userId)
			.eq(BorrowRecord::getBookId, bookId)
			.in(BorrowRecord::getStatus, BORROWING_STATUS, OVERDUE_STATUS));
		if (alreadyBorrowed != null && alreadyBorrowed > 0) {
			throw new BusinessException("你已借阅过该图书，请先归还后再借阅");
		}

		// 使用数据库原子自减，避免并发下出现库存被借成负数。
		int updated = bookMapper.update(null, new LambdaUpdateWrapper<Book>()
			.eq(Book::getBookId, bookId)
			.eq(Book::getStatus, BOOK_ON_SHELF_STATUS)
			.gt(Book::getAvailableCount, 0)
			.setSql("available_count = available_count - 1")
			.setSql("borrow_count = IFNULL(borrow_count, 0) + 1"));
		if (updated <= 0) {
			throw new BusinessException("图书库存不足，借阅失败");
		}

		LocalDateTime now = LocalDateTime.now();
		BorrowRecord borrowRecord = new BorrowRecord();
		borrowRecord.setUserId(userId);
		borrowRecord.setBookId(bookId);
		borrowRecord.setBorrowDate(now);
		borrowRecord.setDueDate(now.plusDays(DEFAULT_BORROW_DAYS));
		borrowRecord.setReturnDate(null);
		borrowRecord.setRenewCount(0);
		borrowRecord.setStatus(BORROWING_STATUS);
		borrowRecord.setOverdueDays(0);
		borrowRecord.setFineAmount(BigDecimal.ZERO);
		borrowRecord.setCreateTime(now);
		borrowRecord.setUpdateTime(now);
		borrowRecordMapper.insert(borrowRecord);

		return new BorrowResultVO(
			borrowRecord.getBorrowId(),
			bookId,
			borrowRecord.getBorrowDate(),
			borrowRecord.getDueDate()
		);
	}

	/**
	 * 分页查询我的借阅记录。
	 *
	 * @param userId 用户ID
	 * @param queryDTO 查询参数
	 * @return 借阅记录分页结果
	 */
	@Override
	public PageResult<BorrowRecordPageVO> pageMyBorrowRecords(Long userId, BorrowRecordPageQueryDTO queryDTO) {
		if (userId == null || userId <= 0) {
			throw new BusinessException("请先登录后查看借阅记录");
		}

		long current = normalizeCurrent(queryDTO);
		long size = normalizeSize(queryDTO);
		Page<BorrowRecord> page = new Page<>(current, size);
		LambdaQueryWrapper<BorrowRecord> queryWrapper = new LambdaQueryWrapper<BorrowRecord>()
			.eq(BorrowRecord::getUserId, userId);
		if (queryDTO != null && queryDTO.getStatus() != null) {
			queryWrapper.eq(BorrowRecord::getStatus, queryDTO.getStatus());
		}

		queryWrapper.orderByDesc(BorrowRecord::getBorrowDate)
			.orderByDesc(BorrowRecord::getBorrowId);

		Page<BorrowRecord> resultPage = borrowRecordMapper.selectPage(page, queryWrapper);
		List<BorrowRecord> records = resultPage.getRecords() == null ? List.of() : resultPage.getRecords();

		Map<Long, Book> bookMap = resolveBookMap(records);
		Map<Long, String> categoryNameMap = resolveCategoryNameMap(bookMap);
		Map<Long, BookLocation> locationMap = resolveLatestLocationMap(bookMap);
		List<BorrowRecordPageVO> pageRecords = records.stream()
			.map((record) -> buildBorrowRecordPageVO(record, bookMap, categoryNameMap, locationMap))
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
	 * 批量查询图书映射。
	 *
	 * @param records 借阅记录列表
	 * @return 图书ID到图书实体的映射
	 */
	private Map<Long, Book> resolveBookMap(List<BorrowRecord> records) {
		if (records == null || records.isEmpty()) {
			return Map.of();
		}

		List<Long> bookIds = records.stream()
			.map(BorrowRecord::getBookId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		if (bookIds.isEmpty()) {
			return Map.of();
		}

		List<Book> books = bookMapper.selectBatchIds(bookIds);
		if (books == null || books.isEmpty()) {
			return Map.of();
		}

		return books.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(Book::getBookId, (book) -> book, (existing, ignored) -> existing));
	}

	/**
	 * 批量查询分类名称映射。
	 *
	 * @param bookMap 图书映射
	 * @return 分类ID到分类名称的映射
	 */
	private Map<Long, String> resolveCategoryNameMap(Map<Long, Book> bookMap) {
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

		List<BookCategory> categories = bookCategoryMapper.selectBatchIds(categoryIds);
		if (categories == null || categories.isEmpty()) {
			return Map.of();
		}

		return categories.stream()
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
	 * @param bookMap 图书映射
	 * @return 图书ID到位置信息的映射
	 */
	private Map<Long, BookLocation> resolveLatestLocationMap(Map<Long, Book> bookMap) {
		if (bookMap == null || bookMap.isEmpty()) {
			return Map.of();
		}

		List<Long> bookIds = bookMap.keySet().stream()
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
	 * 构建借阅记录分页返回对象。
	 *
	 * @param record 借阅记录
	 * @param bookMap 图书映射
	 * @param categoryNameMap 分类名称映射
	 * @param locationMap 位置信息映射
	 * @return 借阅记录分页返回对象
	 */
	private BorrowRecordPageVO buildBorrowRecordPageVO(
		BorrowRecord record,
		Map<Long, Book> bookMap,
		Map<Long, String> categoryNameMap,
		Map<Long, BookLocation> locationMap
	) {
		BorrowRecordPageVO vo = new BorrowRecordPageVO();
		if (record != null) {
			vo.setBorrowId(record.getBorrowId());
			vo.setBookId(record.getBookId());
			vo.setBorrowDate(record.getBorrowDate());
			vo.setDueDate(record.getDueDate());
			vo.setReturnDate(record.getReturnDate());
			vo.setRenewCount(record.getRenewCount());
			vo.setStatus(record.getStatus());
			vo.setOverdueDays(record.getOverdueDays());
			vo.setFineAmount(record.getFineAmount());
			vo.setCreateTime(record.getCreateTime());
			vo.setUpdateTime(record.getUpdateTime());
		}

		Long bookId = record == null ? null : record.getBookId();
		Book book = bookId == null || bookMap == null ? null : bookMap.get(bookId);
		if (book != null) {
			vo.setIsbn(book.getIsbn());
			vo.setBookName(book.getBookName());
			vo.setAuthor(book.getAuthor());
			vo.setPublisher(book.getPublisher());
			vo.setPublishDate(book.getPublishDate());
			vo.setCategoryId(book.getCategoryId());
			vo.setCoverUrl(book.getCoverUrl());
			vo.setCategoryName(categoryNameMap == null ? null : categoryNameMap.get(book.getCategoryId()));
		}

		BookLocation location = bookId == null || locationMap == null ? null : locationMap.get(bookId);
		if (location != null) {
			vo.setFloor(location.getFloor());
			vo.setArea(location.getArea());
			vo.setShelfNo(location.getShelfNo());
			vo.setLayer(location.getLayer());
		}

		return vo;
	}

	/**
	 * 规范化当前页码。
	 *
	 * @param queryDTO 查询参数
	 * @return 当前页码
	 */
	private long normalizeCurrent(BorrowRecordPageQueryDTO queryDTO) {
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
	private long normalizeSize(BorrowRecordPageQueryDTO queryDTO) {
		if (queryDTO == null || queryDTO.getSize() == null || queryDTO.getSize() <= 0) {
			return DEFAULT_SIZE;
		}
		if (queryDTO.getSize() > MAX_SIZE) {
			return MAX_SIZE;
		}
		return queryDTO.getSize();
	}
}

