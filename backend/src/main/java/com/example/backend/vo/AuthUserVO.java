package com.example.backend.vo;

import lombok.Data;

/**
 * 认证用户返回对象。
 */
@Data
public class AuthUserVO {

	/**
	 * 用户ID。
	 */
	private Long userId;

	/**
	 * 用户名。
	 */
	private String username;

	/**
	 * 真实姓名。
	 */
	private String realName;

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
}
