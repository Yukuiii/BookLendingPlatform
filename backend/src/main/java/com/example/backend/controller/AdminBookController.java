package com.example.backend.controller;

import com.example.backend.dto.AdminBookSaveDTO;
import com.example.backend.dto.BookPageQueryDTO;
import com.example.backend.entity.BookCategory;
import com.example.backend.service.AdminService;
import com.example.backend.vo.BookDetailVO;
import com.example.backend.vo.BookPageVO;
import com.example.backend.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端图书接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/books")
public class AdminBookController {

	private final AdminService adminService;

	/**
	 * 管理端分页查询图书。
	 *
	 * @param userId 当前管理员ID
	 * @param queryDTO 查询参数
	 * @return 图书分页结果
	 */
	@GetMapping("/page")
	public PageResult<BookPageVO> pageAdminBooks(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@ModelAttribute BookPageQueryDTO queryDTO
	) {
		return adminService.pageAdminBooks(userId, queryDTO);
	}

	/**
	 * 管理端新增图书。
	 *
	 * @param userId 当前管理员ID
	 * @param requestDTO 新增参数
	 * @return 图书详情
	 */
	@PostMapping
	public BookDetailVO createAdminBook(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@RequestBody AdminBookSaveDTO requestDTO
	) {
		return adminService.createAdminBook(userId, requestDTO);
	}

	/**
	 * 管理端修改图书。
	 *
	 * @param userId 当前管理员ID
	 * @param bookId 图书ID
	 * @param requestDTO 修改参数
	 * @return 图书详情
	 */
	@PutMapping("/{bookId}")
	public BookDetailVO updateAdminBook(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@PathVariable Long bookId,
		@RequestBody AdminBookSaveDTO requestDTO
	) {
		return adminService.updateAdminBook(userId, bookId, requestDTO);
	}

	/**
	 * 查询图书分类列表。
	 *
	 * @param userId 当前管理员ID
	 * @return 图书分类列表
	 */
	@GetMapping("/categories")
	public List<BookCategory> listAdminBookCategories(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
		return adminService.listAdminBookCategories(userId);
	}
}

