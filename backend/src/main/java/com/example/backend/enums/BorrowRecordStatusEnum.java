package com.example.backend.enums;

import java.util.Arrays;

/**
 * 借阅记录状态枚举。
 */
public enum BorrowRecordStatusEnum {

	/**
	 * 借阅中状态。
	 */
	BORROWING(1, "借阅中"),

	/**
	 * 已归还状态。
	 */
	RETURNED(2, "已归还"),

	/**
	 * 超期状态。
	 */
	OVERDUE(3, "超期"),

	/**
	 * 审核中状态。
	 */
	PENDING_REVIEW(4, "审核中");

	/**
	 * 状态编码。
	 */
	private final int code;

	/**
	 * 状态名称。
	 */
	private final String label;

	/**
	 * 使用编码和名称构造借阅状态枚举。
	 *
	 * @param code 状态编码
	 * @param label 状态名称
	 */
	BorrowRecordStatusEnum(int code, String label) {
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
	 * 根据编码解析借阅状态枚举。
	 *
	 * @param code 状态编码
	 * @return 借阅状态枚举
	 */
	public static BorrowRecordStatusEnum fromCode(Integer code) {
		if (code == null) {
			return null;
		}

		return Arrays.stream(values())
			.filter((item) -> item.code == code)
			.findFirst()
			.orElse(null);
	}
}
