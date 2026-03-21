package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.NotificationPageQueryDTO;
import com.example.backend.entity.NotificationMessage;
import com.example.backend.entity.User;
import com.example.backend.enums.NotificationReadStatusEnum;
import com.example.backend.enums.NotificationTypeEnum;
import com.example.backend.enums.UserStatusEnum;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.NotificationMessageMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.NotificationService;
import com.example.backend.vo.NotificationPageVO;
import com.example.backend.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 通知服务实现类。
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	/**
	 * 默认页码。
	 */
	private static final long DEFAULT_CURRENT = 1L;

	/**
	 * 默认每页条数。
	 */
	private static final long DEFAULT_SIZE = 10L;

	/**
	 * 每页最大条数。
	 */
	private static final long MAX_SIZE = 50L;

	/**
	 * 通知业务类型：借阅记录。
	 */
	private static final String BORROW_BUSINESS_TYPE = "BORROW_RECORD";

	private final NotificationMessageMapper notificationMessageMapper;
	private final UserMapper userMapper;

	/**
	 * 发送借阅超期提醒通知。
	 *
	 * @param userId 用户ID
	 * @param borrowId 借阅记录ID
	 * @param bookName 图书名称
	 * @param overdueDays 超期天数
	 * @param fineAmount 罚款金额
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void createBorrowOverdueNotification(Long userId, Long borrowId, String bookName, Integer overdueDays, BigDecimal fineAmount) {
		if (userId == null || userId <= 0 || borrowId == null || borrowId <= 0) {
			return;
		}

		LocalDateTime now = LocalDateTime.now();
		NotificationMessage notification = new NotificationMessage();
		notification.setUserId(userId);
		notification.setNotificationType(NotificationTypeEnum.BORROW_OVERDUE.getCode());
		notification.setTitle("图书超期提醒");
		notification.setContent(buildBorrowOverdueContent(bookName, overdueDays, fineAmount));
		notification.setBusinessType(BORROW_BUSINESS_TYPE);
		notification.setBusinessId(borrowId);
		notification.setReadStatus(NotificationReadStatusEnum.UNREAD.getCode());
		notification.setReadTime(null);
		notification.setCreateTime(now);
		notification.setUpdateTime(now);

		try {
			// 借助唯一索引保证同一条借阅记录只生成一条超期提醒，避免定时任务重复发送。
			notificationMessageMapper.insert(notification);
		} catch (DuplicateKeyException ignored) {
			// 并发场景下若已有相同通知，则直接忽略即可。
		}
	}

	/**
	 * 发送预约兑现后的借阅成功通知。
	 *
	 * @param userId 用户ID
	 * @param borrowId 借阅记录ID
	 * @param bookName 图书名称
	 * @param dueDate 应还时间
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void createReservationBorrowSuccessNotification(Long userId, Long borrowId, String bookName, LocalDateTime dueDate) {
		if (userId == null || userId <= 0 || borrowId == null || borrowId <= 0) {
			return;
		}

		LocalDateTime now = LocalDateTime.now();
		NotificationMessage notification = new NotificationMessage();
		notification.setUserId(userId);
		notification.setNotificationType(NotificationTypeEnum.RESERVATION_BORROW_SUCCESS.getCode());
		notification.setTitle("预约借阅成功");
		notification.setContent(buildReservationBorrowSuccessContent(bookName, dueDate));
		notification.setBusinessType(BORROW_BUSINESS_TYPE);
		notification.setBusinessId(borrowId);
		notification.setReadStatus(NotificationReadStatusEnum.UNREAD.getCode());
		notification.setReadTime(null);
		notification.setCreateTime(now);
		notification.setUpdateTime(now);

		try {
			notificationMessageMapper.insert(notification);
		} catch (DuplicateKeyException ignored) {
			// 并发场景下若已有相同通知，则直接忽略即可。
		}
	}

	/**
	 * 分页查询我的通知。
	 *
	 * @param userId 用户ID
	 * @param queryDTO 查询参数
	 * @return 通知分页结果
	 */
	@Override
	public PageResult<NotificationPageVO> pageMyNotifications(Long userId, NotificationPageQueryDTO queryDTO) {
		requireAvailableUser(userId);

		Page<NotificationMessage> page = new Page<>(normalizeCurrent(queryDTO), normalizeSize(queryDTO));
		LambdaQueryWrapper<NotificationMessage> queryWrapper = new LambdaQueryWrapper<NotificationMessage>()
			.eq(NotificationMessage::getUserId, userId);
		if (queryDTO != null && queryDTO.getReadStatus() != null) {
			queryWrapper.eq(NotificationMessage::getReadStatus, queryDTO.getReadStatus());
		}
		queryWrapper.orderByAsc(NotificationMessage::getReadStatus)
			.orderByDesc(NotificationMessage::getCreateTime)
			.orderByDesc(NotificationMessage::getNotificationId);

		Page<NotificationMessage> resultPage = notificationMessageMapper.selectPage(page, queryWrapper);
		List<NotificationPageVO> records = (resultPage.getRecords() == null ? List.<NotificationMessage>of() : resultPage.getRecords()).stream()
			.map(this::buildNotificationPageVO)
			.toList();
		return new PageResult<>(
			records,
			resultPage.getTotal(),
			resultPage.getCurrent(),
			resultPage.getSize(),
			resultPage.getPages()
		);
	}

	/**
	 * 标记我的通知为已读。
	 *
	 * @param userId 用户ID
	 * @param notificationId 通知ID
	 * @return 更新后的通知
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public NotificationPageVO markMyNotificationRead(Long userId, Long notificationId) {
		requireAvailableUser(userId);
		if (notificationId == null || notificationId <= 0) {
			throw new BusinessException("通知ID不合法");
		}

		NotificationMessage notification = notificationMessageMapper.selectById(notificationId);
		if (notification == null) {
			throw new BusinessException("通知不存在");
		}
		if (!Objects.equals(notification.getUserId(), userId)) {
			throw new BusinessException("无权操作该通知");
		}
		if (!Objects.equals(notification.getReadStatus(), NotificationReadStatusEnum.READ.getCode())) {
			LocalDateTime now = LocalDateTime.now();
			notification.setReadStatus(NotificationReadStatusEnum.READ.getCode());
			notification.setReadTime(now);
			notification.setUpdateTime(now);
			notificationMessageMapper.updateById(notification);
		}
		return buildNotificationPageVO(notificationMessageMapper.selectById(notificationId));
	}

	/**
	 * 校验并返回可用用户。
	 *
	 * @param userId 用户ID
	 * @return 用户实体
	 */
	private User requireAvailableUser(Long userId) {
		if (userId == null || userId <= 0) {
			throw new BusinessException("请先登录后查看通知");
		}

		User user = userMapper.selectById(userId);
		if (user == null) {
			throw new BusinessException("用户不存在");
		}
		if (!Objects.equals(user.getStatus(), UserStatusEnum.NORMAL.getCode())) {
			throw new BusinessException("当前账号已被禁用，无法查看通知");
		}
		return user;
	}

	/**
	 * 构建借阅超期提醒内容。
	 *
	 * @param bookName 图书名称
	 * @param overdueDays 超期天数
	 * @param fineAmount 罚款金额
	 * @return 通知内容
	 */
	private String buildBorrowOverdueContent(String bookName, Integer overdueDays, BigDecimal fineAmount) {
		String safeBookName = (bookName == null || bookName.isBlank()) ? "当前图书" : bookName.trim();
		int safeOverdueDays = overdueDays == null || overdueDays <= 0 ? 1 : overdueDays;
		BigDecimal safeFineAmount = fineAmount == null ? BigDecimal.ZERO : fineAmount;
		return String.format("你借阅的《%s》已超期 %d 天，当前罚款 %.2f 元，请尽快归还。", safeBookName, safeOverdueDays, safeFineAmount);
	}

	/**
	 * 构建预约借阅成功通知内容。
	 *
	 * @param bookName 图书名称
	 * @param dueDate 应还时间
	 * @return 通知内容
	 */
	private String buildReservationBorrowSuccessContent(String bookName, LocalDateTime dueDate) {
		String safeBookName = (bookName == null || bookName.isBlank()) ? "当前图书" : bookName.trim();
		String dueDateText = dueDate == null ? "系统暂未生成应还时间" : dueDate.toString().replace('T', ' ');
		return String.format("你预约的《%s》已自动借阅成功，无需管理员审核，应还时间：%s。", safeBookName, dueDateText);
	}

	/**
	 * 构建通知分页返回对象。
	 *
	 * @param notification 通知实体
	 * @return 通知分页返回对象
	 */
	private NotificationPageVO buildNotificationPageVO(NotificationMessage notification) {
		NotificationPageVO vo = new NotificationPageVO();
		if (notification == null) {
			return vo;
		}
		vo.setNotificationId(notification.getNotificationId());
		vo.setNotificationType(notification.getNotificationType());
		vo.setTitle(notification.getTitle());
		vo.setContent(notification.getContent());
		vo.setBusinessType(notification.getBusinessType());
		vo.setBusinessId(notification.getBusinessId());
		vo.setReadStatus(notification.getReadStatus());
		vo.setReadTime(notification.getReadTime());
		vo.setCreateTime(notification.getCreateTime());
		return vo;
	}

	/**
	 * 规范化当前页码。
	 *
	 * @param queryDTO 查询参数
	 * @return 当前页码
	 */
	private long normalizeCurrent(NotificationPageQueryDTO queryDTO) {
		if (queryDTO == null || queryDTO.getCurrent() == null || queryDTO.getCurrent() <= 0) {
			return DEFAULT_CURRENT;
		}
		return queryDTO.getCurrent();
	}

	/**
	 * 规范化每页条数。
	 *
	 * @param queryDTO 查询参数
	 * @return 每页条数
	 */
	private long normalizeSize(NotificationPageQueryDTO queryDTO) {
		if (queryDTO == null || queryDTO.getSize() == null || queryDTO.getSize() <= 0) {
			return DEFAULT_SIZE;
		}
		return Math.min(queryDTO.getSize(), MAX_SIZE);
	}
}
