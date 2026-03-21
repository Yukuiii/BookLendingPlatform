<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { pageMyBorrowRecords, renewBorrowBook, returnBorrowBook } from '../api/borrow'
import { createComment } from '../api/comment'
import { BORROW_STATUS, BORROW_STATUS_LABEL_MAP, BORROW_STATUS_OPTIONS, BORROW_STATUS_TAG_TYPE_MAP } from '../constants/status'
import { formatDateTime, formatLocation } from '../utils/book'

/**
 * 我的借阅页面，负责展示当前用户的借阅记录。
 */

const loading = ref(false)
const submitError = ref('')
const commentDialogVisible = ref(false)
const commentSubmitting = ref(false)
const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const activeCommentRecord = ref(null)

const queryForm = reactive({
  status: null,
})

const commentForm = reactive({
  rating: 5,
  content: '',
})

const commentRules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
}

const commentFormRef = ref(null)

const statusOptions = BORROW_STATUS_OPTIONS

const MAX_RENEW_COUNT = 1

/**
 * 页面挂载后初始化借阅记录。
 */
onMounted(() => {
  loadBorrowRecords()
})

/**
 * 加载借阅记录分页数据。
 */
async function loadBorrowRecords() {
  loading.value = true
  submitError.value = ''

  try {
    const response = await pageMyBorrowRecords({
      current: currentPage.value,
      size: pageSize.value,
      status: queryForm.status,
    })

    records.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    submitError.value = error.message || '借阅记录加载失败，请稍后重试'
    ElMessage.error(submitError.value)
  } finally {
    loading.value = false
  }
}

/**
 * 执行筛选查询。
 */
function handleSearch() {
  currentPage.value = 1
  loadBorrowRecords()
}

/**
 * 重置筛选条件并重新加载数据。
 */
function handleReset() {
  queryForm.status = null
  currentPage.value = 1
  loadBorrowRecords()
}

/**
 * 切换分页页码。
 *
 * @param {number} page 页码
 */
function handlePageChange(page) {
  currentPage.value = page
  loadBorrowRecords()
}

/**
 * 修改每页展示条数。
 *
 * @param {number} size 每页条数
 */
function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  loadBorrowRecords()
}

/**
 * 处理归还图书操作。
 *
 * @param {object} record 借阅记录对象
 */
async function handleReturn(record) {
  const borrowId = record?.borrowId
  if (!borrowId) {
    ElMessage.warning('借阅记录不完整，暂无法归还')
    return
  }

  try {
    await ElMessageBox.confirm(`确认归还《${record.bookName || '当前图书'}》吗？`, '归还确认', {
      type: 'warning',
      confirmButtonText: '确认归还',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  try {
    const result = await returnBorrowBook(borrowId)
    const returnDate = formatDateTime(result?.returnDate)
    const overdueDays = Number(result?.overdueDays || 0)
    const fineAmount = Number(result?.fineAmount || 0)
    ElMessage.success(
      overdueDays > 0
        ? `归还成功，归还时间：${returnDate}，超期 ${overdueDays} 天，罚款 ${fineAmount} 元`
        : `归还成功，归还时间：${returnDate}`,
    )
    await loadBorrowRecords()
  } catch (error) {
    ElMessage.error(error.message || '归还失败，请稍后重试')
  }
}

/**
 * 处理续借图书操作。
 *
 * @param {object} record 借阅记录对象
 */
async function handleRenew(record) {
  const borrowId = record?.borrowId
  if (!borrowId) {
    ElMessage.warning('借阅记录不完整，暂无法续借')
    return
  }

  try {
    await ElMessageBox.confirm(`确认续借《${record.bookName || '当前图书'}》吗？`, '续借确认', {
      type: 'warning',
      confirmButtonText: '确认续借',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  try {
    const result = await renewBorrowBook(borrowId)
    const dueDate = formatDateTime(result?.dueDate)
    ElMessage.success(`续借成功，新的应还日期：${dueDate}`)
    await loadBorrowRecords()
  } catch (error) {
    ElMessage.error(error.message || '续借失败，请稍后重试')
  }
}

/**
 * 打开评论弹窗。
 *
 * @param {object} record 借阅记录对象
 */
async function openCommentDialog(record) {
  if (!canCreateComment(record)) {
    ElMessage.warning('当前借阅记录暂不可评论')
    return
  }

  activeCommentRecord.value = record
  commentForm.rating = 5
  commentForm.content = ''
  commentDialogVisible.value = true
  await nextTick()
  commentFormRef.value?.clearValidate()
}

/**
 * 提交图书评论。
 */
async function handleCreateComment() {
  try {
    await commentFormRef.value?.validate()
  } catch {
    return
  }

  const borrowId = activeCommentRecord.value?.borrowId
  const content = commentForm.content.trim()
  if (!borrowId) {
    ElMessage.warning('借阅记录不完整，暂无法评论')
    return
  }
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }

  commentSubmitting.value = true
  try {
    await createComment({
      borrowId,
      rating: commentForm.rating,
      content,
    })
    commentDialogVisible.value = false
    ElMessage.success('评论提交成功，等待管理员审核')
    await loadBorrowRecords()
  } catch (error) {
    ElMessage.error(error.message || '评论提交失败，请稍后重试')
  } finally {
    commentSubmitting.value = false
  }
}

/**
 * 生成图书封面占位文本。
 *
 * @param {object} record 借阅记录对象
 * @returns {string} 占位文本
 */
function buildCoverPlaceholder(record) {
  return (record?.bookName || '图书').slice(0, 2)
}

/**
 * 获取借阅状态文案。
 *
 * @param {number} status 借阅状态
 * @returns {string} 状态文案
 */
function resolveBorrowStatusLabel(status) {
  return BORROW_STATUS_LABEL_MAP[Number(status)] || '未知'
}

/**
 * 获取借阅状态标签类型。
 *
 * @param {number} status 借阅状态
 * @returns {string} 标签类型
 */
function resolveBorrowStatusType(status) {
  return BORROW_STATUS_TAG_TYPE_MAP[Number(status)] || 'warning'
}

/**
 * 判断当前记录是否允许归还。
 *
 * @param {number} status 借阅状态
 * @returns {boolean} 是否允许归还
 */
function canReturnBook(status) {
  return Number(status) === BORROW_STATUS.BORROWING || Number(status) === BORROW_STATUS.OVERDUE
}

/**
 * 判断当前记录是否允许续借。
 *
 * @param {object} record 借阅记录对象
 * @returns {boolean} 是否允许续借
 */
function canRenewBook(record) {
  return Number(record?.status) === BORROW_STATUS.BORROWING && Number(record?.renewCount || 0) < MAX_RENEW_COUNT
}

/**
 * 判断当前记录是否允许评论。
 *
 * @param {object} record 借阅记录对象
 * @returns {boolean} 是否允许评论
 */
function canCreateComment(record) {
  return Number(record?.status) === BORROW_STATUS.RETURNED && record?.commented !== true
}

/**
 * 获取评论按钮文案。
 *
 * @param {object} record 借阅记录对象
 * @returns {string} 按钮文案
 */
function resolveCommentActionText(record) {
  if (Number(record?.status) === BORROW_STATUS.PENDING_REVIEW) {
    return '待审核'
  }
  if (Number(record?.status) === BORROW_STATUS.BORROWING && Number(record?.renewCount || 0) >= MAX_RENEW_COUNT) {
    return '续借已满'
  }
  if (record?.commented) {
    const rating = Number(record?.commentRating || 0)
    return rating > 0 ? `已评论 ${rating} 分` : '已评论'
  }
  return '--'
}
</script>

<template>
  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>我的借阅</strong>
          <p>查看与筛选你的借阅记录（审核中、借阅中、已归还、超期），每本书最多续借 1 次</p>
        </div>
        <el-button type="primary" plain @click="handleSearch">刷新列表</el-button>
      </div>
    </template>

    <el-form class="home-filter-form" :model="queryForm" label-width="70px" @submit.prevent>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" clearable placeholder="全部状态">
          <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item class="home-filter-actions">
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <el-alert v-if="submitError" class="home-alert" :closable="false" type="warning" :title="submitError" show-icon />

  <el-card shadow="never">
    <el-table
      v-loading="loading"
      :data="records"
      style="width: 100%"
      :empty-text="loading ? '加载中...' : '暂无借阅记录'"
    >
      <el-table-column label="图书" min-width="320">
        <template #default="{ row }">
          <div class="borrow-book-cell">
            <el-image v-if="row.coverUrl" :src="row.coverUrl" fit="cover" class="borrow-book-cover">
              <template #error>
                <div class="borrow-book-cover-placeholder">{{ buildCoverPlaceholder(row) }}</div>
              </template>
            </el-image>
            <div v-else class="borrow-book-cover-placeholder">{{ buildCoverPlaceholder(row) }}</div>

            <div class="borrow-book-meta">
              <strong class="borrow-book-title">{{ row.bookName || '未知图书' }}</strong>
              <p class="borrow-book-subtitle">{{ row.author || '未知作者' }} · {{ row.publisher || '未知出版社' }}</p>
              <p class="borrow-book-extra">ISBN：{{ row.isbn || '暂无' }}</p>
              <p class="borrow-book-extra">
                分类：{{ row.categoryName || '暂无' }} · 位置：{{ formatLocation(row) }}
              </p>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="申请/借阅时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.borrowDate) }}</template>
      </el-table-column>

      <el-table-column label="应还时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.dueDate) }}</template>
      </el-table-column>

      <el-table-column label="归还时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.returnDate) }}</template>
      </el-table-column>

      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" effect="light" :type="resolveBorrowStatusType(row.status)">
            {{ resolveBorrowStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="续借" width="80" align="center">
        <template #default="{ row }">{{ row.renewCount ?? 0 }}</template>
      </el-table-column>

      <el-table-column label="超期" width="90" align="center">
        <template #default="{ row }">{{ row.overdueDays > 0 ? `${row.overdueDays}天` : '--' }}</template>
      </el-table-column>

      <el-table-column label="罚款" width="90" align="center">
        <template #default="{ row }">{{ row.fineAmount ?? 0 }}</template>
      </el-table-column>

      <el-table-column label="操作" width="176" align="center" fixed="right">
        <template #default="{ row }">
          <div v-if="canReturnBook(row.status)" class="borrow-action-group">
            <el-button v-if="canRenewBook(row)" type="primary" link @click="handleRenew(row)">续借</el-button>
            <el-button type="primary" link @click="handleReturn(row)">立即归还</el-button>
          </div>
          <el-button v-else-if="canCreateComment(row)" type="warning" link @click="openCommentDialog(row)">立即评论</el-button>
          <span v-else class="borrow-action-placeholder">{{ resolveCommentActionText(row) }}</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <div class="home-pagination">
    <el-pagination
      background
      layout="total, sizes, prev, pager, next"
      :current-page="currentPage"
      :page-size="pageSize"
      :page-sizes="[10, 20, 50]"
      :total="total"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>

  <el-dialog
    v-model="commentDialogVisible"
    width="520px"
    title="发表图书评论"
    destroy-on-close
  >
    <div class="comment-dialog-book">
      <strong>{{ activeCommentRecord?.bookName || '当前图书' }}</strong>
      <p>{{ activeCommentRecord?.author || '未知作者' }} · {{ activeCommentRecord?.publisher || '未知出版社' }}</p>
    </div>

    <el-form ref="commentFormRef" :model="commentForm" :rules="commentRules" label-width="72px">
      <el-form-item label="评分" prop="rating">
        <el-rate v-model="commentForm.rating" show-score />
      </el-form-item>
      <el-form-item label="评论内容">
        <el-input
          v-model="commentForm.content"
          type="textarea"
          :rows="5"
          maxlength="1000"
          show-word-limit
          placeholder="请输入你的借阅体验、内容评价或推荐理由"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="comment-dialog-footer">
        <el-button @click="commentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="commentSubmitting" @click="handleCreateComment">提交评论</el-button>
      </div>
    </template>
  </el-dialog>
</template>
