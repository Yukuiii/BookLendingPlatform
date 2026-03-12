import { request } from '../utils/request'

/**
 * 查询管理端用户分页列表。
 *
 * @param {{current?: number, size?: number, username?: string, realName?: string, userType?: number|null, status?: number|null}} params 查询参数
 * @returns {Promise<any>} 用户分页结果
 */
export function pageAdminUsers(params = {}) {
  return buildGetRequest('/admin/users/page', params)
}

/**
 * 修改管理端用户。
 *
 * @param {number|string} userId 用户ID
 * @param {object} payload 修改参数
 * @returns {Promise<any>} 修改结果
 */
export function updateAdminUser(userId, payload) {
  return request(`/admin/users/${encodeURIComponent(userId)}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

/**
 * 查询管理端图书分页列表。
 *
 * @param {{current?: number, size?: number, bookName?: string, author?: string, status?: number|null}} params 查询参数
 * @returns {Promise<any>} 图书分页结果
 */
export function pageAdminBooks(params = {}) {
  return buildGetRequest('/admin/books/page', params)
}

/**
 * 新增图书。
 *
 * @param {object} payload 图书参数
 * @returns {Promise<any>} 新增结果
 */
export function createAdminBook(payload) {
  return request('/admin/books', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/**
 * 修改图书。
 *
 * @param {number|string} bookId 图书ID
 * @param {object} payload 图书参数
 * @returns {Promise<any>} 修改结果
 */
export function updateAdminBook(bookId, payload) {
  return request(`/admin/books/${encodeURIComponent(bookId)}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

/**
 * 查询图书分类列表。
 *
 * @returns {Promise<any[]>} 图书分类列表
 */
export function listAdminBookCategories() {
  return request('/admin/books/categories', {
    method: 'GET',
  })
}

/**
 * 查询管理端图书位置分页列表。
 *
 * @param {{current?: number, size?: number, bookName?: string, isbn?: string}} params 查询参数
 * @returns {Promise<any>} 图书位置分页结果
 */
export function pageAdminBookLocations(params = {}) {
  return buildGetRequest('/admin/book-locations/page', params)
}

/**
 * 新增图书位置。
 *
 * @param {object} payload 图书位置参数
 * @returns {Promise<any>} 新增结果
 */
export function createAdminBookLocation(payload) {
  return request('/admin/book-locations', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/**
 * 修改图书位置。
 *
 * @param {number|string} locationId 位置ID
 * @param {object} payload 图书位置参数
 * @returns {Promise<any>} 修改结果
 */
export function updateAdminBookLocation(locationId, payload) {
  return request(`/admin/book-locations/${encodeURIComponent(locationId)}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

/**
 * 查询管理端借阅记录分页列表。
 *
 * @param {{current?: number, size?: number, username?: string, bookName?: string, status?: number|null}} params 查询参数
 * @returns {Promise<any>} 借阅记录分页结果
 */
export function pageAdminBorrowRecords(params = {}) {
  return buildGetRequest('/admin/borrow-records/page', params)
}

/**
 * 查询管理端评论分页列表。
 *
 * @param {{current?: number, size?: number, username?: string, bookName?: string, status?: number|null}} params 查询参数
 * @returns {Promise<any>} 评论分页结果
 */
export function pageAdminComments(params = {}) {
  return buildGetRequest('/admin/comments/page', params)
}

/**
 * 管理端修改评论状态。
 *
 * @param {number|string} commentId 评论ID
 * @param {{status: number}} payload 修改参数
 * @returns {Promise<any>} 修改结果
 */
export function updateAdminCommentStatus(commentId, payload) {
  return request(`/admin/comments/${encodeURIComponent(commentId)}/status`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

/**
 * 管理端审核通过借阅记录。
 *
 * @param {number|string} borrowId 借阅记录ID
 * @returns {Promise<any>} 审核结果
 */
export function approveAdminBorrowRecord(borrowId) {
  return request(`/admin/borrow-records/${encodeURIComponent(borrowId)}/approve`, {
    method: 'POST',
  })
}

/**
 * 管理端归还借阅记录。
 *
 * @param {number|string} borrowId 借阅记录ID
 * @returns {Promise<any>} 归还结果
 */
export function returnAdminBorrowRecord(borrowId) {
  return request(`/admin/borrow-records/${encodeURIComponent(borrowId)}/return`, {
    method: 'POST',
  })
}

/**
 * 获取管理端统计数据。
 *
 * @returns {Promise<any>} 统计数据
 */
export function getAdminStatistics() {
  return request('/admin/statistics', {
    method: 'GET',
  })
}

/**
 * 构建 GET 请求。
 *
 * @param {string} path 请求路径
 * @param {object} params 查询参数
 * @returns {Promise<any>} 请求结果
 */
function buildGetRequest(path, params = {}) {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }
    searchParams.append(key, String(value))
  })

  const queryString = searchParams.toString()
  const requestPath = queryString ? `${path}?${queryString}` : path

  return request(requestPath, {
    method: 'GET',
  })
}
