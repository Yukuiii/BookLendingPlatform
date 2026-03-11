package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理端用户分页返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserPageVO {

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
	 * 身份证号。
	 */
	private String identityCard;

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

	/**
	 * 创建时间。
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间。
	 */
	private LocalDateTime updateTime;
}

