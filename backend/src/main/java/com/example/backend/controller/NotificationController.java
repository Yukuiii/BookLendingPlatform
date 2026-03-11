package com.example.backend.controller;

import com.example.backend.dto.NotificationPageQueryDTO;
import com.example.backend.service.NotificationService;
import com.example.backend.vo.NotificationPageVO;
import com.example.backend.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	/**
	 * 分页查询我的通知。
	 *
	 * @param userId 当前用户ID
	 * @param queryDTO 查询参数
	 * @return 通知分页结果
	 */
	@GetMapping("/my/page")
	public PageResult<NotificationPageVO> pageMyNotifications(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@ModelAttribute NotificationPageQueryDTO queryDTO
	) {
		return notificationService.pageMyNotifications(userId, queryDTO);
	}

	/**
	 * 标记通知为已读。
	 *
	 * @param userId 当前用户ID
	 * @param notificationId 通知ID
	 * @return 更新后的通知
	 */
	@PostMapping("/{notificationId}/read")
	public NotificationPageVO markMyNotificationRead(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@PathVariable Long notificationId
	) {
		return notificationService.markMyNotificationRead(userId, notificationId);
	}
}
