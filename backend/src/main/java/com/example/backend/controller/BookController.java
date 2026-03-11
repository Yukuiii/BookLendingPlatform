package com.example.backend.controller;

import com.example.backend.dto.BookPageQueryDTO;
import com.example.backend.service.BookService;
import com.example.backend.vo.BookDetailVO;
import com.example.backend.vo.BookPageVO;
import com.example.backend.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 图书接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

	private final BookService bookService;

	/**
	 * 分页查询图书列表。
	 *
	 * @param queryDTO 查询参数
	 * @return 图书分页结果
	 */
	@GetMapping("/page")
	public PageResult<BookPageVO> pageBooks(@ModelAttribute BookPageQueryDTO queryDTO) {
		return bookService.pageBooks(queryDTO);
	}

	/**
	 * 查询图书详情。
	 *
	 * @param bookId 图书ID
	 * @return 图书详情
	 */
	@GetMapping("/{bookId}")
	public BookDetailVO getBookDetail(@PathVariable Long bookId) {
		return bookService.getBookDetail(bookId);
	}

	/**
	 * 查询当前用户猜你喜欢图书。
	 *
	 * @param userId 当前用户ID
	 * @param limit 返回条数
	 * @return 推荐图书列表
	 */
	@GetMapping("/recommendations")
	public List<BookPageVO> listRecommendBooks(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@RequestParam(value = "limit", required = false) Integer limit
	) {
		return bookService.listRecommendBooks(userId, limit);
	}
}
