package com.example.backend.enums;

import java.util.Arrays;

/**
 * 通知已读状态枚举。
 */
public enum NotificationReadStatusEnum {

	/**
	 * 未读状态。
	 */
	UNREAD(0, "未读"),

	/**
	 * 已读状态。
	 */
	READ(1, "已读");

	/**
	 * 状态编码。
	 */
	private final int code;

	/**
	 * 状态名称。
	 */
	private final String label;

	/**
	 * 使用编码和名称构造通知已读状态枚举。
	 *
	 * @param code 状态编码
	 * @param label 状态名称
	 */
	NotificationReadStatusEnum(int code, String label) {
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
	 * 根据编码解析通知已读状态枚举。
	 *
	 * @param code 状态编码
	 * @return 通知已读状态枚举
	 */
	public static NotificationReadStatusEnum fromCode(Integer code) {
		if (code == null) {
			return null;
		}

		return Arrays.stream(values())
			.filter((item) -> item.code == code)
			.findFirst()
			.orElse(null);
	}
}
