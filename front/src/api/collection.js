import { request } from '../utils/request'

/**
 * 查询当前用户收藏分类列表。
 *
 * @returns {Promise<any[]>} 收藏分类列表
 */
export function listMyCollectionCategories() {
  return request('/collection-categories/my', {
    method: 'GET',
  })
}

/**
 * 新建收藏分类。
 *
 * @param {{categoryName: string}} payload 新建分类参数
 * @returns {Promise<any>} 新建后的分类
 */
export function createCollectionCategory(payload) {
  return request('/collection-categories', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/**
 * 收藏图书。
 *
 * @param {{bookId: number|string, collectionCategoryId?: number|string|null}} payload 收藏参数
 * @returns {Promise<any>} 收藏结果
 */
export function collectBook(payload) {
  return request('/collections', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/**
 * 分页查询我的收藏。
 *
 * @param {{current?: number, size?: number, collectionCategoryId?: number|string|null}} params 查询参数
 * @returns {Promise<any>} 收藏分页结果
 */
export function pageMyCollections(params = {}) {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }
    searchParams.append(key, String(value))
  })

  const queryString = searchParams.toString()
  const requestPath = queryString ? `/collections/my/page?${queryString}` : '/collections/my/page'

  return request(requestPath, {
    method: 'GET',
  })
}

/**
 * 取消收藏。
 *
 * @param {number|string} collectionId 收藏记录ID
 * @returns {Promise<any>} 取消结果
 */
export function removeCollection(collectionId) {
  return request(`/collections/${encodeURIComponent(collectionId)}`, {
    method: 'DELETE',
  })
}

/**
 * 修改收藏所属分类。
 *
 * @param {number|string} collectionId 收藏记录ID
 * @param {{collectionCategoryId: number|string}} payload 修改参数
 * @returns {Promise<any>} 修改结果
 */
export function updateCollectionCategory(collectionId, payload) {
  return request(`/collections/${encodeURIComponent(collectionId)}/category`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

