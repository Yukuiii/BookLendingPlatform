package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.BookPageQueryDTO;
import com.example.backend.entity.Book;
import com.example.backend.entity.BookCategory;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.BookCategoryMapper;
import com.example.backend.mapper.BookMapper;
import com.example.backend.service.BookService;
import com.example.backend.vo.BookDetailVO;
import com.example.backend.vo.BookPageVO;
import com.example.backend.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
	 * 默认每页条数。
	 */
	private static final long DEFAULT_SIZE = 10L;

	/**
	 * 每页最大条数。
	 */
	private static final long MAX_SIZE = 50L;

	private final BookMapper bookMapper;
	private final BookCategoryMapper bookCategoryMapper;

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
		List<BookPageVO> records = books.stream()
			.map((book) -> buildBookPageVO(book, categoryNameMap.get(book.getCategoryId())))
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

		return bookCategoryMapper.selectBatchIds(categoryIds).stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(
				BookCategory::getCategoryId,
				BookCategory::getCategoryName,
				(existing, ignored) -> existing
			));
	}

	/**
	 * 构建图书分页返回对象。
	 *
	 * @param book 图书实体
	 * @param categoryName 分类名称
	 * @return 图书分页返回对象
	 */
	private BookPageVO buildBookPageVO(Book book, String categoryName) {
		BookPageVO bookPageVO = new BookPageVO();
		if (book != null) {
			BeanUtils.copyProperties(book, bookPageVO);
		}
		bookPageVO.setCategoryName(categoryName);
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
		return buildBookDetailVO(book, categoryName);
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
	 * 构建图书详情返回对象。
	 *
	 * @param book 图书实体
	 * @param categoryName 分类名称
	 * @return 图书详情返回对象
	 */
	private BookDetailVO buildBookDetailVO(Book book, String categoryName) {
		BookDetailVO bookDetailVO = new BookDetailVO();
		BeanUtils.copyProperties(book, bookDetailVO);
		bookDetailVO.setCategoryName(categoryName);
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
