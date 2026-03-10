package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用接口响应对象。
 *
 * @param <T> 业务数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

	/**
	 * 成功状态码。
	 */
	private static final int SUCCESS_CODE = 0;

	/**
	 * 失败状态码。
	 */
	private static final int FAIL_CODE = 1;

	/**
	 * 业务状态码。
	 */
	private Integer code;

	/**
	 * 响应消息。
	 */
	private String message;

	/**
	 * 响应数据。
	 */
	private T data;

	/**
	 * 构建成功响应。
	 *
	 * @param message 成功消息
	 * @param data 响应数据
	 * @return 通用响应对象
	 * @param <T> 数据类型
	 */
	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(SUCCESS_CODE, message, data);
	}

	/**
	 * 构建失败响应。
	 *
	 * @param message 失败消息
	 * @return 通用响应对象
	 * @param <T> 数据类型
	 */
	public static <T> ApiResponse<T> fail(String message) {
		return new ApiResponse<>(FAIL_CODE, message, null);
	}
}
