package com.example.backend.controller;

import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.RegisterRequestDTO;
import com.example.backend.service.AuthService;
import com.example.backend.vo.ApiResponse;
import com.example.backend.vo.AuthUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	/**
	 * 处理用户登录请求。
	 *
	 * @param loginRequestDTO 登录请求参数
	 * @return 登录用户信息
	 */
	@PostMapping("/login")
	public ApiResponse<AuthUserVO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
		return ApiResponse.success("登录成功", authService.login(loginRequestDTO));
	}

	/**
	 * 处理用户注册请求。
	 *
	 * @param registerRequestDTO 注册请求参数
	 * @return 注册用户信息
	 */
	@PostMapping("/register")
	public ApiResponse<AuthUserVO> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
		return ApiResponse.success("注册成功", authService.register(registerRequestDTO));
	}
}
