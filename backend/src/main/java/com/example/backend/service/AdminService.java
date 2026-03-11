package com.example.backend.service;

import com.example.backend.dto.AdminBookLocationPageQueryDTO;
import com.example.backend.dto.AdminBookLocationSaveDTO;
import com.example.backend.dto.AdminBookSaveDTO;
import com.example.backend.dto.AdminBorrowRecordPageQueryDTO;
import com.example.backend.dto.BookPageQueryDTO;
import com.example.backend.entity.BookCategory;
import com.example.backend.vo.AdminBookLocationVO;
import com.example.backend.vo.AdminBorrowRecordPageVO;
import com.example.backend.vo.BookDetailVO;
import com.example.backend.vo.BookPageVO;
import com.example.backend.vo.PageResult;
import com.example.backend.vo.ReturnBookVO;

import java.util.List;

/**
 * 管理端服务接口。
 */
public interface AdminService {

	/**
	 * 管理端分页查询图书。
	 *
	 * @param adminUserId 管理员ID
	 * @param queryDTO 查询参数
	 * @return 图书分页结果
	 */
	PageResult<BookPageVO> pageAdminBooks(Long adminUserId, BookPageQueryDTO queryDTO);

	/**
	 * 管理端新增图书。
	 *
	 * @param adminUserId 管理员ID
	 * @param requestDTO 新增参数
	 * @return 图书详情
	 */
	BookDetailVO createAdminBook(Long adminUserId, AdminBookSaveDTO requestDTO);

	/**
	 * 管理端修改图书。
	 *
	 * @param adminUserId 管理员ID
	 * @param bookId 图书ID
	 * @param requestDTO 修改参数
	 * @return 图书详情
	 */
	BookDetailVO updateAdminBook(Long adminUserId, Long bookId, AdminBookSaveDTO requestDTO);

	/**
	 * 查询可用图书分类。
	 *
	 * @param adminUserId 管理员ID
	 * @return 图书分类列表
	 */
	List<BookCategory> listAdminBookCategories(Long adminUserId);

	/**
	 * 分页查询图书位置。
	 *
	 * @param adminUserId 管理员ID
	 * @param queryDTO 查询参数
	 * @return 图书位置分页结果
	 */
	PageResult<AdminBookLocationVO> pageAdminBookLocations(Long adminUserId, AdminBookLocationPageQueryDTO queryDTO);

	/**
	 * 新增图书位置。
	 *
	 * @param adminUserId 管理员ID
	 * @param requestDTO 新增参数
	 * @return 图书位置
	 */
	AdminBookLocationVO createAdminBookLocation(Long adminUserId, AdminBookLocationSaveDTO requestDTO);

	/**
	 * 修改图书位置。
	 *
	 * @param adminUserId 管理员ID
	 * @param locationId 位置ID
	 * @param requestDTO 修改参数
	 * @return 图书位置
	 */
	AdminBookLocationVO updateAdminBookLocation(Long adminUserId, Long locationId, AdminBookLocationSaveDTO requestDTO);

	/**
	 * 分页查询借阅记录。
	 *
	 * @param adminUserId 管理员ID
	 * @param queryDTO 查询参数
	 * @return 借阅记录分页结果
	 */
	PageResult<AdminBorrowRecordPageVO> pageAdminBorrowRecords(Long adminUserId, AdminBorrowRecordPageQueryDTO queryDTO);

	/**
	 * 管理端归还图书。
	 *
	 * @param adminUserId 管理员ID
	 * @param borrowId 借阅记录ID
	 * @return 归还结果
	 */
	ReturnBookVO returnAdminBorrowRecord(Long adminUserId, Long borrowId);
}

