package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知分页返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPageVO {

	/**
	 * 通知ID。
	 */
	private Long notificationId;

	/**
	 * 通知类型：1超期提醒，2预约借阅成功。
	 */
	private Integer notificationType;

	/**
	 * 通知标题。
	 */
	private String title;

	/**
	 * 通知内容。
	 */
	private String content;

	/**
	 * 业务类型。
	 */
	private String businessType;

	/**
	 * 业务ID。
	 */
	private Long businessId;

	/**
	 * 已读状态：0未读，1已读。
	 */
	private Integer readStatus;

	/**
	 * 阅读时间。
	 */
	private LocalDateTime readTime;

	/**
	 * 创建时间。
	 */
	private LocalDateTime createTime;
}
