/**
 * 通知类型常量。
 */
export const NOTIFICATION_TYPE = Object.freeze({
  BORROW_OVERDUE: 1,
  RESERVATION_BORROW_SUCCESS: 2,
})

/**
 * 通知类型文案映射。
 */
export const NOTIFICATION_TYPE_LABEL_MAP = Object.freeze({
  [NOTIFICATION_TYPE.BORROW_OVERDUE]: '超期提醒',
  [NOTIFICATION_TYPE.RESERVATION_BORROW_SUCCESS]: '预约到书',
})
