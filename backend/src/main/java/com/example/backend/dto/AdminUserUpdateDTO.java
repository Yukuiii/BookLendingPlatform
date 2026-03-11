package com.example.backend.dto;

import lombok.Data;

/**
 * 管理端修改用户请求参数。
 */
@Data
public class AdminUserUpdateDTO {

	/**
	 * 真实姓名。
	 */
	private String realName;

	/**
	 * 专业。
	 */
	private String major;

	/**
	 * 邮箱。
	 */
	private String email;

	/**
	 * 手机号。
	 */
	private String phone;

	/**
	 * 用户类型。
	 */
	private Integer userType;

	/**
	 * 最大借阅数。
	 */
	private Integer maxBorrowCount;

	/**
	 * 状态。
	 */
	private Integer status;
}

