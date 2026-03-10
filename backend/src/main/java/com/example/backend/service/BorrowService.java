package com.example.backend.service;

import com.example.backend.dto.BorrowBookRequestDTO;
import com.example.backend.dto.BorrowRecordPageQueryDTO;
import com.example.backend.vo.BorrowRecordPageVO;
import com.example.backend.vo.BorrowResultVO;
import com.example.backend.vo.PageResult;
import com.example.backend.vo.ReturnBookVO;

/**
 * 借阅服务接口。
 */
public interface BorrowService {

	/**
	 * 立即借阅图书。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 借阅请求参数
	 * @return 借阅结果
	 */
	BorrowResultVO borrowBook(Long userId, BorrowBookRequestDTO requestDTO);

	/**
	 * 归还图书。
	 *
	 * @param userId 用户ID
	 * @param borrowId 借阅记录ID
	 * @return 归还结果
	 */
	ReturnBookVO returnBook(Long userId, Long borrowId);

	/**
	 * 分页查询我的借阅记录。
	 *
	 * @param userId 用户ID
	 * @param queryDTO 查询参数
	 * @return 借阅记录分页结果
	 */
	PageResult<BorrowRecordPageVO> pageMyBorrowRecords(Long userId, BorrowRecordPageQueryDTO queryDTO);
}
