package com.example.backend.dto;

import lombok.Data;

/**
 * 通知分页查询参数。
 */
@Data
public class NotificationPageQueryDTO {

	/**
	 * 当前页码。
	 */
	private Long current;

	/**
	 * 每页条数。
	 */
	private Long size;

	/**
	 * 已读状态：0未读，1已读。
	 */
	private Integer readStatus;
}
