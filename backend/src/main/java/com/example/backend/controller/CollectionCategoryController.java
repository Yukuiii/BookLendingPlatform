package com.example.backend.controller;

import com.example.backend.dto.CreateCollectionCategoryDTO;
import com.example.backend.service.CollectionService;
import com.example.backend.vo.CollectionCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 收藏分类接口控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/collection-categories")
public class CollectionCategoryController {

	private final CollectionService collectionService;

	/**
	 * 查询当前用户收藏分类列表。
	 *
	 * @param userId 当前用户ID
	 * @return 收藏分类列表
	 */
	@GetMapping("/my")
	public List<CollectionCategoryVO> listMyCollectionCategories(
		@RequestHeader(value = "X-User-Id", required = false) Long userId
	) {
		return collectionService.listMyCollectionCategories(userId);
	}

	/**
	 * 新建收藏分类。
	 *
	 * @param userId 当前用户ID
	 * @param requestDTO 新建参数
	 * @return 新建后的分类
	 */
	@PostMapping
	public CollectionCategoryVO createCollectionCategory(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@RequestBody CreateCollectionCategoryDTO requestDTO
	) {
		return collectionService.createCollectionCategory(userId, requestDTO);
	}

	/**
	 * 删除收藏分类。
	 *
	 * @param userId 当前用户ID
	 * @param collectionCategoryId 收藏分类ID
	 */
	@DeleteMapping("/{collectionCategoryId}")
	public void removeCollectionCategory(
		@RequestHeader(value = "X-User-Id", required = false) Long userId,
		@PathVariable Long collectionCategoryId
	) {
		collectionService.removeCollectionCategory(userId, collectionCategoryId);
	}
}
