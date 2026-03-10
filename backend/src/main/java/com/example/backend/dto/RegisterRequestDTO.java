package com.example.backend.dto;

import lombok.Data;

/**
 * 注册请求参数对象。
 */
@Data
public class RegisterRequestDTO {

	/**
	 * 用户名。
	 */
	private String username;

	/**
	 * 真实姓名。
	 */
	private String realName;

	/**
	 * 密码。
	 */
	private String password;

	/**
	 * 手机号。
	 */
	private String phone;

	/**
	 * 身份证号。
	 */
	private String identityCard;

	/**
	 * 邮箱。
	 */
	private String email;

	/**
	 * 用户类型。
	 */
	private Integer userType;
}
