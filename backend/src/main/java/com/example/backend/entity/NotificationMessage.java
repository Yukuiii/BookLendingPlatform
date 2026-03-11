package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知消息实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification_message")
public class NotificationMessage {

	/**
	 * 通知ID。
	 */
	@TableId(value = "notification_id", type = IdType.AUTO)
	private Long notificationId;

	/**
	 * 接收通知的用户ID。
	 */
	private Long userId;

	/**
	 * 通知类型：1超期提醒。
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

	/**
	 * 更新时间。
	 */
	private LocalDateTime updateTime;
}
