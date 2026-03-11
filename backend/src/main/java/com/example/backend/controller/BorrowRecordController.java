package com.example.backend.controller;

import com.example.backend.dto.BorrowBookRequestDTO;
import com.example.backend.dto.BorrowRecordPageQueryDTO;
import com.example.backend.service.BorrowService;
import com.example.backend.vo.BorrowRecordPageVO;
import com.example.backend.vo.BorrowResultVO;
import com.example.backend.vo.PageResult;
import com.example.backend.vo.ReturnBookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 借阅记录接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/borrow-records")
public class BorrowRecordController {

	private final BorrowService borrowService;

	/**
	 * 提交借阅申请。
	 *
	 * @param userId 当前用户ID
	 * @param requestDTO 借阅请求参数
	 * @return 借阅结果
	 */
	@PostMapping
	public BorrowResultVO borrowBook(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@RequestBody BorrowBookRequestDTO requestDTO
	) {
		return borrowService.borrowBook(userId, requestDTO);
	}

	/**
	 * 归还图书。
	 *
	 * @param userId 当前用户ID
	 * @param borrowId 借阅记录ID
	 * @return 归还结果
	 */
	@PostMapping("/{borrowId}/return")
	public ReturnBookVO returnBook(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@PathVariable Long borrowId
	) {
		return borrowService.returnBook(userId, borrowId);
	}

	/**
	 * 分页查询我的借阅记录。
	 *
	 * @param userId 当前用户ID
	 * @param queryDTO 查询参数
	 * @return 借阅记录分页结果
	 */
	@GetMapping("/my/page")
	public PageResult<BorrowRecordPageVO> pageMyBorrowRecords(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@ModelAttribute BorrowRecordPageQueryDTO queryDTO
	) {
		return borrowService.pageMyBorrowRecords(userId, queryDTO);
	}
}
