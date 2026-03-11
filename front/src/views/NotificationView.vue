<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { markMyNotificationRead, pageMyNotifications } from '../api/notification'
import { formatDateTime } from '../utils/book'

/**
 * 信息通知页面，负责展示当前用户的通知记录。
 */

const router = useRouter()

const loading = ref(false)
const submitError = ref('')
const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const readingNotificationId = ref(null)

const queryForm = reactive({
  readStatus: null,
})

const readStatusOptions = [
  { label: '未读', value: 0 },
  { label: '已读', value: 1 },
]

/**
 * 页面挂载后加载通知数据。
 */
onMounted(() => {
  loadNotifications()
})

/**
 * 加载通知分页数据。
 */
async function loadNotifications() {
  loading.value = true
  submitError.value = ''

  try {
    const response = await pageMyNotifications({
      current: currentPage.value,
      size: pageSize.value,
      readStatus: queryForm.readStatus,
    })
    records.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    submitError.value = error.message || '通知列表加载失败，请稍后重试'
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
  loadNotifications()
}

/**
 * 重置筛选条件。
 */
function handleReset() {
  queryForm.readStatus = null
  currentPage.value = 1
  loadNotifications()
}

/**
 * 切换分页页码。
 *
 * @param {number} page 页码
 */
function handlePageChange(page) {
  currentPage.value = page
  loadNotifications()
}

/**
 * 修改每页展示条数。
 *
 * @param {number} size 每页条数
 */
function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  loadNotifications()
}

/**
 * 获取通知类型文案。
 *
 * @param {number} notificationType 通知类型
 * @returns {string} 文案
 */
function resolveNotificationTypeLabel(notificationType) {
  if (notificationType === 1) {
    return '超期提醒'
  }
  return '系统通知'
}

/**
 * 获取已读状态文案。
 *
 * @param {number} readStatus 已读状态
 * @returns {string} 文案
 */
function resolveReadStatusLabel(readStatus) {
  return Number(readStatus) === 1 ? '已读' : '未读'
}

/**
 * 获取已读状态标签类型。
 *
 * @param {number} readStatus 已读状态
 * @returns {string} 标签类型
 */
function resolveReadStatusType(readStatus) {
  return Number(readStatus) === 1 ? 'info' : 'danger'
}

/**
 * 标记通知为已读。
 *
 * @param {object} notification 通知对象
 */
async function handleRead(notification) {
  const notificationId = notification?.notificationId
  if (!notificationId || Number(notification?.readStatus) === 1) {
    return
  }

  readingNotificationId.value = notificationId
  try {
    const result = await markMyNotificationRead(notificationId)
    notification.readStatus = result?.readStatus ?? 1
    notification.readTime = result?.readTime || notification.readTime
    ElMessage.success('通知已标记为已读')
  } catch (error) {
    ElMessage.error(error.message || '通知状态更新失败，请稍后重试')
  } finally {
    readingNotificationId.value = null
  }
}

/**
 * 跳转到借阅页面查看详情。
 *
 * @param {object} notification 通知对象
 */
function goBorrowPage(notification) {
  if (notification?.businessType === 'BORROW_RECORD') {
    router.push({ name: 'borrow' })
  }
}
</script>

<template>
  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>信息通知</strong>
          <p>查看系统发送给你的借阅提醒通知</p>
        </div>
        <el-button type="primary" plain @click="handleSearch">刷新列表</el-button>
      </div>
    </template>

    <el-form class="home-filter-form" :model="queryForm" label-width="70px" @submit.prevent>
      <el-form-item label="状态">
        <el-select v-model="queryForm.readStatus" clearable placeholder="全部状态">
          <el-option v-for="option in readStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
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
      :empty-text="loading ? '加载中...' : '暂无通知记录'"
    >
      <el-table-column label="通知类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag type="warning" effect="light">{{ resolveNotificationTypeLabel(row.notificationType) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="title" label="标题" min-width="180" />

      <el-table-column label="通知内容" min-width="360">
        <template #default="{ row }">
          <p class="notification-content">{{ row.content || '暂无通知内容' }}</p>
        </template>
      </el-table-column>

      <el-table-column label="发送时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="resolveReadStatusType(row.readStatus)" effect="light">{{ resolveReadStatusLabel(row.readStatus) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="阅读时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.readTime) }}</template>
      </el-table-column>

      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <div class="notification-action-group">
            <el-button
              type="primary"
              link
              :disabled="Number(row.readStatus) === 1"
              :loading="readingNotificationId === row.notificationId"
              @click="handleRead(row)"
            >
              标记已读
            </el-button>
            <el-button v-if="row.businessType === 'BORROW_RECORD'" type="primary" link @click="goBorrowPage(row)">查看借阅</el-button>
          </div>
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
</template>
