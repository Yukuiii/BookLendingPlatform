package com.example.backend.scheduler;

import com.example.backend.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 借阅超期状态定时刷新任务。
 */
@Component
@RequiredArgsConstructor
public class BorrowOverdueRefreshScheduler {

	private final BorrowService borrowService;

	/**
	 * 定时刷新全部未归还借阅记录的超期状态与罚款。
	 */
	@Scheduled(
		initialDelayString = "${library.borrow.overdue-refresh.initial-delay-ms:60000}",
		fixedDelayString = "${library.borrow.overdue-refresh.fixed-delay-ms:600000}"
	)
	public void refreshBorrowOverdueStatus() {
		borrowService.refreshAllExpiredBorrowRecords();
	}
}
