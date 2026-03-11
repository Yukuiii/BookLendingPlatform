package com.example.backend.service;

import com.example.backend.dto.BookPageQueryDTO;
import com.example.backend.vo.BookDetailVO;
import com.example.backend.vo.BookPageVO;
import com.example.backend.vo.PageResult;

import java.util.List;

/**
 * 图书服务接口。
 */
public interface BookService {

	/**
	 * 分页查询图书信息。
	 *
	 * @param queryDTO 查询参数
	 * @return 图书分页结果
	 */
	PageResult<BookPageVO> pageBooks(BookPageQueryDTO queryDTO);

	/**
	 * 查询图书详情信息。
	 *
	 * @param bookId 图书ID
	 * @return 图书详情
	 */
	BookDetailVO getBookDetail(Long bookId);

	/**
	 * 查询当前用户猜你喜欢图书。
	 *
	 * @param userId 用户ID
	 * @param limit 返回条数
	 * @return 推荐图书列表
	 */
	List<BookPageVO> listRecommendBooks(Long userId, Integer limit);
}
