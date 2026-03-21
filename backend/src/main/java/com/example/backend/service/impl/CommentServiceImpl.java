package com.example.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.CommentPageQueryDTO;
import com.example.backend.dto.CreateCommentDTO;
import com.example.backend.entity.Book;
import com.example.backend.entity.BookCategory;
import com.example.backend.entity.BorrowRecord;
import com.example.backend.entity.Comment;
import com.example.backend.entity.User;
import com.example.backend.enums.BorrowRecordStatusEnum;
import com.example.backend.enums.CommentStatusEnum;
import com.example.backend.enums.UserStatusEnum;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.BookCategoryMapper;
import com.example.backend.mapper.BookMapper;
import com.example.backend.mapper.BorrowRecordMapper;
import com.example.backend.mapper.CommentMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.CommentService;
import com.example.backend.vo.BookCommentVO;
import com.example.backend.vo.CommentPageVO;
import com.example.backend.vo.PageResult;

import lombok.RequiredArgsConstructor;

/**
 * 评论服务实现类。
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

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
	 * 最大评论长度。
	 */
	private static final int MAX_COMMENT_CONTENT_LENGTH = 1000;

	private final CommentMapper commentMapper;
	private final BorrowRecordMapper borrowRecordMapper;
	private final BookMapper bookMapper;
	private final BookCategoryMapper bookCategoryMapper;
	private final UserMapper userMapper;

	/**
	 * 对已归还图书发表评论。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 评论参数
	 * @return 评论结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public CommentPageVO createComment(Long userId, CreateCommentDTO requestDTO) {
		requireAvailableUser(userId);
		validateCreateCommentRequest(requestDTO);

		BorrowRecord borrowRecord = requireReturnedBorrowRecord(userId, requestDTO.getBorrowId());
		Book book = requireBook(borrowRecord.getBookId());

		// 以借阅记录为唯一评论对象，确保每次归还记录最多只产生一条评论。
		Comment existingComment = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
			.eq(Comment::getBorrowId, borrowRecord.getBorrowId())
			.last("limit 1"));
		if (existingComment != null) {
			throw new BusinessException("该借阅记录已评论，请勿重复提交");
		}

		LocalDateTime now = LocalDateTime.now();
		Comment comment = new Comment();
		comment.setUserId(userId);
		comment.setBorrowId(borrowRecord.getBorrowId());
		comment.setBookId(book.getBookId());
		comment.setContent(requestDTO.getContent().trim());
		comment.setRating(requestDTO.getRating());
		comment.setLikeCount(0);
		comment.setStatus(CommentStatusEnum.PENDING.getCode());
		comment.setCreateTime(now);
		comment.setUpdateTime(now);
		commentMapper.insert(comment);

		String categoryName = resolveCategoryName(book.getCategoryId());
		return buildCommentPageVO(comment, book, categoryName);
	}

	/**
	 * 查询图书审核通过的评论列表。
	 *
	 * @param bookId 图书ID
	 * @return 评论列表
	 */
	@Override
	public List<BookCommentVO> listApprovedBookComments(Long bookId) {
		if (bookId == null || bookId <= 0) {
			throw new BusinessException("图书ID不合法");
		}

		requireBook(bookId);
		List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
			.eq(Comment::getBookId, bookId)
			.eq(Comment::getStatus, CommentStatusEnum.VISIBLE.getCode())
			.orderByDesc(Comment::getCreateTime)
			.orderByDesc(Comment::getId));
		if (comments == null || comments.isEmpty()) {
			return List.of();
		}
		Map<Long, User> userMap = resolveUserMap(comments);
		return comments.stream()
			.map((comment) -> buildBookCommentVO(comment, userMap.get(comment.getUserId())))
			.toList();
	}

	/**
	 * 分页查询我的评论。
	 *
	 * @param userId 用户ID
	 * @param queryDTO 查询参数
	 * @return 评论分页结果
	 */
	@Override
	public PageResult<CommentPageVO> pageMyComments(Long userId, CommentPageQueryDTO queryDTO) {
		requireAvailableUser(userId);

		long current = normalizeCurrent(queryDTO);
		long size = normalizeSize(queryDTO);
		Page<Comment> page = new Page<>(current, size);
		LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<Comment>()
			.eq(Comment::getUserId, userId)
			.orderByDesc(Comment::getCreateTime)
			.orderByDesc(Comment::getId);

		Page<Comment> resultPage = commentMapper.selectPage(page, queryWrapper);
		List<Comment> comments = resultPage.getRecords() == null ? List.of() : resultPage.getRecords();

		Map<Long, Book> bookMap = resolveBookMap(comments);
		Map<Long, String> categoryNameMap = resolveCategoryNameMap(bookMap);
		List<CommentPageVO> pageRecords = comments.stream()
			.map((comment) -> buildCommentPageVO(
				comment,
				comment == null ? null : bookMap.get(comment.getBookId()),
				resolveCategoryName(categoryNameMap, comment == null ? null : comment.getBookId(), bookMap)
			))
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
		if (userId == null || userId <= 0) {
			throw new BusinessException("请先登录后再操作评论");
		}

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
	 * 校验评论创建参数。
	 *
	 * @param requestDTO 评论参数
	 */
	private void validateCreateCommentRequest(CreateCommentDTO requestDTO) {
		if (requestDTO == null) {
			throw new BusinessException("评论参数不能为空");
		}
		if (requestDTO.getBorrowId() == null || requestDTO.getBorrowId() <= 0) {
			throw new BusinessException("借阅记录ID不合法");
		}
		if (requestDTO.getRating() == null || requestDTO.getRating() < 1 || requestDTO.getRating() > 5) {
			throw new BusinessException("评分必须在 1 到 5 分之间");
		}
		if (!StringUtils.hasText(requestDTO.getContent())) {
			throw new BusinessException("评论内容不能为空");
		}
		if (requestDTO.getContent().trim().length() > MAX_COMMENT_CONTENT_LENGTH) {
			throw new BusinessException(String.format("评论内容长度不能超过 %d 字", MAX_COMMENT_CONTENT_LENGTH));
		}
	}

	/**
	 * 校验并返回已归还借阅记录。
	 *
	 * @param userId 用户ID
	 * @param borrowId 借阅记录ID
	 * @return 借阅记录
	 */
	private BorrowRecord requireReturnedBorrowRecord(Long userId, Long borrowId) {
		BorrowRecord borrowRecord = borrowRecordMapper.selectById(borrowId);
		if (borrowRecord == null) {
			throw new BusinessException("借阅记录不存在");
		}
		if (!Objects.equals(borrowRecord.getUserId(), userId)) {
			throw new BusinessException("无权评论该借阅记录");
		}
		if (!Objects.equals(borrowRecord.getStatus(), BorrowRecordStatusEnum.RETURNED.getCode())) {
			throw new BusinessException("仅支持对已归还图书发表评论");
		}
		return borrowRecord;
	}

	/**
	 * 校验并返回图书实体。
	 *
	 * @param bookId 图书ID
	 * @return 图书实体
	 */
	private Book requireBook(Long bookId) {
		Book book = bookMapper.selectById(bookId);
		if (book == null) {
			throw new BusinessException("图书不存在");
		}
		return book;
	}

	/**
	 * 批量查询图书映射。
	 *
	 * @param comments 评论列表
	 * @return 图书映射
	 */
	private Map<Long, Book> resolveBookMap(List<Comment> comments) {
		if (comments == null || comments.isEmpty()) {
			return Map.of();
		}

		List<Long> bookIds = comments.stream()
			.map(Comment::getBookId)
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
	 * 批量查询用户映射。
	 *
	 * @param comments 评论列表
	 * @return 用户映射
	 */
	private Map<Long, User> resolveUserMap(List<Comment> comments) {
		if (comments == null || comments.isEmpty()) {
			return Map.of();
		}

		List<Long> userIds = comments.stream()
			.map(Comment::getUserId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		if (userIds.isEmpty()) {
			return Map.of();
		}

		List<User> users = userMapper.selectByIds(userIds);
		if (users == null || users.isEmpty()) {
			return Map.of();
		}

		return users.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(User::getNameId, (user) -> user, (existing, ignored) -> existing));
	}

	/**
	 * 批量查询分类名称映射。
	 *
	 * @param bookMap 图书映射
	 * @return 分类映射
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
	 * 查询单个分类名称。
	 *
	 * @param categoryId 分类ID
	 * @return 分类名称
	 */
	private String resolveCategoryName(Long categoryId) {
		if (categoryId == null) {
			return null;
		}

		BookCategory category = bookCategoryMapper.selectById(categoryId);
		return category == null ? null : category.getCategoryName();
	}

	/**
	 * 从映射中解析分类名称。
	 *
	 * @param categoryNameMap 分类映射
	 * @param bookId 图书ID
	 * @param bookMap 图书映射
	 * @return 分类名称
	 */
	private String resolveCategoryName(Map<Long, String> categoryNameMap, Long bookId, Map<Long, Book> bookMap) {
		if (bookId == null || bookMap == null || categoryNameMap == null) {
			return null;
		}

		Book book = bookMap.get(bookId);
		return book == null ? null : categoryNameMap.get(book.getCategoryId());
	}

	/**
	 * 构建评论分页返回对象。
	 *
	 * @param comment 评论实体
	 * @param book 图书实体
	 * @param categoryName 分类名称
	 * @return 评论分页返回对象
	 */
	private CommentPageVO buildCommentPageVO(Comment comment, Book book, String categoryName) {
		CommentPageVO vo = new CommentPageVO();
		if (comment != null) {
			vo.setCommentId(comment.getId());
			vo.setBorrowId(comment.getBorrowId());
			vo.setBookId(comment.getBookId());
			vo.setContent(comment.getContent());
			vo.setRating(comment.getRating());
			vo.setLikeCount(comment.getLikeCount());
			vo.setStatus(comment.getStatus());
			vo.setCreateTime(comment.getCreateTime());
			vo.setUpdateTime(comment.getUpdateTime());
		}

		if (book != null) {
			vo.setIsbn(book.getIsbn());
			vo.setBookName(book.getBookName());
			vo.setAuthor(book.getAuthor());
			vo.setPublisher(book.getPublisher());
			vo.setCategoryId(book.getCategoryId());
			vo.setCategoryName(categoryName);
			vo.setCoverUrl(book.getCoverUrl());
		}
		return vo;
	}

	/**
	 * 构建图书详情评论返回对象。
	 *
	 * @param comment 评论实体
	 * @param user 用户实体
	 * @return 图书详情评论返回对象
	 */
	private BookCommentVO buildBookCommentVO(Comment comment, User user) {
		BookCommentVO vo = new BookCommentVO();
		if (comment == null) {
			return vo;
		}

		vo.setCommentId(comment.getId());
		vo.setUserId(comment.getUserId());
		vo.setContent(comment.getContent());
		vo.setRating(comment.getRating());
		vo.setLikeCount(comment.getLikeCount());
		vo.setCreateTime(comment.getCreateTime());
		if (user != null) {
			vo.setUsername(user.getUsername());
			vo.setRealName(user.getRealName());
		}
		return vo;
	}

	/**
	 * 规范化当前页码。
	 *
	 * @param queryDTO 查询参数
	 * @return 当前页码
	 */
	private long normalizeCurrent(CommentPageQueryDTO queryDTO) {
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
	private long normalizeSize(CommentPageQueryDTO queryDTO) {
		if (queryDTO == null || queryDTO.getSize() == null || queryDTO.getSize() <= 0) {
			return DEFAULT_SIZE;
		}
		if (queryDTO.getSize() > MAX_SIZE) {
			return MAX_SIZE;
		}
		return queryDTO.getSize();
	}
}
