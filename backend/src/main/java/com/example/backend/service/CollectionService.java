package com.example.backend.service;

import com.example.backend.dto.CollectBookRequestDTO;
import com.example.backend.dto.CollectionPageQueryDTO;
import com.example.backend.dto.CreateCollectionCategoryDTO;
import com.example.backend.dto.UpdateCollectionRecordCategoryDTO;
import com.example.backend.vo.CollectionCategoryVO;
import com.example.backend.vo.CollectionPageVO;
import com.example.backend.vo.CollectionResultVO;
import com.example.backend.vo.PageResult;

import java.util.List;

/**
 * 收藏服务接口。
 */
public interface CollectionService {

	/**
	 * 查询当前用户收藏分类列表。
	 *
	 * @param userId 用户ID
	 * @return 收藏分类列表
	 */
	List<CollectionCategoryVO> listMyCollectionCategories(Long userId);

	/**
	 * 新建收藏分类。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 新建分类参数
	 * @return 新建后的分类
	 */
	CollectionCategoryVO createCollectionCategory(Long userId, CreateCollectionCategoryDTO requestDTO);

	/**
	 * 删除收藏分类。
	 *
	 * @param userId 用户ID
	 * @param collectionCategoryId 收藏分类ID
	 */
	void removeCollectionCategory(Long userId, Long collectionCategoryId);

	/**
	 * 收藏图书。
	 *
	 * @param userId 用户ID
	 * @param requestDTO 收藏参数
	 * @return 收藏结果
	 */
	CollectionResultVO collectBook(Long userId, CollectBookRequestDTO requestDTO);

	/**
	 * 分页查询我的收藏。
	 *
	 * @param userId 用户ID
	 * @param queryDTO 查询参数
	 * @return 收藏分页结果
	 */
	PageResult<CollectionPageVO> pageMyCollections(Long userId, CollectionPageQueryDTO queryDTO);

	/**
	 * 取消收藏。
	 *
	 * @param userId 用户ID
	 * @param collectionId 收藏记录ID
	 */
	void removeCollection(Long userId, Long collectionId);

	/**
	 * 修改收藏所属分类。
	 *
	 * @param userId 用户ID
	 * @param collectionId 收藏记录ID
	 * @param requestDTO 修改参数
	 * @return 更新后的收藏结果
	 */
	CollectionResultVO updateCollectionCategory(Long userId, Long collectionId, UpdateCollectionRecordCategoryDTO requestDTO);
}
