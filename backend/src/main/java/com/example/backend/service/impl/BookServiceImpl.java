package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.BookPageQueryDTO;
import com.example.backend.entity.Book;
import com.example.backend.mapper.BookMapper;
import com.example.backend.service.BookService;
import com.example.backend.vo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 图书服务实现类。
 */
@Service
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

	public BookServiceImpl(BookMapper bookMapper) {
		this.bookMapper = bookMapper;
	}

	/**
	 * 分页查询图书信息。
	 *
	 * @param queryDTO 查询参数
	 * @return 图书分页结果
	 */
	@Override
	public PageResult<Book> pageBooks(BookPageQueryDTO queryDTO) {
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
		return new PageResult<>(
			resultPage.getRecords(),
			resultPage.getTotal(),
			resultPage.getCurrent(),
			resultPage.getSize(),
			resultPage.getPages()
		);
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
