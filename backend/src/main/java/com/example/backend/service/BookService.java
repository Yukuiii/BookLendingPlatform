package com.example.backend.service;

import com.example.backend.dto.BookPageQueryDTO;
import com.example.backend.entity.Book;
import com.example.backend.vo.PageResult;

/**
 * 图书服务接口。
 */
public interface BookService {

	/**
	 * 分页查询图书信息。
	 *
	 * @param queryDTO 查询参数
	 * @return 图书分页结果
	 */
	PageResult<Book> pageBooks(BookPageQueryDTO queryDTO);
}
