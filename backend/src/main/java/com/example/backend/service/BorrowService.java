package com.example.backend.service;

import com.example.backend.dto.BorrowBookRequestDTO;
import com.example.backend.dto.BorrowRecordPageQueryDTO;
import com.example.backend.dto.ReservationPageQueryDTO;
import com.example.backend.vo.BookReservationVO;
import com.example.backend.vo.BorrowRecordPageVO;
import com.example.backend.vo.BorrowResultVO;
import com.example.backend.vo.PageResult;
import com.example.backend.vo.RenewBookVO;
import com.example.backend.vo.ReservationPageVO;
import com.example.backend.vo.ReturnBookVO;

/**
 * 借阅服务接口。
 */
public interface BorrowService {

	/**
	 * 提交借阅申请。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 借阅请求参数
	 * @return 借阅结果
	 */
	BorrowResultVO borrowBook(Long userId, BorrowBookRequestDTO requestDTO);

	/**
	 * 提交图书预约申请。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 预约请求参数
	 * @return 预约结果
	 */
	BookReservationVO reserveBook(Long userId, BorrowBookRequestDTO requestDTO);

	/**
	 * 管理端审核通过借阅申请。
	 *
	 * @param adminUserId 管理员ID
	 * @param borrowId 借阅记录ID
	 * @return 审核结果
	 */
	BorrowResultVO approveBorrowRecord(Long adminUserId, Long borrowId);

	/**
	 * 续借图书。
	 *
	 * @param userId 用户ID
	 * @param borrowId 借阅记录ID
	 * @return 续借结果
	 */
	RenewBookVO renewBook(Long userId, Long borrowId);

	/**
	 * 归还图书。
	 *
	 * @param userId 用户ID
	 * @param borrowId 借阅记录ID
	 * @return 归还结果
	 */
	ReturnBookVO returnBook(Long userId, Long borrowId);

	/**
	 * 管理端归还图书。
	 *
	 * @param adminUserId 管理员ID
	 * @param borrowId 借阅记录ID
	 * @return 归还结果
	 */
	ReturnBookVO returnAdminBorrowRecord(Long adminUserId, Long borrowId);

	/**
	 * 刷新全部超期未归还记录状态。
	 */
	void refreshAllExpiredBorrowRecords();

	/**
	 * 分页查询我的借阅记录。
	 *
	 * @param userId 用户ID
	 * @param queryDTO 查询参数
	 * @return 借阅记录分页结果
	 */
	PageResult<BorrowRecordPageVO> pageMyBorrowRecords(Long userId, BorrowRecordPageQueryDTO queryDTO);

	/**
	 * 分页查询我的预约记录。
	 *
	 * @param userId 用户ID
	 * @param queryDTO 查询参数
	 * @return 预约记录分页结果
	 */
	PageResult<ReservationPageVO> pageMyReservations(Long userId, ReservationPageQueryDTO queryDTO);

	/**
	 * 取消预约。
	 *
	 * @param userId 用户ID
	 * @param reservationId 预约记录ID
	 */
	void cancelReservation(Long userId, Long reservationId);
}
