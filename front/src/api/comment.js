import { request } from '../utils/request'

/**
 * 对已归还图书发表评论。
 *
 * @param {{borrowId: number | string, rating: number, content: string}} payload 评论参数
 * @returns {Promise<any>} 评论结果
 */
export function createComment(payload) {
  return request('/comments', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/**
 * 查询图书审核通过的评论列表。
 *
 * @param {number|string} bookId 图书ID
 * @returns {Promise<any[]>} 评论列表
 */
export function listApprovedBookComments(bookId) {
  return request(`/comments/book/${encodeURIComponent(bookId)}/approved`, {
    method: 'GET',
  })
}

/**
 * 分页查询我的评论。
 *
 * @param {{current?: number, size?: number}} params 查询参数
 * @returns {Promise<any>} 评论分页结果
 */
export function pageMyComments(params = {}) {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }
    searchParams.append(key, String(value))
  })

  const queryString = searchParams.toString()
  const requestPath = queryString ? `/comments/my/page?${queryString}` : '/comments/my/page'

  return request(requestPath, {
    method: 'GET',
  })
}
