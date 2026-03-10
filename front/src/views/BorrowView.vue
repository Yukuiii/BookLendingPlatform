<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { pageMyBorrowRecords } from '../api/borrow'
import { formatDateTime, formatLocation } from '../utils/book'

/**
 * 我的借阅页面，负责展示当前用户的借阅记录。
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

const statusOptions = [
  { label: '借阅中', value: 1 },
  { label: '已归还', value: 2 },
  { label: '超期', value: 3 },
]

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
  if (status === 1) {
    return '借阅中'
  }
  if (status === 2) {
    return '已归还'
  }
  if (status === 3) {
    return '超期'
  }
  return '未知'
}

/**
 * 获取借阅状态标签类型。
 *
 * @param {number} status 借阅状态
 * @returns {string} 标签类型
 */
function resolveBorrowStatusType(status) {
  if (status === 1) {
    return 'success'
  }
  if (status === 2) {
    return 'info'
  }
  if (status === 3) {
    return 'danger'
  }
  return 'warning'
}
</script>

<template>
  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>我的借阅</strong>
          <p>查看与筛选你的借阅记录（借阅中、已归还、超期）</p>
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

      <el-table-column label="借阅时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.borrowDate) }}</template>
      </el-table-column>

      <el-table-column label="应还时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.dueDate) }}</template>
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

      <el-table-column label="罚款" width="90" align="center">
        <template #default="{ row }">{{ row.fineAmount ?? 0 }}</template>
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
