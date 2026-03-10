package com.example.backend.exception;

/**
 * 业务异常类。
 */
public class BusinessException extends RuntimeException {

	/**
	 * 使用错误信息构造业务异常。
	 *
	 * @param message 错误信息
	 */
	public BusinessException(String message) {
		super(message);
	}
}
