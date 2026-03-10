package com.example.backend.service;

import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.RegisterRequestDTO;
import com.example.backend.vo.AuthUserVO;

/**
 * 认证服务接口。
 */
public interface AuthService {

	/**
	 * 校验用户登录信息。
	 *
	 * @param loginRequestDTO 登录请求参数
	 * @return 登录用户信息
	 */
	AuthUserVO login(LoginRequestDTO loginRequestDTO);

	/**
	 * 完成用户注册。
	 *
	 * @param registerRequestDTO 注册请求参数
	 * @return 注册用户信息
	 */
	AuthUserVO register(RegisterRequestDTO registerRequestDTO);
}
