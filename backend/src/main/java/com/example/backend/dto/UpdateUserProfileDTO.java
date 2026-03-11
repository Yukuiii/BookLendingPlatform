package com.example.backend.dto;

import lombok.Data;

/**
 * 修改个人信息请求参数。
 */
@Data
public class UpdateUserProfileDTO {

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
}

