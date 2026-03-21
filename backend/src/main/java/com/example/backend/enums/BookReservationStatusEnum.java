package com.example.backend.enums;

import java.util.Arrays;

/**
 * 图书预约状态枚举。
 */
public enum BookReservationStatusEnum {

	/**
	 * 排队中状态。
	 */
	WAITING(1, "排队中"),

	/**
	 * 已兑现状态。
	 */
	FULFILLED(2, "已完成"),

	/**
	 * 已失效状态。
	 */
	EXPIRED(3, "已失效");

	/**
	 * 状态编码。
	 */
	private final int code;

	/**
	 * 状态名称。
	 */
	private final String label;

	/**
	 * 使用编码和名称构造预约状态枚举。
	 *
	 * @param code 状态编码
	 * @param label 状态名称
	 */
	BookReservationStatusEnum(int code, String label) {
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
	 * 根据编码解析预约状态枚举。
	 *
	 * @param code 状态编码
	 * @return 预约状态枚举
	 */
	public static BookReservationStatusEnum fromCode(Integer code) {
		if (code == null) {
			return null;
		}

		return Arrays.stream(values())
			.filter((item) -> item.code == code)
			.findFirst()
			.orElse(null);
	}
}
