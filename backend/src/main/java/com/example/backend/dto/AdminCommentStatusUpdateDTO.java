package com.example.backend.dto;

import lombok.Data;

/**
 * 管理端评论状态修改参数。
 */
@Data
public class AdminCommentStatusUpdateDTO {

	/**
	 * 评论状态：0隐藏，1审核通过，2审核中。
	 */
	private Integer status;
}
