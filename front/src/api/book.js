import { request } from '../utils/request'

/**
 * 分页查询图书列表。
 *
 * @param {{current?: number, size?: number, bookName?: string, author?: string, status?: number | null}} params 查询参数
 * @returns {Promise<any>} 图书分页结果
 */
export function pageBooks(params = {}) {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }
    searchParams.append(key, String(value))
  })

  const queryString = searchParams.toString()
  const requestPath = queryString ? `/books/page?${queryString}` : '/books/page'

  return request(requestPath, {
    method: 'GET',
  })
}

/**
 * 查询图书详情。
 *
 * @param {number|string} bookId 图书ID
 * @returns {Promise<any>} 图书详情
 */
export function getBookDetail(bookId) {
  return request(`/books/${encodeURIComponent(bookId)}`, {
    method: 'GET',
  })
}

/**
 * 查询当前用户猜你喜欢图书。
 *
 * @param {number} limit 返回条数
 * @returns {Promise<any[]>} 推荐图书列表
 */
export function listRecommendedBooks(limit = 4) {
  const searchParams = new URLSearchParams()
  if (limit > 0) {
    searchParams.append('limit', String(limit))
  }

  const queryString = searchParams.toString()
  const requestPath = queryString ? `/books/recommendations?${queryString}` : '/books/recommendations'

  return request(requestPath, {
    method: 'GET',
  })
}
