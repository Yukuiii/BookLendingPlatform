package com.example.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.BorrowBookRequestDTO;
import com.example.backend.dto.BorrowRecordPageQueryDTO;
import com.example.backend.entity.Book;
import com.example.backend.entity.BookCategory;
import com.example.backend.entity.BookLocation;
import com.example.backend.entity.BorrowRecord;
import com.example.backend.entity.Comment;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.BookCategoryMapper;
import com.example.backend.mapper.BookLocationMapper;
import com.example.backend.mapper.BookMapper;
import com.example.backend.mapper.BorrowRecordMapper;
import com.example.backend.mapper.CommentMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.BorrowService;
import com.example.backend.vo.BorrowRecordPageVO;
import com.example.backend.vo.BorrowResultVO;
import com.example.backend.vo.PageResult;
import com.example.backend.vo.RenewBookVO;
import com.example.backend.vo.ReturnBookVO;

import lombok.RequiredArgsConstructor;

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
	 * 最大续借次数。
	 */
	private static final int MAX_RENEW_COUNT = 1;

	/**
	 * 普通用户最大借阅数默认值。
	 */
	private static final int DEFAULT_MAX_BORROW_COUNT = 5;

	/**
	 * 用户正常状态。
	 */
	private static final int NORMAL_USER_STATUS = 1;

	/**
	 * 管理员角色集合。
	 */
	private static final Set<Integer> ADMIN_USER_TYPES = Set.of(2, 3);

	/**
	 * 图书上架状态。
	 */
	private static final int BOOK_ON_SHELF_STATUS = 1;

	/**
	 * 借阅中状态。
	 */
	private static final int BORROWING_STATUS = 1;

	/**
	 * 已归还状态。
	 */
	private static final int RETURNED_STATUS = 2;

	/**
	 * 超期状态。
	 */
	private static final int OVERDUE_STATUS = 3;

	/**
	 * 审核中状态。
	 */
	private static final int PENDING_REVIEW_STATUS = 4;

	private final BorrowRecordMapper borrowRecordMapper;
	private final BookMapper bookMapper;
	private final UserMapper userMapper;
	private final BookCategoryMapper bookCategoryMapper;
	private final BookLocationMapper bookLocationMapper;
	private final CommentMapper commentMapper;

	/**
	 * 提交借阅申请。
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

		User user = requireAvailableUser(userId);
		refreshExpiredBorrowRecords(userId);

		int maxBorrowCount = user.getMaxBorrowCount() == null ? DEFAULT_MAX_BORROW_COUNT : user.getMaxBorrowCount();
		Long activeBorrowCount = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
			.eq(BorrowRecord::getUserId, userId)
			.in(BorrowRecord::getStatus, BORROWING_STATUS, OVERDUE_STATUS, PENDING_REVIEW_STATUS));
		if (activeBorrowCount != null && activeBorrowCount >= maxBorrowCount) {
			throw new BusinessException(String.format("已达到最大借阅数量（%d），请先完成现有借阅或等待审核结果", maxBorrowCount));
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
			.in(BorrowRecord::getStatus, BORROWING_STATUS, OVERDUE_STATUS, PENDING_REVIEW_STATUS));
		if (alreadyBorrowed != null && alreadyBorrowed > 0) {
			throw new BusinessException("你已提交过该图书的借阅申请或仍有未归还记录");
		}

		LocalDateTime now = LocalDateTime.now();
		BorrowRecord borrowRecord = new BorrowRecord();
		borrowRecord.setUserId(userId);
		borrowRecord.setBookId(bookId);
		borrowRecord.setBorrowDate(now);
		borrowRecord.setDueDate(null);
		borrowRecord.setReturnDate(null);
		borrowRecord.setRenewCount(0);
		borrowRecord.setStatus(PENDING_REVIEW_STATUS);
		borrowRecord.setOverdueDays(0);
		borrowRecord.setFineAmount(BigDecimal.ZERO);
		borrowRecord.setCreateTime(now);
		borrowRecord.setUpdateTime(now);
		borrowRecordMapper.insert(borrowRecord);

		return new BorrowResultVO(
			borrowRecord.getBorrowId(),
			bookId,
			borrowRecord.getBorrowDate(),
			borrowRecord.getDueDate(),
			borrowRecord.getStatus()
		);
	}

	/**
	 * 管理端审核通过借阅申请。
	 *
	 * @param adminUserId 管理员ID
	 * @param borrowId 借阅记录ID
	 * @return 审核结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BorrowResultVO approveBorrowRecord(Long adminUserId, Long borrowId) {
		requireAdminUser(adminUserId);
		if (borrowId == null || borrowId <= 0) {
			throw new BusinessException("借阅记录ID不合法");
		}

		BorrowRecord borrowRecord = borrowRecordMapper.selectById(borrowId);
		if (borrowRecord == null) {
			throw new BusinessException("借阅记录不存在");
		}
		if (!Objects.equals(borrowRecord.getStatus(), PENDING_REVIEW_STATUS)) {
			throw new BusinessException("当前借阅记录不在审核中");
		}

		User borrower = requireAvailableUser(borrowRecord.getUserId());
		int maxBorrowCount = borrower.getMaxBorrowCount() == null ? DEFAULT_MAX_BORROW_COUNT : borrower.getMaxBorrowCount();
		Long activeBorrowCount = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
			.eq(BorrowRecord::getUserId, borrower.getNameId())
			.in(BorrowRecord::getStatus, BORROWING_STATUS, OVERDUE_STATUS));
		if (activeBorrowCount != null && activeBorrowCount >= maxBorrowCount) {
			throw new BusinessException(String.format("该用户已达到最大借阅数量（%d）", maxBorrowCount));
		}

		Long duplicateBorrowCount = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
			.eq(BorrowRecord::getUserId, borrower.getNameId())
			.eq(BorrowRecord::getBookId, borrowRecord.getBookId())
			.in(BorrowRecord::getStatus, BORROWING_STATUS, OVERDUE_STATUS)
			.ne(BorrowRecord::getBorrowId, borrowRecord.getBorrowId()));
		if (duplicateBorrowCount != null && duplicateBorrowCount > 0) {
			throw new BusinessException("该用户已借阅此图书，无需重复审核通过");
		}

		Book book = bookMapper.selectById(borrowRecord.getBookId());
		if (book == null) {
			throw new BusinessException("图书不存在");
		}
		if (!Objects.equals(book.getStatus(), BOOK_ON_SHELF_STATUS)) {
			throw new BusinessException("图书已下架，暂不可审核通过");
		}

		// 审核通过时再原子扣减库存，确保最终借阅成功的记录才占用馆藏。
		int updated = bookMapper.update(null, new LambdaUpdateWrapper<Book>()
			.eq(Book::getBookId, borrowRecord.getBookId())
			.eq(Book::getStatus, BOOK_ON_SHELF_STATUS)
			.gt(Book::getAvailableCount, 0)
			.setSql("available_count = available_count - 1")
			.setSql("borrow_count = IFNULL(borrow_count, 0) + 1"));
		if (updated <= 0) {
			throw new BusinessException("图书库存不足，暂不可审核通过");
		}

		LocalDateTime now = LocalDateTime.now();
		borrowRecord.setBorrowDate(now);
		borrowRecord.setDueDate(now.plusDays(DEFAULT_BORROW_DAYS));
		borrowRecord.setStatus(BORROWING_STATUS);
		borrowRecord.setOverdueDays(0);
		borrowRecord.setFineAmount(BigDecimal.ZERO);
		borrowRecord.setUpdateTime(now);
		borrowRecordMapper.updateById(borrowRecord);

		return new BorrowResultVO(
			borrowRecord.getBorrowId(),
			borrowRecord.getBookId(),
			borrowRecord.getBorrowDate(),
			borrowRecord.getDueDate(),
			borrowRecord.getStatus()
		);
	}

	/**
	 * 续借图书。
	 *
	 * @param userId 用户ID
	 * @param borrowId 借阅记录ID
	 * @return 续借结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public RenewBookVO renewBook(Long userId, Long borrowId) {
		if (userId == null || userId <= 0) {
			throw new BusinessException("请先登录再续借图书");
		}
		if (borrowId == null || borrowId <= 0) {
			throw new BusinessException("借阅记录ID不合法");
		}

		requireAvailableUser(userId);
		refreshExpiredBorrowRecords(userId);

		BorrowRecord borrowRecord = borrowRecordMapper.selectById(borrowId);
		if (borrowRecord == null) {
			throw new BusinessException("借阅记录不存在");
		}
		if (!Objects.equals(borrowRecord.getUserId(), userId)) {
			throw new BusinessException("无权续借该借阅记录");
		}
		if (!Objects.equals(borrowRecord.getStatus(), BORROWING_STATUS)) {
			if (Objects.equals(borrowRecord.getStatus(), OVERDUE_STATUS)) {
				throw new BusinessException("图书已超期，请先归还后再处理");
			}
			throw new BusinessException("当前借阅记录状态不支持续借");
		}
		if (borrowRecord.getDueDate() == null) {
			throw new BusinessException("当前借阅记录缺少应还时间，暂无法续借");
		}
		int renewCount = borrowRecord.getRenewCount() == null ? 0 : borrowRecord.getRenewCount();
		if (renewCount >= MAX_RENEW_COUNT) {
			throw new BusinessException(String.format("单次借阅最多续借 %d 次", MAX_RENEW_COUNT));
		}

		LocalDateTime newDueDate = borrowRecord.getDueDate().plusDays(DEFAULT_BORROW_DAYS);
		borrowRecord.setDueDate(newDueDate);
		borrowRecord.setRenewCount(renewCount + 1);
		borrowRecord.setOverdueDays(0);
		borrowRecord.setUpdateTime(LocalDateTime.now());
		borrowRecordMapper.updateById(borrowRecord);

		return new RenewBookVO(
			borrowRecord.getBorrowId(),
			borrowRecord.getBookId(),
			borrowRecord.getDueDate(),
			borrowRecord.getRenewCount(),
			borrowRecord.getStatus()
		);
	}

	/**
	 * 归还图书。
	 *
	 * @param userId 用户ID
	 * @param borrowId 借阅记录ID
	 * @return 归还结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ReturnBookVO returnBook(Long userId, Long borrowId) {
		if (userId == null || userId <= 0) {
			throw new BusinessException("请先登录再归还图书");
		}
		if (borrowId == null || borrowId <= 0) {
			throw new BusinessException("借阅记录ID不合法");
		}

		requireAvailableUser(userId);
		return executeReturnBook(userId, borrowId, false);
	}

	/**
	 * 管理端归还图书。
	 *
	 * @param adminUserId 管理员ID
	 * @param borrowId 借阅记录ID
	 * @return 归还结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ReturnBookVO returnAdminBorrowRecord(Long adminUserId, Long borrowId) {
		requireAdminUser(adminUserId);
		if (borrowId == null || borrowId <= 0) {
			throw new BusinessException("借阅记录ID不合法");
		}
		return executeReturnBook(adminUserId, borrowId, true);
	}

	/**
	 * 刷新全部超期未归还记录状态。
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void refreshAllExpiredBorrowRecords() {
		refreshExpiredBorrowRecords(null);
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

		refreshExpiredBorrowRecords(userId);

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
		Map<Long, Comment> commentMap = resolveCommentMap(userId, records);
		List<BorrowRecordPageVO> pageRecords = records.stream()
			.map((record) -> buildBorrowRecordPageVO(record, bookMap, categoryNameMap, locationMap, commentMap))
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
	 * 校验并返回可用用户。
	 *
	 * @param userId 用户ID
	 * @return 用户实体
	 */
	private User requireAvailableUser(Long userId) {
		User user = userMapper.selectById(userId);
		if (user == null) {
			throw new BusinessException("用户不存在");
		}
		if (!Objects.equals(user.getStatus(), NORMAL_USER_STATUS)) {
			throw new BusinessException("当前账号已被禁用，无法执行该操作");
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
		User user = requireAvailableUser(userId);
		if (!ADMIN_USER_TYPES.contains(user.getUserType())) {
			throw new BusinessException("当前用户无管理权限");
		}
		return user;
	}

	/**
	 * 执行归还图书流程。
	 *
	 * @param operatorUserId 操作人ID
	 * @param borrowId 借阅记录ID
	 * @param ignoreOwner 是否忽略归属校验
	 * @return 归还结果
	 */
	private ReturnBookVO executeReturnBook(Long operatorUserId, Long borrowId, boolean ignoreOwner) {
		BorrowRecord borrowRecord = borrowRecordMapper.selectById(borrowId);
		if (borrowRecord == null) {
			throw new BusinessException("借阅记录不存在");
		}
		if (!ignoreOwner && !Objects.equals(borrowRecord.getUserId(), operatorUserId)) {
			throw new BusinessException("无权归还该借阅记录");
		}
		if (Objects.equals(borrowRecord.getStatus(), RETURNED_STATUS)) {
			throw new BusinessException("该图书已归还，请勿重复操作");
		}
		if (!Objects.equals(borrowRecord.getStatus(), BORROWING_STATUS)
			&& !Objects.equals(borrowRecord.getStatus(), OVERDUE_STATUS)) {
			throw new BusinessException("当前借阅记录状态不支持归还");
		}

		Book book = bookMapper.selectById(borrowRecord.getBookId());
		if (book == null) {
			throw new BusinessException("关联图书不存在，暂无法归还");
		}

		LocalDateTime now = LocalDateTime.now();
		int overdueDays = calculateOverdueDays(borrowRecord.getDueDate(), now);

		// 先回补库存，再更新借阅记录，确保归还成功后图书立即恢复可借数量。
		bookMapper.update(null, new LambdaUpdateWrapper<Book>()
			.eq(Book::getBookId, borrowRecord.getBookId())
			.setSql("available_count = IFNULL(available_count, 0) + 1"));

		borrowRecord.setReturnDate(now);
		borrowRecord.setStatus(RETURNED_STATUS);
		borrowRecord.setOverdueDays(overdueDays);
		borrowRecord.setFineAmount(normalizeFineAmount(borrowRecord.getFineAmount()));
		borrowRecord.setUpdateTime(now);
		borrowRecordMapper.updateById(borrowRecord);

		return new ReturnBookVO(
			borrowRecord.getBorrowId(),
			borrowRecord.getBookId(),
			borrowRecord.getReturnDate(),
			borrowRecord.getOverdueDays(),
			borrowRecord.getFineAmount(),
			borrowRecord.getStatus()
		);
	}

	/**
	 * 刷新用户已超期但仍未归还的借阅记录状态。
	 *
	 * @param userId 用户ID
	 */
	private void refreshExpiredBorrowRecords(Long userId) {
		LambdaQueryWrapper<BorrowRecord> queryWrapper = new LambdaQueryWrapper<BorrowRecord>()
			.eq(BorrowRecord::getStatus, BORROWING_STATUS);
		if (userId != null && userId > 0) {
			queryWrapper.eq(BorrowRecord::getUserId, userId);
		}

		List<BorrowRecord> activeRecords = borrowRecordMapper.selectList(queryWrapper);
		if (activeRecords == null || activeRecords.isEmpty()) {
			return;
		}

		LocalDateTime now = LocalDateTime.now();
		for (BorrowRecord record : activeRecords) {
			if (record == null || record.getBorrowId() == null) {
				continue;
			}

			int overdueDays = calculateOverdueDays(record.getDueDate(), now);
			if (overdueDays <= 0) {
				continue;
			}

			// 只同步真正已超时的记录，避免前端筛选“超期”时漏数据。
			record.setStatus(OVERDUE_STATUS);
			record.setOverdueDays(overdueDays);
			record.setFineAmount(normalizeFineAmount(record.getFineAmount()));
			record.setUpdateTime(now);
			borrowRecordMapper.updateById(record);
		}
	}

	/**
	 * 计算超期天数。
	 *
	 * @param dueDate 应还时间
	 * @param currentTime 当前时间
	 * @return 超期天数
	 */
	private int calculateOverdueDays(LocalDateTime dueDate, LocalDateTime currentTime) {
		if (dueDate == null || currentTime == null || !currentTime.isAfter(dueDate)) {
			return 0;
		}

		long days = ChronoUnit.DAYS.between(dueDate.toLocalDate(), currentTime.toLocalDate());
		return (int) Math.max(1L, days);
	}

	/**
	 * 归一化罚款金额。
	 *
	 * @param fineAmount 原罚款金额
	 * @return 归一化后的罚款金额
	 */
	private BigDecimal normalizeFineAmount(BigDecimal fineAmount) {
		return fineAmount == null ? BigDecimal.ZERO : fineAmount;
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

		List<Book> books = bookMapper.selectByIds(bookIds);
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

		List<BookCategory> categories = bookCategoryMapper.selectByIds(categoryIds);
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
	 * 批量查询借阅记录评论映射。
	 *
	 * @param userId 用户ID
	 * @param records 借阅记录列表
	 * @return 借阅记录ID到评论实体的映射
	 */
	private Map<Long, Comment> resolveCommentMap(Long userId, List<BorrowRecord> records) {
		if (userId == null || userId <= 0 || records == null || records.isEmpty()) {
			return Map.of();
		}

		List<Long> borrowIds = records.stream()
			.map(BorrowRecord::getBorrowId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		if (borrowIds.isEmpty()) {
			return Map.of();
		}

		List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
			.eq(Comment::getUserId, userId)
			.in(Comment::getBorrowId, borrowIds)
			.orderByDesc(Comment::getUpdateTime)
			.orderByDesc(Comment::getId));
		if (comments == null || comments.isEmpty()) {
			return Map.of();
		}

		Map<Long, Comment> result = new java.util.HashMap<>();
		for (Comment comment : comments) {
			if (comment == null || comment.getBorrowId() == null) {
				continue;
			}
			result.putIfAbsent(comment.getBorrowId(), comment);
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
		Map<Long, BookLocation> locationMap,
		Map<Long, Comment> commentMap
	) {
		BorrowRecordPageVO vo = new BorrowRecordPageVO();
		vo.setCommented(false);
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

		Long borrowId = record == null ? null : record.getBorrowId();
		Comment comment = borrowId == null || commentMap == null ? null : commentMap.get(borrowId);
		if (comment != null) {
			vo.setCommented(true);
			vo.setCommentId(comment.getId());
			vo.setCommentRating(comment.getRating());
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
