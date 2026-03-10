/**
 * 格式化图书位置信息。
 *
 * @param {object} book 图书对象
 * @returns {string} 位置信息
 */
export function formatLocation(book) {
  const floor = book?.floor
  const area = book?.area
  const shelfNo = book?.shelfNo
  const layer = book?.layer

  if (floor == null && !area && !shelfNo && layer == null) {
    return '暂无'
  }

  const fragments = []
  if (floor != null) {
    fragments.push(`${floor}层`)
  }
  if (area) {
    fragments.push(`${area}区`)
  }
  if (shelfNo) {
    fragments.push(`书架${shelfNo}`)
  }
  if (layer != null) {
    fragments.push(`第${layer}层`)
  }
  return fragments.join(' · ')
}

/**
 * 格式化后端返回的日期时间。
 *
 * @param {string | null | undefined} value 日期时间字符串
 * @returns {string} 格式化结果
 */
export function formatDateTime(value) {
  if (!value) {
    return '暂无'
  }

  const text = String(value)
  if (text.includes('T')) {
    return text.replace('T', ' ').replace(/\.\d+$/, '')
  }
  return text
}

