package com.example.backend.util;

import com.example.backend.exception.BusinessException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码处理工具类。
 */
public final class PasswordUtils {

	/**
	 * 禁止实例化工具类。
	 */
	private PasswordUtils() {
	}

	/**
	 * 对原始密码做 SHA-256 摘要。
	 *
	 * @param rawPassword 原始密码
	 * @return 摘要后的密码
	 */
	public static String encode(String rawPassword) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] digest = messageDigest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
			StringBuilder stringBuilder = new StringBuilder();

			// 将二进制摘要结果转换成十六进制字符串，便于数据库持久化。
			for (byte value : digest) {
				stringBuilder.append(String.format("%02x", value));
			}
			return stringBuilder.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new BusinessException("密码加密失败，请稍后重试");
		}
	}

	/**
	 * 比较原始密码和密文是否一致。
	 *
	 * @param rawPassword 原始密码
	 * @param encodedPassword 密文密码
	 * @return 是否一致
	 */
	public static boolean matches(String rawPassword, String encodedPassword) {
		return encode(rawPassword).equals(encodedPassword);
	}
}
