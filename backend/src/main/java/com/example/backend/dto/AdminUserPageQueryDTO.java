package com.example.backend.dto;

import lombok.Data;

/**
 * 管理端用户分页查询参数。
 */
@Data
public class AdminUserPageQueryDTO {

	/**
	 * 当前页码。
	 */
	private Long current;

	/**
	 * 每页条数。
	 */
	private Long size;

	/**
	 * 用户名。
	 */
	private String username;

	/**
	 * 真实姓名。
	 */
	private String realName;

	/**
	 * 用户类型。
	 */
	private Integer userType;

	/**
	 * 状态。
	 */
	private Integer status;
}

