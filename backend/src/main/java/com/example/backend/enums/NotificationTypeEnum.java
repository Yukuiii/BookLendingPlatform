package com.example.backend.enums;

import java.util.Arrays;

/**
 * 通知类型枚举。
 */
public enum NotificationTypeEnum {

	/**
	 * 图书超期提醒通知。
	 */
	BORROW_OVERDUE(1, "超期提醒"),

	/**
	 * 预约兑现后的借阅成功通知。
	 */
	RESERVATION_BORROW_SUCCESS(2, "预约到书");

	/**
	 * 通知类型编码。
	 */
	private final int code;

	/**
	 * 通知类型名称。
	 */
	private final String label;

	/**
	 * 使用编码与名称构造通知类型枚举。
	 *
	 * @param code 通知类型编码
	 * @param label 通知类型名称
	 */
	NotificationTypeEnum(int code, String label) {
		this.code = code;
		this.label = label;
	}

	/**
	 * 获取通知类型编码。
	 *
	 * @return 通知类型编码
	 */
	public int getCode() {
		return code;
	}

	/**
	 * 获取通知类型名称。
	 *
	 * @return 通知类型名称
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * 根据编码解析通知类型枚举。
	 *
	 * @param code 通知类型编码
	 * @return 通知类型枚举
	 */
	public static NotificationTypeEnum fromCode(Integer code) {
		if (code == null) {
			return null;
		}

		return Arrays.stream(values())
			.filter((item) -> item.code == code)
			.findFirst()
			.orElse(null);
	}
}
