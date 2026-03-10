package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("`user`")
public class User {

	/**
	 * 用户ID。
	 */
	@TableId(value = "name_id", type = IdType.AUTO)
	private Long nameId;

	/**
	 * 用户名。
	 */
	private String username;

	/**
	 * 密码（加密后存储）。
	 */
	private String password;

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
	 * 身份证号。
	 */
	private String identityCard;

	/**
	 * 专业。
	 */
	private String major;

	/**
	 * 用户类型：1用户，2图书管理员，3系统管理员。
	 */
	private Integer userType;

	/**
	 * 最大借阅数量。
	 */
	private Integer maxBorrowCount;

	/**
	 * 状态：0禁用，1正常。
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
