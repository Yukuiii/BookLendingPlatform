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
