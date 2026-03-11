package com.example.backend.controller;

import com.example.backend.dto.AdminBorrowRecordPageQueryDTO;
import com.example.backend.service.AdminService;
import com.example.backend.vo.AdminBorrowRecordPageVO;
import com.example.backend.vo.PageResult;
import com.example.backend.vo.ReturnBookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端借阅记录接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/borrow-records")
public class AdminBorrowRecordController {

	private final AdminService adminService;

	/**
	 * 管理端分页查询借阅记录。
	 *
	 * @param userId 当前管理员ID
	 * @param queryDTO 查询参数
	 * @return 借阅记录分页结果
	 */
	@GetMapping("/page")
	public PageResult<AdminBorrowRecordPageVO> pageAdminBorrowRecords(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@ModelAttribute AdminBorrowRecordPageQueryDTO queryDTO
	) {
		return adminService.pageAdminBorrowRecords(userId, queryDTO);
	}

	/**
	 * 管理端归还借阅记录。
	 *
	 * @param userId 当前管理员ID
	 * @param borrowId 借阅记录ID
	 * @return 归还结果
	 */
	@PostMapping("/{borrowId}/return")
	public ReturnBookVO returnAdminBorrowRecord(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@PathVariable Long borrowId
	) {
		return adminService.returnAdminBorrowRecord(userId, borrowId);
	}
}

