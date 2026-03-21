package com.example.backend.enums;

import java.util.Arrays;

/**
 * 用户状态枚举。
 */
public enum UserStatusEnum {

	/**
	 * 已禁用状态。
	 */
	DISABLED(0, "禁用"),

	/**
	 * 正常状态。
	 */
	NORMAL(1, "正常");

	/**
	 * 状态编码。
	 */
	private final int code;

	/**
	 * 状态名称。
	 */
	private final String label;

	/**
	 * 使用编码和名称构造用户状态枚举。
	 *
	 * @param code 状态编码
	 * @param label 状态名称
	 */
	UserStatusEnum(int code, String label) {
		this.code = code;
		this.label = label;
	}

	/**
	 * 获取状态编码。
	 *
	 * @return 状态编码
	 */
	public int getCode() {
		return code;
	}

	/**
	 * 获取状态名称。
	 *
	 * @return 状态名称
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * 根据编码解析用户状态枚举。
	 *
	 * @param code 状态编码
	 * @return 用户状态枚举
	 */
	public static UserStatusEnum fromCode(Integer code) {
		if (code == null) {
			return null;
		}

		return Arrays.stream(values())
			.filter((item) -> item.code == code)
			.findFirst()
			.orElse(null);
	}
}
