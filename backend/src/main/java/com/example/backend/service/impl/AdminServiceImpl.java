package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.AdminBookLocationPageQueryDTO;
import com.example.backend.dto.AdminBookLocationSaveDTO;
import com.example.backend.dto.AdminBookSaveDTO;
import com.example.backend.dto.AdminBorrowRecordPageQueryDTO;
import com.example.backend.dto.BookPageQueryDTO;
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
import com.example.backend.service.AdminService;
import com.example.backend.service.BookService;
import com.example.backend.service.BorrowService;
import com.example.backend.vo.AdminBookLocationVO;
import com.example.backend.vo.AdminBorrowRecordPageVO;
import com.example.backend.vo.BookDetailVO;
import com.example.backend.vo.BookPageVO;
import com.example.backend.vo.PageResult;
import com.example.backend.vo.ReturnBookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理端服务实现类。
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

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
	 * 可用管理员角色。
	 */
	private static final Set<Integer> ADMIN_USER_TYPES = Set.of(2, 3);

	private final UserMapper userMapper;
	private final BookMapper bookMapper;
	private final BookCategoryMapper bookCategoryMapper;
	private final BookLocationMapper bookLocationMapper;
	private final BorrowRecordMapper borrowRecordMapper;
	private final BookService bookService;
	private final BorrowService borrowService;

	/**
	 * 管理端分页查询图书。
	 *
	 * @param adminUserId 管理员ID
	 * @param queryDTO 查询参数
	 * @return 图书分页结果
	 */
	@Override
	public PageResult<BookPageVO> pageAdminBooks(Long adminUserId, BookPageQueryDTO queryDTO) {
		requireAdminUser(adminUserId);
		return bookService.pageBooks(queryDTO);
	}

	/**
	 * 管理端新增图书。
	 *
	 * @param adminUserId 管理员ID
	 * @param requestDTO 新增参数
	 * @return 图书详情
	 */
	@Override
	public BookDetailVO createAdminBook(Long adminUserId, AdminBookSaveDTO requestDTO) {
		requireAdminUser(adminUserId);
		validateBookRequest(requestDTO);

		Book existingBook = bookMapper.selectOne(new LambdaQueryWrapper<Book>()
			.eq(Book::getIsbn, normalizeText(requestDTO.getIsbn()))
			.last("limit 1"));
		if (existingBook != null) {
			throw new BusinessException("ISBN 已存在");
		}

		Book book = new Book();
		applyBookRequest(book, requestDTO);
		book.setCreateTime(LocalDateTime.now());
		book.setUpdateTime(LocalDateTime.now());
		bookMapper.insert(book);
		return bookService.getBookDetail(book.getBookId());
	}

	/**
	 * 管理端修改图书。
	 *
	 * @param adminUserId 管理员ID
	 * @param bookId 图书ID
	 * @param requestDTO 修改参数
	 * @return 图书详情
	 */
	@Override
	public BookDetailVO updateAdminBook(Long adminUserId, Long bookId, AdminBookSaveDTO requestDTO) {
		requireAdminUser(adminUserId);
		validateBookRequest(requestDTO);

		Book book = requireBook(bookId);
		Book existingBook = bookMapper.selectOne(new LambdaQueryWrapper<Book>()
			.eq(Book::getIsbn, normalizeText(requestDTO.getIsbn()))
			.ne(Book::getBookId, bookId)
			.last("limit 1"));
		if (existingBook != null) {
			throw new BusinessException("ISBN 已存在");
		}

		applyBookRequest(book, requestDTO);
		bookMapper.updateById(book);
		return bookService.getBookDetail(bookId);
	}

	/**
	 * 查询可用图书分类。
	 *
	 * @param adminUserId 管理员ID
	 * @return 图书分类列表
	 */
	@Override
	public List<BookCategory> listAdminBookCategories(Long adminUserId) {
		requireAdminUser(adminUserId);
		return bookCategoryMapper.selectList(new LambdaQueryWrapper<BookCategory>()
			.eq(BookCategory::getStatus, NORMAL_STATUS)
			.orderByAsc(BookCategory::getSortOrder)
			.orderByAsc(BookCategory::getCategoryId));
	}

	/**
	 * 分页查询图书位置。
	 *
	 * @param adminUserId 管理员ID
	 * @param queryDTO 查询参数
	 * @return 图书位置分页结果
	 */
	@Override
	public PageResult<AdminBookLocationVO> pageAdminBookLocations(Long adminUserId, AdminBookLocationPageQueryDTO queryDTO) {
		requireAdminUser(adminUserId);

		Page<BookLocation> page = new Page<>(normalizeCurrent(queryDTO), normalizeSize(queryDTO));
		LambdaQueryWrapper<BookLocation> queryWrapper = new LambdaQueryWrapper<>();
		Map<Long, Book> filteredBookMap = resolveFilteredBookMap(queryDTO == null ? null : queryDTO.getBookName(), queryDTO == null ? null : queryDTO.getIsbn());
		if (filteredBookMap != null) {
			if (filteredBookMap.isEmpty()) {
				return new PageResult<>(List.of(), 0L, page.getCurrent(), page.getSize(), 0L);
			}
			queryWrapper.in(BookLocation::getBookId, filteredBookMap.keySet());
		}

		queryWrapper.orderByDesc(BookLocation::getUpdateTime)
			.orderByDesc(BookLocation::getLocationId);
		Page<BookLocation> resultPage = bookLocationMapper.selectPage(page, queryWrapper);
		List<BookLocation> locations = resultPage.getRecords() == null ? List.of() : resultPage.getRecords();
		Map<Long, Book> bookMap = filteredBookMap == null ? resolveBookMapByIds(locations.stream().map(BookLocation::getBookId).toList()) : filteredBookMap;

		List<AdminBookLocationVO> records = locations.stream()
			.map((location) -> buildAdminBookLocationVO(location, bookMap.get(location.getBookId())))
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
	 * 新增图书位置。
	 *
	 * @param adminUserId 管理员ID
	 * @param requestDTO 新增参数
	 * @return 图书位置
	 */
	@Override
	public AdminBookLocationVO createAdminBookLocation(Long adminUserId, AdminBookLocationSaveDTO requestDTO) {
		requireAdminUser(adminUserId);
		validateBookLocationRequest(requestDTO);

		Book book = requireBook(requestDTO.getBookId());
		ensureRfidAvailable(requestDTO.getRfidCode(), null);

		BookLocation bookLocation = new BookLocation();
		BeanUtils.copyProperties(requestDTO, bookLocation);
		bookLocation.setArea(normalizeText(requestDTO.getArea()));
		bookLocation.setShelfNo(normalizeText(requestDTO.getShelfNo()));
		bookLocation.setRfidCode(normalizeText(requestDTO.getRfidCode()));
		bookLocation.setCreateTime(LocalDateTime.now());
		bookLocation.setUpdateTime(LocalDateTime.now());
		bookLocationMapper.insert(bookLocation);
		return buildAdminBookLocationVO(bookLocation, book);
	}

	/**
	 * 修改图书位置。
	 *
	 * @param adminUserId 管理员ID
	 * @param locationId 位置ID
	 * @param requestDTO 修改参数
	 * @return 图书位置
	 */
	@Override
	public AdminBookLocationVO updateAdminBookLocation(Long adminUserId, Long locationId, AdminBookLocationSaveDTO requestDTO) {
		requireAdminUser(adminUserId);
		validateBookLocationRequest(requestDTO);

		BookLocation bookLocation = requireBookLocation(locationId);
		Book book = requireBook(requestDTO.getBookId());
		ensureRfidAvailable(requestDTO.getRfidCode(), locationId);

		bookLocation.setBookId(requestDTO.getBookId());
		bookLocation.setFloor(requestDTO.getFloor());
		bookLocation.setArea(normalizeText(requestDTO.getArea()));
		bookLocation.setShelfNo(normalizeText(requestDTO.getShelfNo()));
		bookLocation.setLayer(requestDTO.getLayer());
		bookLocation.setRfidCode(normalizeText(requestDTO.getRfidCode()));
		bookLocation.setUpdateTime(LocalDateTime.now());
		bookLocationMapper.updateById(bookLocation);
		return buildAdminBookLocationVO(bookLocation, book);
	}

	/**
	 * 分页查询借阅记录。
	 *
	 * @param adminUserId 管理员ID
	 * @param queryDTO 查询参数
	 * @return 借阅记录分页结果
	 */
	@Override
	public PageResult<AdminBorrowRecordPageVO> pageAdminBorrowRecords(Long adminUserId, AdminBorrowRecordPageQueryDTO queryDTO) {
		requireAdminUser(adminUserId);

		Page<BorrowRecord> page = new Page<>(normalizeCurrent(queryDTO), normalizeSize(queryDTO));
		LambdaQueryWrapper<BorrowRecord> queryWrapper = new LambdaQueryWrapper<>();

		Map<Long, User> userMap = resolveFilteredUserMap(queryDTO == null ? null : queryDTO.getUsername());
		if (userMap != null) {
			if (userMap.isEmpty()) {
				return new PageResult<>(List.of(), 0L, page.getCurrent(), page.getSize(), 0L);
			}
			queryWrapper.in(BorrowRecord::getUserId, userMap.keySet());
		}

		Map<Long, Book> filteredBookMap = resolveFilteredBookMap(queryDTO == null ? null : queryDTO.getBookName(), null);
		if (filteredBookMap != null) {
			if (filteredBookMap.isEmpty()) {
				return new PageResult<>(List.of(), 0L, page.getCurrent(), page.getSize(), 0L);
			}
			queryWrapper.in(BorrowRecord::getBookId, filteredBookMap.keySet());
		}

		if (queryDTO != null && queryDTO.getStatus() != null) {
			queryWrapper.eq(BorrowRecord::getStatus, queryDTO.getStatus());
		}
		queryWrapper.orderByDesc(BorrowRecord::getBorrowDate)
			.orderByDesc(BorrowRecord::getBorrowId);

		Page<BorrowRecord> resultPage = borrowRecordMapper.selectPage(page, queryWrapper);
		List<BorrowRecord> records = resultPage.getRecords() == null ? List.of() : resultPage.getRecords();
		Map<Long, User> finalUserMap = userMap == null ? resolveUserMapByIds(records.stream().map(BorrowRecord::getUserId).toList()) : userMap;
		Map<Long, Book> finalBookMap = filteredBookMap == null ? resolveBookMapByIds(records.stream().map(BorrowRecord::getBookId).toList()) : filteredBookMap;

		List<AdminBorrowRecordPageVO> pageRecords = records.stream()
			.map((record) -> buildAdminBorrowRecordPageVO(record, finalUserMap.get(record.getUserId()), finalBookMap.get(record.getBookId())))
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
	 * 管理端归还图书。
	 *
	 * @param adminUserId 管理员ID
	 * @param borrowId 借阅记录ID
	 * @return 归还结果
	 */
	@Override
	public ReturnBookVO returnAdminBorrowRecord(Long adminUserId, Long borrowId) {
		requireAdminUser(adminUserId);
		return borrowService.returnAdminBorrowRecord(adminUserId, borrowId);
	}

	/**
	 * 校验管理员身份。
	 *
	 * @param adminUserId 管理员ID
	 * @return 管理员用户
	 */
	private User requireAdminUser(Long adminUserId) {
		if (adminUserId == null || adminUserId <= 0) {
			throw new BusinessException("管理员ID不合法");
		}
		User adminUser = userMapper.selectById(adminUserId);
		if (adminUser == null) {
			throw new BusinessException("管理员不存在");
		}
		if (!Objects.equals(adminUser.getStatus(), NORMAL_STATUS) || !ADMIN_USER_TYPES.contains(adminUser.getUserType())) {
			throw new BusinessException("当前用户无管理权限");
		}
		return adminUser;
	}

	/**
	 * 校验图书保存参数。
	 *
	 * @param requestDTO 保存参数
	 */
	private void validateBookRequest(AdminBookSaveDTO requestDTO) {
		if (requestDTO == null) {
			throw new BusinessException("图书参数不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getIsbn())) {
			throw new BusinessException("ISBN 不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getBookName())) {
			throw new BusinessException("书名不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getAuthor())) {
			throw new BusinessException("作者不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getPublisher())) {
			throw new BusinessException("出版社不能为空");
		}
		if (requestDTO.getCategoryId() == null || requestDTO.getCategoryId() <= 0) {
			throw new BusinessException("分类不能为空");
		}
		if (requestDTO.getDifficultyLevel() == null || requestDTO.getDifficultyLevel() < 1 || requestDTO.getDifficultyLevel() > 3) {
			throw new BusinessException("难度等级不合法");
		}
		if (requestDTO.getStatus() == null || !(requestDTO.getStatus() == 0 || requestDTO.getStatus() == 1)) {
			throw new BusinessException("图书状态不合法");
		}
		if (requestDTO.getTotalCount() == null || requestDTO.getTotalCount() < 0) {
			throw new BusinessException("馆藏总数不能小于 0");
		}
		if (requestDTO.getAvailableCount() == null || requestDTO.getAvailableCount() < 0) {
			throw new BusinessException("可借数量不能小于 0");
		}
		if (requestDTO.getAvailableCount() > requestDTO.getTotalCount()) {
			throw new BusinessException("可借数量不能大于馆藏总数");
		}
		if (bookCategoryMapper.selectById(requestDTO.getCategoryId()) == null) {
			throw new BusinessException("图书分类不存在");
		}
	}

	/**
	 * 将图书请求参数写入图书实体。
	 *
	 * @param book 图书实体
	 * @param requestDTO 请求参数
	 */
	private void applyBookRequest(Book book, AdminBookSaveDTO requestDTO) {
		book.setIsbn(normalizeText(requestDTO.getIsbn()));
		book.setBookName(normalizeText(requestDTO.getBookName()));
		book.setAuthor(normalizeText(requestDTO.getAuthor()));
		book.setPublisher(normalizeText(requestDTO.getPublisher()));
		book.setPublishDate(requestDTO.getPublishDate());
		book.setCategoryId(requestDTO.getCategoryId());
		book.setSubField(normalizeText(requestDTO.getSubField()));
		book.setDifficultyLevel(requestDTO.getDifficultyLevel());
		book.setSuitableScene(normalizeText(requestDTO.getSuitableScene()));
		book.setCoverUrl(normalizeText(requestDTO.getCoverUrl()));
		book.setDescription(normalizeText(requestDTO.getDescription()));
		book.setCatalog(normalizeText(requestDTO.getCatalog()));
		book.setAuthorIntro(normalizeText(requestDTO.getAuthorIntro()));
		book.setTargetAudience(normalizeText(requestDTO.getTargetAudience()));
		book.setTotalCount(requestDTO.getTotalCount());
		book.setAvailableCount(requestDTO.getAvailableCount());
		book.setStatus(requestDTO.getStatus());
		book.setUpdateTime(LocalDateTime.now());
	}

	/**
	 * 校验图书位置参数。
	 *
	 * @param requestDTO 图书位置参数
	 */
	private void validateBookLocationRequest(AdminBookLocationSaveDTO requestDTO) {
		if (requestDTO == null) {
			throw new BusinessException("图书位置参数不能为空");
		}
		if (requestDTO.getBookId() == null || requestDTO.getBookId() <= 0) {
			throw new BusinessException("图书ID不合法");
		}
		if (requestDTO.getFloor() == null || requestDTO.getFloor() <= 0) {
			throw new BusinessException("楼层必须大于 0");
		}
		if (!StringUtils.hasText(requestDTO.getArea())) {
			throw new BusinessException("区域不能为空");
		}
		if (!StringUtils.hasText(requestDTO.getShelfNo())) {
			throw new BusinessException("书架号不能为空");
		}
		if (requestDTO.getLayer() == null || requestDTO.getLayer() <= 0) {
			throw new BusinessException("层数必须大于 0");
		}
	}

	/**
	 * 校验 RFID 唯一性。
	 *
	 * @param rfidCode RFID编码
	 * @param excludeLocationId 排除位置ID
	 */
	private void ensureRfidAvailable(String rfidCode, Long excludeLocationId) {
		if (!StringUtils.hasText(rfidCode)) {
			return;
		}
		BookLocation existingLocation = bookLocationMapper.selectOne(new LambdaQueryWrapper<BookLocation>()
			.eq(BookLocation::getRfidCode, normalizeText(rfidCode))
			.ne(excludeLocationId != null, BookLocation::getLocationId, excludeLocationId)
			.last("limit 1"));
		if (existingLocation != null) {
			throw new BusinessException("RFID 编码已存在");
		}
	}

	/**
	 * 查询图书。
	 *
	 * @param bookId 图书ID
	 * @return 图书实体
	 */
	private Book requireBook(Long bookId) {
		if (bookId == null || bookId <= 0) {
			throw new BusinessException("图书ID不合法");
		}
		Book book = bookMapper.selectById(bookId);
		if (book == null) {
			throw new BusinessException("图书不存在");
		}
		return book;
	}

	/**
	 * 查询图书位置。
	 *
	 * @param locationId 位置ID
	 * @return 图书位置实体
	 */
	private BookLocation requireBookLocation(Long locationId) {
		if (locationId == null || locationId <= 0) {
			throw new BusinessException("位置ID不合法");
		}
		BookLocation bookLocation = bookLocationMapper.selectById(locationId);
		if (bookLocation == null) {
			throw new BusinessException("图书位置不存在");
		}
		return bookLocation;
	}

	/**
	 * 按条件查询图书映射。
	 *
	 * @param bookName 书名
	 * @param isbn ISBN号
	 * @return 图书映射
	 */
	private Map<Long, Book> resolveFilteredBookMap(String bookName, String isbn) {
		boolean hasBookName = StringUtils.hasText(bookName);
		boolean hasIsbn = StringUtils.hasText(isbn);
		if (!hasBookName && !hasIsbn) {
			return null;
		}

		List<Book> books = bookMapper.selectList(new LambdaQueryWrapper<Book>()
			.like(hasBookName, Book::getBookName, normalizeText(bookName))
			.like(hasIsbn, Book::getIsbn, normalizeText(isbn)));
		if (books == null || books.isEmpty()) {
			return Map.of();
		}
		return books.stream().collect(Collectors.toMap(Book::getBookId, (book) -> book, (existing, ignored) -> existing));
	}

	/**
	 * 批量查询图书映射。
	 *
	 * @param bookIds 图书ID列表
	 * @return 图书映射
	 */
	private Map<Long, Book> resolveBookMapByIds(List<Long> bookIds) {
		if (bookIds == null || bookIds.isEmpty()) {
			return Map.of();
		}
		return bookMapper.selectBatchIds(bookIds.stream().filter(Objects::nonNull).distinct().toList()).stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(Book::getBookId, (book) -> book, (existing, ignored) -> existing));
	}

	/**
	 * 按条件查询用户映射。
	 *
	 * @param username 用户名
	 * @return 用户映射
	 */
	private Map<Long, User> resolveFilteredUserMap(String username) {
		if (!StringUtils.hasText(username)) {
			return null;
		}

		List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
			.like(User::getUsername, normalizeText(username)));
		if (users == null || users.isEmpty()) {
			return Map.of();
		}
		return users.stream().collect(Collectors.toMap(User::getNameId, (user) -> user, (existing, ignored) -> existing));
	}

	/**
	 * 批量查询用户映射。
	 *
	 * @param userIds 用户ID列表
	 * @return 用户映射
	 */
	private Map<Long, User> resolveUserMapByIds(List<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return Map.of();
		}
		return userMapper.selectBatchIds(userIds.stream().filter(Objects::nonNull).distinct().toList()).stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(User::getNameId, (user) -> user, (existing, ignored) -> existing));
	}

	/**
	 * 构建图书位置返回对象。
	 *
	 * @param location 图书位置
	 * @param book 图书实体
	 * @return 图书位置返回对象
	 */
	private AdminBookLocationVO buildAdminBookLocationVO(BookLocation location, Book book) {
		AdminBookLocationVO adminBookLocationVO = new AdminBookLocationVO();
		if (location == null) {
			return adminBookLocationVO;
		}
		adminBookLocationVO.setLocationId(location.getLocationId());
		adminBookLocationVO.setBookId(location.getBookId());
		adminBookLocationVO.setFloor(location.getFloor());
		adminBookLocationVO.setArea(location.getArea());
		adminBookLocationVO.setShelfNo(location.getShelfNo());
		adminBookLocationVO.setLayer(location.getLayer());
		adminBookLocationVO.setRfidCode(location.getRfidCode());
		adminBookLocationVO.setUpdateTime(location.getUpdateTime());
		if (book != null) {
			adminBookLocationVO.setBookName(book.getBookName());
			adminBookLocationVO.setIsbn(book.getIsbn());
		}
		return adminBookLocationVO;
	}

	/**
	 * 构建借阅记录返回对象。
	 *
	 * @param borrowRecord 借阅记录
	 * @param user 用户
	 * @param book 图书
	 * @return 借阅记录返回对象
	 */
	private AdminBorrowRecordPageVO buildAdminBorrowRecordPageVO(BorrowRecord borrowRecord, User user, Book book) {
		AdminBorrowRecordPageVO adminBorrowRecordPageVO = new AdminBorrowRecordPageVO();
		if (borrowRecord == null) {
			return adminBorrowRecordPageVO;
		}
		adminBorrowRecordPageVO.setBorrowId(borrowRecord.getBorrowId());
		adminBorrowRecordPageVO.setUserId(borrowRecord.getUserId());
		adminBorrowRecordPageVO.setBookId(borrowRecord.getBookId());
		adminBorrowRecordPageVO.setBorrowDate(borrowRecord.getBorrowDate());
		adminBorrowRecordPageVO.setDueDate(borrowRecord.getDueDate());
		adminBorrowRecordPageVO.setReturnDate(borrowRecord.getReturnDate());
		adminBorrowRecordPageVO.setStatus(borrowRecord.getStatus());
		adminBorrowRecordPageVO.setOverdueDays(borrowRecord.getOverdueDays());
		adminBorrowRecordPageVO.setFineAmount(borrowRecord.getFineAmount());
		if (user != null) {
			adminBorrowRecordPageVO.setUsername(user.getUsername());
			adminBorrowRecordPageVO.setRealName(user.getRealName());
		}
		if (book != null) {
			adminBorrowRecordPageVO.setBookName(book.getBookName());
			adminBorrowRecordPageVO.setIsbn(book.getIsbn());
		}
		return adminBorrowRecordPageVO;
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
	 * 规范化当前位置页码。
	 *
	 * @param queryDTO 查询参数
	 * @return 当前页码
	 */
	private long normalizeCurrent(Object queryDTO) {
		if (queryDTO instanceof AdminBookLocationPageQueryDTO locationQueryDTO) {
			if (locationQueryDTO.getCurrent() == null || locationQueryDTO.getCurrent() <= 0) {
				return DEFAULT_CURRENT;
			}
			return locationQueryDTO.getCurrent();
		}
		if (queryDTO instanceof AdminBorrowRecordPageQueryDTO borrowRecordPageQueryDTO) {
			if (borrowRecordPageQueryDTO.getCurrent() == null || borrowRecordPageQueryDTO.getCurrent() <= 0) {
				return DEFAULT_CURRENT;
			}
			return borrowRecordPageQueryDTO.getCurrent();
		}
		return DEFAULT_CURRENT;
	}

	/**
	 * 规范化每页条数。
	 *
	 * @param queryDTO 查询参数
	 * @return 每页条数
	 */
	private long normalizeSize(Object queryDTO) {
		Long size = null;
		if (queryDTO instanceof AdminBookLocationPageQueryDTO locationQueryDTO) {
			size = locationQueryDTO.getSize();
		}
		if (queryDTO instanceof AdminBorrowRecordPageQueryDTO borrowRecordPageQueryDTO) {
			size = borrowRecordPageQueryDTO.getSize();
		}
		if (size == null || size <= 0) {
			return DEFAULT_SIZE;
		}
		if (size > MAX_SIZE) {
			return MAX_SIZE;
		}
		return size;
	}
}

