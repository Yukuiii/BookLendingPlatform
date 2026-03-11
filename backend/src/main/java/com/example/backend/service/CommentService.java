package com.example.backend.service;

import com.example.backend.dto.CommentPageQueryDTO;
import com.example.backend.dto.CreateCommentDTO;
import com.example.backend.vo.CommentPageVO;
import com.example.backend.vo.PageResult;

/**
 * 评论服务接口。
 */
public interface CommentService {

	/**
	 * 对已归还图书发表评论。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 评论参数
	 * @return 评论结果
	 */
	CommentPageVO createComment(Long userId, CreateCommentDTO requestDTO);

	/**
	 * 分页查询我的评论。
	 *
	 * @param userId 用户ID
	 * @param queryDTO 查询参数
	 * @return 评论分页结果
	 */
	PageResult<CommentPageVO> pageMyComments(Long userId, CommentPageQueryDTO queryDTO);
}
