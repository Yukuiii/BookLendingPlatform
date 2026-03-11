package com.example.backend.dto;

import lombok.Data;

/**
 * 修改密码请求参数。
 */
@Data
public class ChangePasswordDTO {

	/**
	 * 原密码。
	 */
	private String oldPassword;

	/**
	 * 新密码。
	 */
	private String newPassword;
}

