<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { cancelReservation, pageMyReservations } from '../api/borrow'
import {
  RESERVATION_STATUS,
  RESERVATION_STATUS_LABEL_MAP,
  RESERVATION_STATUS_OPTIONS,
  RESERVATION_STATUS_TAG_TYPE_MAP,
} from '../constants/status'
import { formatDateTime } from '../utils/book'

/**
 * 我的预约页面，负责展示当前用户的预约记录。
 */

const loading = ref(false)
const submitError = ref('')
const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const queryForm = reactive({
  status: null,
})

const statusOptions = RESERVATION_STATUS_OPTIONS

/**
 * 页面挂载后初始化预约记录。
 */
onMounted(() => {
  loadReservations()
})

/**
 * 加载预约记录分页数据。
 */
async function loadReservations() {
  loading.value = true
  submitError.value = ''

  try {
    const response = await pageMyReservations({
      current: currentPage.value,
      size: pageSize.value,
      status: queryForm.status,
    })

    records.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    submitError.value = error.message || '预约记录加载失败，请稍后重试'
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
  loadReservations()
}

/**
 * 重置筛选条件并重新加载数据。
 */
function handleReset() {
  queryForm.status = null
  currentPage.value = 1
  loadReservations()
}

/**
 * 切换分页页码。
 *
 * @param {number} page 页码
 */
function handlePageChange(page) {
  currentPage.value = page
  loadReservations()
}

/**
 * 修改每页展示条数。
 *
 * @param {number} size 每页条数
 */
function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  loadReservations()
}

/**
 * 处理取消预约操作。
 *
 * @param {object} record 预约记录对象
 */
async function handleCancel(record) {
  const reservationId = record?.reservationId
  if (!reservationId) {
    ElMessage.warning('预约记录不完整，暂无法取消')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认取消预约《${record.bookName || '当前图书'}》吗？取消后排队位置将释放。`,
      '取消预约确认',
      {
        type: 'warning',
        confirmButtonText: '确认取消',
        cancelButtonText: '再想想',
      },
    )
  } catch {
    return
  }

  try {
    await cancelReservation(reservationId)
    ElMessage.success('预约已取消')
    await loadReservations()
  } catch (error) {
    ElMessage.error(error.message || '取消预约失败，请稍后重试')
  }
}

/**
 * 生成图书封面占位文本。
 *
 * @param {object} record 预约记录对象
 * @returns {string} 占位文本
 */
function buildCoverPlaceholder(record) {
  return (record?.bookName || '图书').slice(0, 2)
}

/**
 * 获取预约状态文案。
 *
 * @param {number} status 预约状态
 * @returns {string} 状态文案
 */
function resolveReservationStatusLabel(status) {
  return RESERVATION_STATUS_LABEL_MAP[Number(status)] || '未知'
}

/**
 * 获取预约状态标签类型。
 *
 * @param {number} status 预约状态
 * @returns {string} 标签类型
 */
function resolveReservationStatusType(status) {
  return RESERVATION_STATUS_TAG_TYPE_MAP[Number(status)] || 'info'
}

/**
 * 判断当前记录是否可以取消预约。
 *
 * @param {number} status 预约状态
 * @returns {boolean} 是否可取消
 */
function canCancel(status) {
  return Number(status) === RESERVATION_STATUS.WAITING
}
</script>

<template>
  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>我的预约</strong>
          <p>查看与管理你的图书预约记录，排队中的预约可随时取消</p>
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
      :empty-text="loading ? '加载中...' : '暂无预约记录'"
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
              <p class="borrow-book-extra">分类：{{ row.categoryName || '暂无' }}</p>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="排队位置" width="100" align="center">
        <template #default="{ row }">
          <span v-if="Number(row.status) === RESERVATION_STATUS.WAITING" class="reservation-queue-position">
            第 {{ row.queuePosition ?? '--' }} 位
          </span>
          <span v-else>--</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" effect="light" :type="resolveReservationStatusType(row.status)">
            {{ resolveReservationStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="预约时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>

      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
      </el-table-column>

      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="canCancel(row.status)" type="danger" link @click="handleCancel(row)">取消预约</el-button>
          <span v-else class="borrow-action-placeholder">--</span>
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

<style scoped>
.reservation-queue-position {
  font-weight: 600;
  color: var(--el-color-warning);
}
</style>
