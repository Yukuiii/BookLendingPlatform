package com.example.backend.controller;

import com.example.backend.service.AdminService;
import com.example.backend.vo.AdminStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端统计接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {

	private final AdminService adminService;

	/**
	 * 获取管理端统计数据。
	 *
	 * @param userId 当前管理员ID
	 * @return 统计数据
	 */
	@GetMapping
	public AdminStatisticsVO getAdminStatistics(
		@RequestHeader(value = "X-User-Id", required = false) Long userId
	) {
		return adminService.getAdminStatistics(userId);
	}
}
