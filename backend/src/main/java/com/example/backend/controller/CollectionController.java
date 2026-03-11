package com.example.backend.controller;

import com.example.backend.dto.CollectBookRequestDTO;
import com.example.backend.dto.CollectionPageQueryDTO;
import com.example.backend.dto.UpdateCollectionRecordCategoryDTO;
import com.example.backend.service.CollectionService;
import com.example.backend.vo.CollectionPageVO;
import com.example.backend.vo.CollectionResultVO;
import com.example.backend.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收藏接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/collections")
public class CollectionController {

	private final CollectionService collectionService;

	/**
	 * 收藏图书。
	 *
	 * @param userId 当前用户ID
	 * @param requestDTO 收藏参数
	 * @return 收藏结果
	 */
	@PostMapping
	public CollectionResultVO collectBook(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@RequestBody CollectBookRequestDTO requestDTO
	) {
		return collectionService.collectBook(userId, requestDTO);
	}

	/**
	 * 分页查询我的收藏。
	 *
	 * @param userId 当前用户ID
	 * @param queryDTO 查询参数
	 * @return 收藏分页结果
	 */
	@GetMapping("/my/page")
	public PageResult<CollectionPageVO> pageMyCollections(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@ModelAttribute CollectionPageQueryDTO queryDTO
	) {
		return collectionService.pageMyCollections(userId, queryDTO);
	}

	/**
	 * 取消收藏。
	 *
	 * @param userId 当前用户ID
	 * @param collectionId 收藏记录ID
	 */
	@DeleteMapping("/{collectionId}")
	public void removeCollection(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@PathVariable Long collectionId
	) {
		collectionService.removeCollection(userId, collectionId);
	}

	/**
	 * 修改收藏所属分类。
	 *
	 * @param userId 当前用户ID
	 * @param collectionId 收藏记录ID
	 * @param requestDTO 修改参数
	 * @return 修改结果
	 */
	@PutMapping("/{collectionId}/category")
	public CollectionResultVO updateCollectionCategory(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@PathVariable Long collectionId,
		@RequestBody UpdateCollectionRecordCategoryDTO requestDTO
	) {
		return collectionService.updateCollectionCategory(userId, collectionId, requestDTO);
	}
}

