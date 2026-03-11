package com.example.backend.controller;

import com.example.backend.dto.CommentPageQueryDTO;
import com.example.backend.dto.CreateCommentDTO;
import com.example.backend.service.CommentService;
import com.example.backend.vo.CommentPageVO;
import com.example.backend.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

	private final CommentService commentService;

	/**
	 * 对已归还图书发表评论。
	 *
	 * @param userId 当前用户ID
	 * @param requestDTO 评论参数
	 * @return 评论结果
	 */
	@PostMapping
	public CommentPageVO createComment(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@RequestBody CreateCommentDTO requestDTO
	) {
		return commentService.createComment(userId, requestDTO);
	}

	/**
	 * 分页查询我的评论。
	 *
	 * @param userId 当前用户ID
	 * @param queryDTO 查询参数
	 * @return 评论分页结果
	 */
	@GetMapping("/my/page")
	public PageResult<CommentPageVO> pageMyComments(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@ModelAttribute CommentPageQueryDTO queryDTO
	) {
		return commentService.pageMyComments(userId, queryDTO);
	}
}
