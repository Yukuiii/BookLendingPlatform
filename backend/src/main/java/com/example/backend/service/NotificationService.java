package com.example.backend.service;

import com.example.backend.dto.NotificationPageQueryDTO;
import com.example.backend.vo.NotificationPageVO;
import com.example.backend.vo.PageResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 通知服务接口。
 */
public interface NotificationService {

	/**
	 * 发送借阅超期提醒通知。
	 *
	 * @param userId 用户ID
	 * @param borrowId 借阅记录ID
	 * @param bookName 图书名称
	 * @param overdueDays 超期天数
	 * @param fineAmount 罚款金额
	 */
	void createBorrowOverdueNotification(Long userId, Long borrowId, String bookName, Integer overdueDays, BigDecimal fineAmount);

	/**
	 * 发送预约兑现后的借阅成功通知。
	 *
	 * @param userId 用户ID
	 * @param borrowId 借阅记录ID
	 * @param bookName 图书名称
	 * @param dueDate 应还时间
	 */
	void createReservationBorrowSuccessNotification(Long userId, Long borrowId, String bookName, LocalDateTime dueDate);

	/**
	 * 分页查询我的通知。
	 *
	 * @param userId 用户ID
	 * @param queryDTO 查询参数
	 * @return 通知分页结果
	 */
	PageResult<NotificationPageVO> pageMyNotifications(Long userId, NotificationPageQueryDTO queryDTO);

	/**
	 * 标记我的通知为已读。
	 *
	 * @param userId 用户ID
	 * @param notificationId 通知ID
	 * @return 更新后的通知
	 */
	NotificationPageVO markMyNotificationRead(Long userId, Long notificationId);
}
