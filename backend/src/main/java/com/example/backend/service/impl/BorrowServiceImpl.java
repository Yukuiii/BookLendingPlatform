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
import com.example.backend.dto.ReservationPageQueryDTO;
import com.example.backend.entity.Book;
import com.example.backend.entity.BookCategory;
import com.example.backend.entity.BookLocation;
import com.example.backend.entity.BookReservation;
import com.example.backend.entity.BorrowRecord;
import com.example.backend.entity.Comment;
import com.example.backend.entity.User;
import com.example.backend.enums.BookReservationStatusEnum;
import com.example.backend.enums.BookStatusEnum;
import com.example.backend.enums.BorrowRecordStatusEnum;
import com.example.backend.enums.UserStatusEnum;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.BookCategoryMapper;
import com.example.backend.mapper.BookLocationMapper;
import com.example.backend.mapper.BookMapper;
import com.example.backend.mapper.BookReservationMapper;
import com.example.backend.mapper.BorrowRecordMapper;
import com.example.backend.mapper.CommentMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.BorrowService;
import com.example.backend.service.NotificationService;
import com.example.backend.vo.BookReservationVO;
import com.example.backend.vo.BorrowRecordPageVO;
import com.example.backend.vo.BorrowResultVO;
import com.example.backend.vo.PageResult;
import com.example.backend.vo.RenewBookVO;
import com.example.backend.vo.ReservationPageVO;
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
	 * 超期每日罚款金额。
	 */
	private static final BigDecimal DAILY_OVERDUE_FINE = new BigDecimal("5.00");

	/**
	 * 普通用户最大借阅数默认值。
	 */
	private static final int DEFAULT_MAX_BORROW_COUNT = 5;

	/**
	 * 管理员角色集合。
	 */
	private static final Set<Integer> ADMIN_USER_TYPES = Set.of(2, 3);

	private final BorrowRecordMapper borrowRecordMapper;
	private final BookMapper bookMapper;
	private final BookReservationMapper bookReservationMapper;
	private final UserMapper userMapper;
	private final BookCategoryMapper bookCategoryMapper;
	private final BookLocationMapper bookLocationMapper;
	private final CommentMapper commentMapper;
	private final NotificationService notificationService;

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
		// 先记录用户当前是否已有这本书的排队预约，后面处理完队列后用于判断本次请求是否已经被自动兑现。
		boolean hasWaitingReservationBeforeDispatch = hasWaitingReservation(userId, bookId);

		Book book = requireBorrowableBook(bookId);
		// 借阅入口先尝试清空当前图书可兑现的预约队列，避免明明该分给预约用户的库存又被新的普通借阅请求抢走。
		processBookReservationQueue(bookId);
		if (hasWaitingReservationBeforeDispatch) {
			BorrowRecord autoAssignedBorrowRecord = findLatestActiveBorrowRecord(userId, bookId);
			// 预约兑现后生成的记录会直接是借阅中/超期，不会回到审核中；这里直接返回，避免用户重复走普通借阅流程。
			if (autoAssignedBorrowRecord != null
				&& !Objects.equals(autoAssignedBorrowRecord.getStatus(), BorrowRecordStatusEnum.PENDING_REVIEW.getCode())) {
				return buildBorrowResultVO(autoAssignedBorrowRecord);
			}
		}

		// 队列处理后重新读取图书库存，确保后续判断基于最新可借数量。
		book = requireBorrowableBook(bookId);
		validateUserBorrowCapacity(user, true, "已达到最大借阅数量（%d），请先完成现有借阅或等待审核结果");
		if (book.getAvailableCount() == null || book.getAvailableCount() <= 0) {
			throw new BusinessException("当前图书暂无可借库存，可选择预约等待归还");
		}

		// 禁止同一用户在未归还前重复借阅同一本书，避免占用库存产生困惑。
		if (hasUserActiveSameBookBorrowRecord(userId, bookId, true, null)) {
			throw new BusinessException("你已提交过该图书的借阅申请或仍有未归还记录");
		}

		return buildBorrowResultVO(createPendingBorrowRecord(userId, bookId, LocalDateTime.now()));
	}

	/**
	 * 提交图书预约申请。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 预约请求参数
	 * @return 预约结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BookReservationVO reserveBook(Long userId, BorrowBookRequestDTO requestDTO) {
		Long bookId = requestDTO == null ? null : requestDTO.getBookId();
		if (userId == null || userId <= 0) {
			throw new BusinessException("请先登录再预约图书");
		}
		if (bookId == null || bookId <= 0) {
			throw new BusinessException("图书ID不合法");
		}

		User user = requireAvailableUser(userId);
		refreshExpiredBorrowRecords(userId);

		Book book = requireBorrowableBook(bookId);
		// 预约前同样先尝试兑现队列，避免当前用户实际上已经可以直接借到书。
		processBookReservationQueue(bookId);
		// 队列处理后重新读取库存，确保“还能不能预约”的判断准确。
		book = requireBorrowableBook(bookId);
		validateUserBorrowCapacity(user, true, "已达到最大借阅数量（%d），请先完成现有借阅后再预约");
		if (book.getAvailableCount() != null && book.getAvailableCount() > 0) {
			throw new BusinessException("当前图书仍可借阅，请直接提交借阅申请");
		}
		if (hasUserActiveSameBookBorrowRecord(userId, bookId, true, null)) {
			throw new BusinessException("你已提交过该图书的借阅申请或仍有未归还记录");
		}
		if (hasWaitingReservation(userId, bookId)) {
			throw new BusinessException("你已预约过该图书，请等待系统分配");
		}

		LocalDateTime now = LocalDateTime.now();
		// 队列号只在“排队中”集合内递增，保证同一本书的预约兑现顺序稳定。
		int queuePosition = resolveNextReservationQueueNo(bookId);
		BookReservation reservation = new BookReservation();
		reservation.setUserId(userId);
		reservation.setBookId(bookId);
		reservation.setQueueNo(queuePosition);
		reservation.setStatus(BookReservationStatusEnum.WAITING.getCode());
		reservation.setBorrowId(null);
		reservation.setCreateTime(now);
		reservation.setUpdateTime(now);
		bookReservationMapper.insert(reservation);

		return new BookReservationVO(
			reservation.getReservationId(),
			bookId,
			queuePosition,
			reservation.getStatus()
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
		if (!Objects.equals(borrowRecord.getStatus(), BorrowRecordStatusEnum.PENDING_REVIEW.getCode())) {
			throw new BusinessException("当前借阅记录不在审核中");
		}

		User borrower = requireAvailableUser(borrowRecord.getUserId());
		validateUserBorrowCapacity(borrower, false, "该用户已达到最大借阅数量（%d）");

		if (hasUserActiveSameBookBorrowRecord(borrower.getNameId(), borrowRecord.getBookId(), false, borrowRecord.getBorrowId())) {
			throw new BusinessException("该用户已借阅此图书，无需重复审核通过");
		}

		Book book = requireBorrowableBook(borrowRecord.getBookId());

		// 审核通过时再原子扣减库存，确保最终借阅成功的记录才占用馆藏。
		int updated = bookMapper.update(null, new LambdaUpdateWrapper<Book>()
			.eq(Book::getBookId, borrowRecord.getBookId())
			.eq(Book::getStatus, BookStatusEnum.ON_SHELF.getCode())
			.gt(Book::getAvailableCount, 0)
			.setSql("available_count = available_count - 1")
			.setSql("borrow_count = IFNULL(borrow_count, 0) + 1"));
		if (updated <= 0) {
			throw new BusinessException("图书库存不足，暂不可审核通过");
		}

		LocalDateTime now = LocalDateTime.now();
		borrowRecord.setBorrowDate(now);
		borrowRecord.setDueDate(now.plusDays(DEFAULT_BORROW_DAYS));
		borrowRecord.setStatus(BorrowRecordStatusEnum.BORROWING.getCode());
		borrowRecord.setOverdueDays(0);
		borrowRecord.setFineAmount(BigDecimal.ZERO);
		borrowRecord.setUpdateTime(now);
		borrowRecordMapper.updateById(borrowRecord);

		return buildBorrowResultVO(borrowRecord);
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
		if (!Objects.equals(borrowRecord.getStatus(), BorrowRecordStatusEnum.BORROWING.getCode())) {
			if (Objects.equals(borrowRecord.getStatus(), BorrowRecordStatusEnum.OVERDUE.getCode())) {
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
		borrowRecord.setFineAmount(BigDecimal.ZERO);
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
	 * 刷新全部未归还记录的借阅/超期状态。
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
	 * 分页查询我的预约记录。
	 */
	@Override
	public PageResult<ReservationPageVO> pageMyReservations(Long userId, ReservationPageQueryDTO queryDTO) {
		if (userId == null || userId <= 0) {
			throw new BusinessException("请先登录后查看预约记录");
		}

		long current = normalizeReservationCurrent(queryDTO);
		long size = normalizeReservationSize(queryDTO);
		Page<BookReservation> page = new Page<>(current, size);
		LambdaQueryWrapper<BookReservation> queryWrapper = new LambdaQueryWrapper<BookReservation>()
			.eq(BookReservation::getUserId, userId);
		if (queryDTO != null && queryDTO.getStatus() != null) {
			queryWrapper.eq(BookReservation::getStatus, queryDTO.getStatus());
		}

		queryWrapper.orderByDesc(BookReservation::getCreateTime)
			.orderByDesc(BookReservation::getReservationId);

		Page<BookReservation> resultPage = bookReservationMapper.selectPage(page, queryWrapper);
		List<BookReservation> reservations = resultPage.getRecords() == null ? List.of() : resultPage.getRecords();

		// 批量查询关联的图书信息
		Map<Long, Book> bookMap = resolveReservationBookMap(reservations);
		Map<Long, String> categoryNameMap = resolveCategoryNameMap(bookMap);
		List<ReservationPageVO> pageRecords = reservations.stream()
			.map((reservation) -> buildReservationPageVO(reservation, bookMap, categoryNameMap))
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
	 * 取消预约（仅允许取消排队中的预约）。
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void cancelReservation(Long userId, Long reservationId) {
		if (userId == null || userId <= 0) {
			throw new BusinessException("请先登录后操作");
		}
		if (reservationId == null || reservationId <= 0) {
			throw new BusinessException("预约记录ID无效");
		}

		BookReservation reservation = bookReservationMapper.selectById(reservationId);
		if (reservation == null) {
			throw new BusinessException("预约记录不存在");
		}
		if (!Objects.equals(reservation.getUserId(), userId)) {
			throw new BusinessException("无权操作该预约记录");
		}
		if (!Objects.equals(reservation.getStatus(), BookReservationStatusEnum.WAITING.getCode())) {
			throw new BusinessException("仅排队中的预约可以取消");
		}

		reservation.setStatus(BookReservationStatusEnum.EXPIRED.getCode());
		reservation.setUpdateTime(LocalDateTime.now());
		bookReservationMapper.updateById(reservation);
	}

	/**
	 * 校验图书是否存在且处于上架状态。
	 *
	 * @param bookId 图书ID
	 * @return 图书实体
	 */
	private Book requireBorrowableBook(Long bookId) {
		Book book = bookMapper.selectById(bookId);
		if (book == null) {
			throw new BusinessException("图书不存在");
		}
		if (!Objects.equals(book.getStatus(), BookStatusEnum.ON_SHELF.getCode())) {
			throw new BusinessException("图书已下架，暂不可借阅");
		}
		return book;
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
		if (!Objects.equals(user.getStatus(), UserStatusEnum.NORMAL.getCode())) {
			throw new BusinessException("当前账号已被禁用，无法执行该操作");
		}
		return user;
	}

	/**
	 * 校验用户借阅容量。
	 *
	 * @param user 用户实体
	 * @param includePending 是否统计审核中记录
	 * @param messageTemplate 错误消息模板
	 */
	private void validateUserBorrowCapacity(User user, boolean includePending, String messageTemplate) {
		if (user == null || user.getNameId() == null) {
			throw new BusinessException("用户不存在");
		}

		int maxBorrowCount = resolveUserMaxBorrowCount(user);
		long activeBorrowCount = countUserActiveBorrowRecords(user.getNameId(), includePending);
		if (activeBorrowCount >= maxBorrowCount) {
			throw new BusinessException(String.format(messageTemplate, maxBorrowCount));
		}
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
	 * 计算用户当前可计入额度的借阅数量。
	 *
	 * @param userId 用户ID
	 * @param includePending 是否统计审核中记录
	 * @return 借阅数量
	 */
	private long countUserActiveBorrowRecords(Long userId, boolean includePending) {
		LambdaQueryWrapper<BorrowRecord> queryWrapper = new LambdaQueryWrapper<BorrowRecord>()
			.eq(BorrowRecord::getUserId, userId)
			.in(
				BorrowRecord::getStatus,
				includePending
					? List.of(
						BorrowRecordStatusEnum.BORROWING.getCode(),
						BorrowRecordStatusEnum.OVERDUE.getCode(),
						BorrowRecordStatusEnum.PENDING_REVIEW.getCode()
					)
					: List.of(BorrowRecordStatusEnum.BORROWING.getCode(), BorrowRecordStatusEnum.OVERDUE.getCode())
			);
		Long count = borrowRecordMapper.selectCount(queryWrapper);
		return count == null ? 0L : count;
	}

	/**
	 * 判断用户是否已存在同一本书的有效借阅记录。
	 *
	 * @param userId 用户ID
	 * @param bookId 图书ID
	 * @param includePending 是否统计审核中记录
	 * @param excludeBorrowId 需要排除的借阅记录ID
	 * @return 是否存在
	 */
	private boolean hasUserActiveSameBookBorrowRecord(Long userId, Long bookId, boolean includePending, Long excludeBorrowId) {
		LambdaQueryWrapper<BorrowRecord> queryWrapper = new LambdaQueryWrapper<BorrowRecord>()
			.eq(BorrowRecord::getUserId, userId)
			.eq(BorrowRecord::getBookId, bookId)
			.in(
				BorrowRecord::getStatus,
				includePending
					? List.of(
						BorrowRecordStatusEnum.BORROWING.getCode(),
						BorrowRecordStatusEnum.OVERDUE.getCode(),
						BorrowRecordStatusEnum.PENDING_REVIEW.getCode()
					)
					: List.of(BorrowRecordStatusEnum.BORROWING.getCode(), BorrowRecordStatusEnum.OVERDUE.getCode())
			);
		if (excludeBorrowId != null && excludeBorrowId > 0) {
			queryWrapper.ne(BorrowRecord::getBorrowId, excludeBorrowId);
		}
		Long count = borrowRecordMapper.selectCount(queryWrapper);
		return count != null && count > 0;
	}

	/**
	 * 判断用户是否存在排队中的预约记录。
	 *
	 * @param userId 用户ID
	 * @param bookId 图书ID
	 * @return 是否存在
	 */
	private boolean hasWaitingReservation(Long userId, Long bookId) {
		Long count = bookReservationMapper.selectCount(new LambdaQueryWrapper<BookReservation>()
			.eq(BookReservation::getUserId, userId)
			.eq(BookReservation::getBookId, bookId)
			.eq(BookReservation::getStatus, BookReservationStatusEnum.WAITING.getCode()));
		return count != null && count > 0;
	}

	/**
	 * 查询用户当前同一本书的有效借阅记录。
	 *
	 * @param userId 用户ID
	 * @param bookId 图书ID
	 * @return 借阅记录
	 */
	private BorrowRecord findLatestActiveBorrowRecord(Long userId, Long bookId) {
		return borrowRecordMapper.selectOne(new LambdaQueryWrapper<BorrowRecord>()
			.eq(BorrowRecord::getUserId, userId)
			.eq(BorrowRecord::getBookId, bookId)
			.in(
				BorrowRecord::getStatus,
				BorrowRecordStatusEnum.BORROWING.getCode(),
				BorrowRecordStatusEnum.OVERDUE.getCode(),
				BorrowRecordStatusEnum.PENDING_REVIEW.getCode()
			)
			.orderByDesc(BorrowRecord::getUpdateTime)
			.orderByDesc(BorrowRecord::getBorrowId)
			.last("limit 1"));
	}

	/**
	 * 创建审核中的借阅记录。
	 *
	 * @param userId 用户ID
	 * @param bookId 图书ID
	 * @param now 当前时间
	 * @return 借阅记录
	 */
	private BorrowRecord createPendingBorrowRecord(Long userId, Long bookId, LocalDateTime now) {
		BorrowRecord borrowRecord = new BorrowRecord();
		borrowRecord.setUserId(userId);
		borrowRecord.setBookId(bookId);
		borrowRecord.setBorrowDate(now);
		borrowRecord.setDueDate(null);
		borrowRecord.setReturnDate(null);
		borrowRecord.setRenewCount(0);
		borrowRecord.setStatus(BorrowRecordStatusEnum.PENDING_REVIEW.getCode());
		borrowRecord.setOverdueDays(0);
		borrowRecord.setFineAmount(BigDecimal.ZERO);
		borrowRecord.setCreateTime(now);
		borrowRecord.setUpdateTime(now);
		borrowRecordMapper.insert(borrowRecord);
		return borrowRecord;
	}

	/**
	 * 构建借阅结果返回对象。
	 *
	 * @param borrowRecord 借阅记录
	 * @return 借阅结果
	 */
	private BorrowResultVO buildBorrowResultVO(BorrowRecord borrowRecord) {
		return new BorrowResultVO(
			borrowRecord == null ? null : borrowRecord.getBorrowId(),
			borrowRecord == null ? null : borrowRecord.getBookId(),
			borrowRecord == null ? null : borrowRecord.getBorrowDate(),
			borrowRecord == null ? null : borrowRecord.getDueDate(),
			borrowRecord == null ? null : borrowRecord.getStatus()
		);
	}

	/**
	 * 获取用户最大借阅数量。
	 *
	 * @param user 用户实体
	 * @return 最大借阅数量
	 */
	private int resolveUserMaxBorrowCount(User user) {
		return user.getMaxBorrowCount() == null ? DEFAULT_MAX_BORROW_COUNT : user.getMaxBorrowCount();
	}

	/**
	 * 计算下一条预约记录的队列序号。
	 *
	 * @param bookId 图书ID
	 * @return 队列序号
	 */
	private int resolveNextReservationQueueNo(Long bookId) {
		BookReservation latestReservation = bookReservationMapper.selectOne(new LambdaQueryWrapper<BookReservation>()
			.eq(BookReservation::getBookId, bookId)
			.eq(BookReservation::getStatus, BookReservationStatusEnum.WAITING.getCode())
			.orderByDesc(BookReservation::getQueueNo)
			.orderByDesc(BookReservation::getReservationId)
			.last("limit 1"));
		if (latestReservation == null || latestReservation.getQueueNo() == null || latestReservation.getQueueNo() <= 0) {
			return 1;
		}
		return latestReservation.getQueueNo() + 1;
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
		if (Objects.equals(borrowRecord.getStatus(), BorrowRecordStatusEnum.RETURNED.getCode())) {
			throw new BusinessException("该图书已归还，请勿重复操作");
		}
		if (!Objects.equals(borrowRecord.getStatus(), BorrowRecordStatusEnum.BORROWING.getCode())
			&& !Objects.equals(borrowRecord.getStatus(), BorrowRecordStatusEnum.OVERDUE.getCode())) {
			throw new BusinessException("当前借阅记录状态不支持归还");
		}

		Book book = bookMapper.selectById(borrowRecord.getBookId());
		if (book == null) {
			throw new BusinessException("关联图书不存在，暂无法归还");
		}

		LocalDateTime now = LocalDateTime.now();
		int overdueDays = calculateOverdueDays(borrowRecord.getDueDate(), now);
		BigDecimal fineAmount = calculateFineAmount(overdueDays);

		// 先回补库存，再更新借阅记录，确保归还成功后图书立即恢复可借数量。
		bookMapper.update(null, new LambdaUpdateWrapper<Book>()
			.eq(Book::getBookId, borrowRecord.getBookId())
			.setSql("available_count = IFNULL(available_count, 0) + 1"));

		// 先把当前归还记录落库成已归还，再分配预约队列，这样后续额度校验和“是否已持有同一本书”的判断才是最新状态。
		borrowRecord.setReturnDate(now);
		borrowRecord.setStatus(BorrowRecordStatusEnum.RETURNED.getCode());
		borrowRecord.setOverdueDays(overdueDays);
		borrowRecord.setFineAmount(fineAmount);
		borrowRecord.setUpdateTime(now);
		borrowRecordMapper.updateById(borrowRecord);
		processBookReservationQueue(borrowRecord.getBookId());

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
	 * 处理指定图书的预约队列分配。
	 *
	 * @param bookId 图书ID
	 */
	private void processBookReservationQueue(Long bookId) {
		if (bookId == null || bookId <= 0) {
			return;
		}

		while (true) {
			Book book = bookMapper.selectById(bookId);
			// 图书不存在、已下架或已经没有可借库存时，当前这轮预约分配立即停止。
			if (book == null
				|| !Objects.equals(book.getStatus(), BookStatusEnum.ON_SHELF.getCode())
				|| book.getAvailableCount() == null
				|| book.getAvailableCount() <= 0) {
				return;
			}

			BookReservation reservation = bookReservationMapper.selectOne(new LambdaQueryWrapper<BookReservation>()
				.eq(BookReservation::getBookId, bookId)
				.eq(BookReservation::getStatus, BookReservationStatusEnum.WAITING.getCode())
				.orderByAsc(BookReservation::getQueueNo)
				.orderByAsc(BookReservation::getReservationId)
				.last("limit 1"));
			if (reservation == null) {
				return;
			}

			// 返回 false 代表库存扣减失败等需要终止本轮处理，返回 true 则继续看下一位是否还可以被兑现。
			if (!handleWaitingReservation(reservation, book)) {
				return;
			}
		}
	}

	/**
	 * 处理队首预约记录。
	 *
	 * @param reservation 预约记录
	 * @param book 图书实体
	 * @return 是否继续处理后续队列
	 */
	private boolean handleWaitingReservation(BookReservation reservation, Book book) {
		User user = reservation == null ? null : userMapper.selectById(reservation.getUserId());
		// 队首预约人如果已被禁用、达到借阅上限或已经持有同一本书，则当前预约无法兑现，直接失效并继续尝试后续队列。
		if (!canReservationUserReceiveBook(user, book == null ? null : book.getBookId())) {
			markReservationExpired(reservation);
			return true;
		}

		int updated = bookMapper.update(null, new LambdaUpdateWrapper<Book>()
			.eq(Book::getBookId, book.getBookId())
			.eq(Book::getStatus, BookStatusEnum.ON_SHELF.getCode())
			.gt(Book::getAvailableCount, 0)
			.setSql("available_count = available_count - 1")
			.setSql("borrow_count = IFNULL(borrow_count, 0) + 1"));
		// 这里扣减失败通常表示并发下库存已被别的请求抢先消耗，本轮队列处理应立即结束，等待下一次触发再重试。
		if (updated <= 0) {
			return false;
		}

		LocalDateTime now = LocalDateTime.now();
		// 预约兑现属于系统直借成功，因此直接生成“借阅中”记录，而不是再走管理员审核链路。
		BorrowRecord borrowRecord = new BorrowRecord();
		borrowRecord.setUserId(reservation.getUserId());
		borrowRecord.setBookId(book.getBookId());
		borrowRecord.setBorrowDate(now);
		borrowRecord.setDueDate(now.plusDays(DEFAULT_BORROW_DAYS));
		borrowRecord.setReturnDate(null);
		borrowRecord.setRenewCount(0);
		borrowRecord.setStatus(BorrowRecordStatusEnum.BORROWING.getCode());
		borrowRecord.setOverdueDays(0);
		borrowRecord.setFineAmount(BigDecimal.ZERO);
		borrowRecord.setCreateTime(now);
		borrowRecord.setUpdateTime(now);
		borrowRecordMapper.insert(borrowRecord);

		// 队首预约兑现后立即标记完成，并关联自动生成的借阅记录。
		reservation.setStatus(BookReservationStatusEnum.FULFILLED.getCode());
		reservation.setBorrowId(borrowRecord.getBorrowId());
		reservation.setUpdateTime(now);
		bookReservationMapper.updateById(reservation);

		notificationService.createReservationBorrowSuccessNotification(
			reservation.getUserId(),
			borrowRecord.getBorrowId(),
			book.getBookName(),
			borrowRecord.getDueDate()
		);
		// 当前队首已经成功兑现，如果图书还有额外库存，外层循环会继续分配下一位。
		return true;
	}

	/**
	 * 判断预约用户当前是否仍具备直接借阅资格。
	 *
	 * @param user 用户实体
	 * @param bookId 图书ID
	 * @return 是否可借
	 */
	private boolean canReservationUserReceiveBook(User user, Long bookId) {
		// 用户不存在、主键缺失或账号已禁用时，不能再承接预约到书。
		if (user == null || user.getNameId() == null || !Objects.equals(user.getStatus(), UserStatusEnum.NORMAL.getCode())) {
			return false;
		}
		// 预约兑现会直接生成借阅中记录，因此这里要按“包含审核中记录”的口径校验借阅额度。
		if (countUserActiveBorrowRecords(user.getNameId(), true) >= resolveUserMaxBorrowCount(user)) {
			return false;
		}
		// 同一本书在用户侧只允许存在一条有效借阅链路，避免重复分配导致库存和记录错乱。
		return !hasUserActiveSameBookBorrowRecord(user.getNameId(), bookId, true, null);
	}

	/**
	 * 将无法兑现的预约记录标记为失效。
	 *
	 * @param reservation 预约记录
	 */
	private void markReservationExpired(BookReservation reservation) {
		if (reservation == null || reservation.getReservationId() == null) {
			return; 
		}
		reservation.setStatus(BookReservationStatusEnum.EXPIRED.getCode());
		reservation.setUpdateTime(LocalDateTime.now());
		bookReservationMapper.updateById(reservation);
	}

	/**
	 * 刷新未归还借阅记录的借阅/超期状态。
	 *
	 * @param userId 用户ID
	 */
	private void refreshExpiredBorrowRecords(Long userId) {
		LambdaQueryWrapper<BorrowRecord> queryWrapper = new LambdaQueryWrapper<BorrowRecord>()
			.in(BorrowRecord::getStatus, BorrowRecordStatusEnum.BORROWING.getCode(), BorrowRecordStatusEnum.OVERDUE.getCode());
		if (userId != null && userId > 0) {
			queryWrapper.eq(BorrowRecord::getUserId, userId);
		}

		List<BorrowRecord> activeRecords = borrowRecordMapper.selectList(queryWrapper);
		if (activeRecords == null || activeRecords.isEmpty()) {
			return;
		}

		LocalDateTime now = LocalDateTime.now();
		for (BorrowRecord record : activeRecords) {
			syncBorrowRecordOverdueStatus(record, now);
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
	 * 计算超期罚款金额。
	 *
	 * @param overdueDays 超期天数
	 * @return 罚款金额
	 */
	private BigDecimal calculateFineAmount(int overdueDays) {
		if (overdueDays <= 0) {
			return BigDecimal.ZERO;
		}
		return DAILY_OVERDUE_FINE.multiply(BigDecimal.valueOf(overdueDays));
	}

	/**
	 * 同步单条借阅记录的超期状态与罚款。
	 *
	 * @param borrowRecord 借阅记录
	 * @param currentTime 当前时间
	 */
	private void syncBorrowRecordOverdueStatus(BorrowRecord borrowRecord, LocalDateTime currentTime) {
		if (borrowRecord == null || borrowRecord.getBorrowId() == null || borrowRecord.getDueDate() == null) {
			return;
		}

		int overdueDays = calculateOverdueDays(borrowRecord.getDueDate(), currentTime);
		int currentStatus = borrowRecord.getStatus() == null ? BorrowRecordStatusEnum.BORROWING.getCode() : borrowRecord.getStatus();
		int targetStatus = overdueDays > 0 ? BorrowRecordStatusEnum.OVERDUE.getCode() : BorrowRecordStatusEnum.BORROWING.getCode();
		BigDecimal targetFineAmount = calculateFineAmount(overdueDays);
		int currentOverdueDays = borrowRecord.getOverdueDays() == null ? 0 : borrowRecord.getOverdueDays();
		BigDecimal currentFineAmount = normalizeFineAmount(borrowRecord.getFineAmount());

		// 只有状态或罚款发生变化时才落库，避免高频定时刷新产生无意义更新。
		if (Objects.equals(borrowRecord.getStatus(), targetStatus)
			&& currentOverdueDays == overdueDays
			&& currentFineAmount.compareTo(targetFineAmount) == 0) {
			return;
		}

		borrowRecord.setStatus(targetStatus);
		borrowRecord.setOverdueDays(overdueDays);
		borrowRecord.setFineAmount(targetFineAmount);
		borrowRecord.setUpdateTime(currentTime);
		borrowRecordMapper.updateById(borrowRecord);

		// 仅在借阅记录首次进入超期状态时发送一次提醒，避免重复打扰用户。
		if (!Objects.equals(currentStatus, BorrowRecordStatusEnum.OVERDUE.getCode())
			&& Objects.equals(targetStatus, BorrowRecordStatusEnum.OVERDUE.getCode())) {
			sendBorrowOverdueNotification(borrowRecord, overdueDays, targetFineAmount);
		}
	}

	/**
	 * 发送借阅超期提醒通知。
	 *
	 * @param borrowRecord 借阅记录
	 * @param overdueDays 超期天数
	 * @param fineAmount 罚款金额
	 */
	private void sendBorrowOverdueNotification(BorrowRecord borrowRecord, int overdueDays, BigDecimal fineAmount) {
		if (borrowRecord == null || borrowRecord.getUserId() == null) {
			return;
		}

		Book book = bookMapper.selectById(borrowRecord.getBookId());
		notificationService.createBorrowOverdueNotification(
			borrowRecord.getUserId(),
			borrowRecord.getBorrowId(),
			book == null ? null : book.getBookName(),
			overdueDays,
			fineAmount
		);
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

	/**
	 * 规范化预约查询页码。
	 *
	 * @param queryDTO 查询参数
	 * @return 页码
	 */
	private long normalizeReservationCurrent(ReservationPageQueryDTO queryDTO) {
		if (queryDTO == null || queryDTO.getCurrent() == null || queryDTO.getCurrent() <= 0) {
			return DEFAULT_CURRENT;
		}
		return queryDTO.getCurrent();
	}

	/**
	 * 规范化预约查询每页条数。
	 *
	 * @param queryDTO 查询参数
	 * @return 每页条数
	 */
	private long normalizeReservationSize(ReservationPageQueryDTO queryDTO) {
		if (queryDTO == null || queryDTO.getSize() == null || queryDTO.getSize() <= 0) {
			return DEFAULT_SIZE;
		}
		if (queryDTO.getSize() > MAX_SIZE) {
			return MAX_SIZE;
		}
		return queryDTO.getSize();
	}

	/**
	 * 批量查询预约记录关联的图书信息。
	 *
	 * @param reservations 预约记录列表
	 * @return 图书ID到图书实体的映射
	 */
	private Map<Long, Book> resolveReservationBookMap(List<BookReservation> reservations) {
		if (reservations == null || reservations.isEmpty()) {
			return Map.of();
		}

		List<Long> bookIds = reservations.stream()
			.map(BookReservation::getBookId)
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
		return books.stream().collect(Collectors.toMap(Book::getBookId, (b) -> b, (a, b) -> a));
	}

	/**
	 * 构建单条预约记录的分页返回对象。
	 *
	 * @param reservation 预约记录
	 * @param bookMap 图书映射
	 * @param categoryNameMap 分类名称映射
	 * @return 预约分页VO
	 */
	private ReservationPageVO buildReservationPageVO(BookReservation reservation, Map<Long, Book> bookMap, Map<Long, String> categoryNameMap) {
		Book book = bookMap.getOrDefault(reservation.getBookId(), null);
		ReservationPageVO vo = new ReservationPageVO();
		vo.setReservationId(reservation.getReservationId());
		vo.setBookId(reservation.getBookId());
		vo.setQueuePosition(reservation.getQueueNo());
		vo.setStatus(reservation.getStatus());
		vo.setBorrowId(reservation.getBorrowId());
		vo.setCreateTime(reservation.getCreateTime());
		vo.setUpdateTime(reservation.getUpdateTime());

		if (book != null) {
			vo.setIsbn(book.getIsbn());
			vo.setBookName(book.getBookName());
			vo.setAuthor(book.getAuthor());
			vo.setPublisher(book.getPublisher());
			vo.setPublishDate(book.getPublishDate());
			vo.setCoverUrl(book.getCoverUrl());
			vo.setCategoryName(categoryNameMap.getOrDefault(book.getCategoryId(), null));
		}

		return vo;
	}
}
