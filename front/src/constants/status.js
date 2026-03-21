/**
 * 前端页面共用的业务状态常量。
 */

export const BORROW_STATUS = Object.freeze({
  BORROWING: 1,
  RETURNED: 2,
  OVERDUE: 3,
  PENDING_REVIEW: 4,
})

export const BORROW_STATUS_OPTIONS = Object.freeze([
  { label: '借阅中', value: BORROW_STATUS.BORROWING },
  { label: '已归还', value: BORROW_STATUS.RETURNED },
  { label: '超期', value: BORROW_STATUS.OVERDUE },
  { label: '审核中', value: BORROW_STATUS.PENDING_REVIEW },
])

export const BORROW_STATUS_LABEL_MAP = Object.freeze({
  [BORROW_STATUS.BORROWING]: '借阅中',
  [BORROW_STATUS.RETURNED]: '已归还',
  [BORROW_STATUS.OVERDUE]: '超期',
  [BORROW_STATUS.PENDING_REVIEW]: '审核中',
})

export const BORROW_STATUS_TAG_TYPE_MAP = Object.freeze({
  [BORROW_STATUS.BORROWING]: 'success',
  [BORROW_STATUS.RETURNED]: 'info',
  [BORROW_STATUS.OVERDUE]: 'danger',
  [BORROW_STATUS.PENDING_REVIEW]: 'warning',
})

export const COMMENT_STATUS = Object.freeze({
  HIDDEN: 0,
  VISIBLE: 1,
  PENDING: 2,
})

export const COMMENT_STATUS_OPTIONS = Object.freeze([
  { label: '审核通过', value: COMMENT_STATUS.VISIBLE },
  { label: '审核中', value: COMMENT_STATUS.PENDING },
  { label: '已隐藏', value: COMMENT_STATUS.HIDDEN },
])

export const COMMENT_STATUS_LABEL_MAP = Object.freeze({
  [COMMENT_STATUS.HIDDEN]: '已隐藏',
  [COMMENT_STATUS.VISIBLE]: '审核通过',
  [COMMENT_STATUS.PENDING]: '审核中',
})

export const COMMENT_STATUS_TAG_TYPE_MAP = Object.freeze({
  [COMMENT_STATUS.HIDDEN]: 'info',
  [COMMENT_STATUS.VISIBLE]: 'success',
  [COMMENT_STATUS.PENDING]: 'warning',
})

export const BOOK_STATUS = Object.freeze({
  OFF_SHELF: 0,
  NORMAL: 1,
})

export const BOOK_STATUS_OPTIONS = Object.freeze([
  { label: '正常', value: BOOK_STATUS.NORMAL },
  { label: '下架', value: BOOK_STATUS.OFF_SHELF },
])

export const BOOK_STATUS_LABEL_MAP = Object.freeze({
  [BOOK_STATUS.NORMAL]: '正常',
  [BOOK_STATUS.OFF_SHELF]: '下架',
})

export const BOOK_STATUS_TAG_TYPE_MAP = Object.freeze({
  [BOOK_STATUS.NORMAL]: 'success',
  [BOOK_STATUS.OFF_SHELF]: 'info',
})

export const USER_STATUS = Object.freeze({
  DISABLED: 0,
  NORMAL: 1,
})

export const USER_STATUS_OPTIONS = Object.freeze([
  { label: '正常', value: USER_STATUS.NORMAL },
  { label: '禁用', value: USER_STATUS.DISABLED },
])

export const USER_STATUS_LABEL_MAP = Object.freeze({
  [USER_STATUS.NORMAL]: '正常',
  [USER_STATUS.DISABLED]: '禁用',
})

export const USER_STATUS_TAG_TYPE_MAP = Object.freeze({
  [USER_STATUS.NORMAL]: 'success',
  [USER_STATUS.DISABLED]: 'danger',
})

export const RESERVATION_STATUS = Object.freeze({
  WAITING: 1,
  FULFILLED: 2,
  EXPIRED: 3,
})

export const RESERVATION_STATUS_OPTIONS = Object.freeze([
  { label: '排队中', value: RESERVATION_STATUS.WAITING },
  { label: '已完成', value: RESERVATION_STATUS.FULFILLED },
  { label: '已失效', value: RESERVATION_STATUS.EXPIRED },
])

export const RESERVATION_STATUS_LABEL_MAP = Object.freeze({
  [RESERVATION_STATUS.WAITING]: '排队中',
  [RESERVATION_STATUS.FULFILLED]: '已完成',
  [RESERVATION_STATUS.EXPIRED]: '已失效',
})

export const RESERVATION_STATUS_TAG_TYPE_MAP = Object.freeze({
  [RESERVATION_STATUS.WAITING]: 'warning',
  [RESERVATION_STATUS.FULFILLED]: 'success',
  [RESERVATION_STATUS.EXPIRED]: 'info',
})

export const NOTIFICATION_READ_STATUS = Object.freeze({
  UNREAD: 0,
  READ: 1,
})

export const NOTIFICATION_READ_STATUS_OPTIONS = Object.freeze([
  { label: '未读', value: NOTIFICATION_READ_STATUS.UNREAD },
  { label: '已读', value: NOTIFICATION_READ_STATUS.READ },
])

export const NOTIFICATION_READ_STATUS_LABEL_MAP = Object.freeze({
  [NOTIFICATION_READ_STATUS.UNREAD]: '未读',
  [NOTIFICATION_READ_STATUS.READ]: '已读',
})

export const NOTIFICATION_READ_STATUS_TAG_TYPE_MAP = Object.freeze({
  [NOTIFICATION_READ_STATUS.UNREAD]: 'danger',
  [NOTIFICATION_READ_STATUS.READ]: 'info',
})
