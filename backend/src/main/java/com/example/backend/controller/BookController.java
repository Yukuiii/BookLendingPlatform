package com.example.backend.controller;

import com.example.backend.dto.BookPageQueryDTO;
import com.example.backend.entity.Book;
import com.example.backend.service.BookService;
import com.example.backend.vo.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图书接口控制器。
 */
@RestController
@RequestMapping("/books")
public class BookController {

	private final BookService bookService;

	/**
	 * 通过构造器注入图书服务。
	 *
	 * @param bookService 图书服务
	 */
	public BookController(BookService bookService) {
		this.bookService = bookService;
	}

	/**
	 * 分页查询图书列表。
	 *
	 * @param queryDTO 查询参数
	 * @return 图书分页结果
	 */
	@GetMapping("/page")
	public PageResult<Book> pageBooks(@ModelAttribute BookPageQueryDTO queryDTO) {
		return bookService.pageBooks(queryDTO);
	}
}
