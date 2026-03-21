package com.example.backend.enums;

import java.util.Arrays;

/**
 * 评论状态枚举。
 */
public enum CommentStatusEnum {

	/**
	 * 已隐藏状态。
	 */
	HIDDEN(0, "已隐藏"),

	/**
	 * 审核通过状态。
	 */
	VISIBLE(1, "审核通过"),

	/**
	 * 审核中状态。
	 */
	PENDING(2, "审核中");

	/**
	 * 状态编码。
	 */
	private final int code;

	/**
	 * 状态名称。
	 */
	private final String label;

	/**
	 * 使用编码和名称构造评论状态枚举。
	 *
	 * @param code 状态编码
	 * @param label 状态名称
	 */
	CommentStatusEnum(int code, String label) {
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
	 * 根据编码解析评论状态枚举。
	 *
	 * @param code 状态编码
	 * @return 评论状态枚举
	 */
	public static CommentStatusEnum fromCode(Integer code) {
		if (code == null) {
			return null;
		}

		return Arrays.stream(values())
			.filter((item) -> item.code == code)
			.findFirst()
			.orElse(null);
	}
}
