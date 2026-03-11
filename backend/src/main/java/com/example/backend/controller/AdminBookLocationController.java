package com.example.backend.controller;

import com.example.backend.dto.AdminBookLocationPageQueryDTO;
import com.example.backend.dto.AdminBookLocationSaveDTO;
import com.example.backend.service.AdminService;
import com.example.backend.vo.AdminBookLocationVO;
import com.example.backend.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端图书位置接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/book-locations")
public class AdminBookLocationController {

	private final AdminService adminService;

	/**
	 * 管理端分页查询图书位置。
	 *
	 * @param userId 当前管理员ID
	 * @param queryDTO 查询参数
	 * @return 图书位置分页结果
	 */
	@GetMapping("/page")
	public PageResult<AdminBookLocationVO> pageAdminBookLocations(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@ModelAttribute AdminBookLocationPageQueryDTO queryDTO
	) {
		return adminService.pageAdminBookLocations(userId, queryDTO);
	}

	/**
	 * 管理端新增图书位置。
	 *
	 * @param userId 当前管理员ID
	 * @param requestDTO 新增参数
	 * @return 图书位置
	 */
	@PostMapping
	public AdminBookLocationVO createAdminBookLocation(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@RequestBody AdminBookLocationSaveDTO requestDTO
	) {
		return adminService.createAdminBookLocation(userId, requestDTO);
	}

	/**
	 * 管理端修改图书位置。
	 *
	 * @param userId 当前管理员ID
	 * @param locationId 位置ID
	 * @param requestDTO 修改参数
	 * @return 图书位置
	 */
	@PutMapping("/{locationId}")
	public AdminBookLocationVO updateAdminBookLocation(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@PathVariable Long locationId,
		@RequestBody AdminBookLocationSaveDTO requestDTO
	) {
		return adminService.updateAdminBookLocation(userId, locationId, requestDTO);
	}
}

