package com.example.backend.controller;

import com.example.backend.dto.AdminCommentPageQueryDTO;
import com.example.backend.dto.AdminCommentStatusUpdateDTO;
import com.example.backend.service.AdminService;
import com.example.backend.vo.AdminCommentPageVO;
import com.example.backend.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端评论接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class AdminCommentController {

	private final AdminService adminService;

	/**
	 * 管理端分页查询评论。
	 *
	 * @param userId 当前管理员ID
	 * @param queryDTO 查询参数
	 * @return 评论分页结果
	 */
	@GetMapping("/page")
	public PageResult<AdminCommentPageVO> pageAdminComments(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@ModelAttribute AdminCommentPageQueryDTO queryDTO
	) {
		return adminService.pageAdminComments(userId, queryDTO);
	}

	/**
	 * 管理端修改评论状态。
	 *
	 * @param userId 当前管理员ID
	 * @param commentId 评论ID
	 * @param requestDTO 修改参数
	 * @return 修改后的评论
	 */
	@PutMapping("/{commentId}/status")
	public AdminCommentPageVO updateAdminCommentStatus(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@PathVariable Long commentId,
		@RequestBody AdminCommentStatusUpdateDTO requestDTO
	) {
		return adminService.updateAdminCommentStatus(userId, commentId, requestDTO);
	}
}
