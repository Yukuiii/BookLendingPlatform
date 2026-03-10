package com.example.backend.dto;

import lombok.Data;

/**
 * 登录请求参数对象。
 */
@Data
public class LoginRequestDTO {

	/**
	 * 用户名。
	 */
	private String username;

	/**
	 * 密码。
	 */
	private String password;

	/**
	 * 用户类型。
	 */
	private Integer userType;
}
