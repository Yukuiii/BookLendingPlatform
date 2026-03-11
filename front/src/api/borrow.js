import { request } from '../utils/request'

/**
 * 立即借阅图书。
 *
 * @param {number|string} bookId 图书ID
 * @returns {Promise<any>} 借阅结果
 */
export function borrowBook(bookId) {
  return request('/borrow-records', {
    method: 'POST',
    body: JSON.stringify({
      bookId,
    }),
  })
}

/**
 * 续借图书。
 *
 * @param {number|string} borrowId 借阅记录ID
 * @returns {Promise<any>} 续借结果
 */
export function renewBorrowBook(borrowId) {
  return request(`/borrow-records/${encodeURIComponent(borrowId)}/renew`, {
    method: 'POST',
  })
}

/**
 * 归还图书。
 *
 * @param {number|string} borrowId 借阅记录ID
 * @returns {Promise<any>} 归还结果
 */
export function returnBorrowBook(borrowId) {
  return request(`/borrow-records/${encodeURIComponent(borrowId)}/return`, {
    method: 'POST',
  })
}

/**
 * 分页查询我的借阅记录。
 *
 * @param {{current?: number, size?: number, status?: number | null}} params 查询参数
 * @returns {Promise<any>} 借阅记录分页结果
 */
export function pageMyBorrowRecords(params = {}) {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }
    searchParams.append(key, String(value))
  })

  const queryString = searchParams.toString()
  const requestPath = queryString ? `/borrow-records/my/page?${queryString}` : '/borrow-records/my/page'

  return request(requestPath, {
    method: 'GET',
  })
}
