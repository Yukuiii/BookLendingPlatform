<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { approveAdminBorrowRecord, pageAdminBorrowRecords, returnAdminBorrowRecord } from '../../api/admin'
import { formatDateTime } from '../../utils/book'

/**
 * 管理端借阅管理页面，负责查询借阅记录并执行管理员归还。
 */

const loading = ref(false)
const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const queryForm = reactive({
  username: '',
  bookName: '',
  status: null,
})

const statusOptions = [
  { label: '借阅中', value: 1 },
  { label: '已归还', value: 2 },
  { label: '超期', value: 3 },
  { label: '审核中', value: 4 },
]

/**
 * 页面挂载后加载借阅记录。
 */
onMounted(() => {
  loadBorrowRecords()
})

/**
 * 加载借阅记录分页数据。
 */
async function loadBorrowRecords() {
  loading.value = true
  try {
    const response = await pageAdminBorrowRecords({
      current: currentPage.value,
      size: pageSize.value,
      username: queryForm.username.trim(),
      bookName: queryForm.bookName.trim(),
      status: queryForm.status,
    })
    records.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error(error.message || '借阅记录加载失败，请稍后重试')
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
 * 重置筛选条件。
 */
function handleReset() {
  queryForm.username = ''
  queryForm.bookName = ''
  queryForm.status = null
  currentPage.value = 1
  loadBorrowRecords()
}

/**
 * 管理员归还图书。
 *
 * @param {object} row 借阅记录
 */
async function handleReturn(row) {
  try {
    await ElMessageBox.confirm(`确认归还《${row.bookName || '当前图书'}》吗？`, '管理员归还', {
      type: 'warning',
      confirmButtonText: '确认归还',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  try {
    await returnAdminBorrowRecord(row.borrowId)
    await loadBorrowRecords()
    ElMessage.success('归还操作已完成')
  } catch (error) {
    ElMessage.error(error.message || '归还失败，请稍后重试')
  }
}

/**
 * 审核通过借阅申请。
 *
 * @param {object} row 借阅记录
 */
async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确认通过《${row.bookName || '当前图书'}》的借阅申请吗？`, '借阅审核', {
      type: 'warning',
      confirmButtonText: '审核通过',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  try {
    const result = await approveAdminBorrowRecord(row.borrowId)
    const dueDate = result?.dueDate ? formatDateTime(result.dueDate) : '暂无'
    await loadBorrowRecords()
    ElMessage.success(`审核通过成功，应还日期：${dueDate}`)
  } catch (error) {
    ElMessage.error(error.message || '审核失败，请稍后重试')
  }
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
  if (status === 4) {
    return '审核中'
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
  if (status === 4) {
    return 'warning'
  }
  return 'warning'
}
</script>

<template>
  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>借阅管理</strong>
          <p>查看全部借阅记录，并对审核中的申请执行通过审核，对借阅中或超期记录执行归还</p>
        </div>
        <el-button type="primary" plain @click="handleSearch">刷新列表</el-button>
      </div>
    </template>

    <el-form class="home-filter-form" :model="queryForm" label-width="70px" @submit.prevent>
      <el-form-item label="用户名">
        <el-input v-model="queryForm.username" clearable placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="书名">
        <el-input v-model="queryForm.bookName" clearable placeholder="请输入书名" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" clearable placeholder="全部状态">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item class="home-filter-actions">
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <el-card shadow="never">
    <el-table v-loading="loading" :data="records" style="width: 100%" :empty-text="loading ? '加载中...' : '暂无借阅数据'">
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="realName" label="真实姓名" min-width="120" />
      <el-table-column prop="bookName" label="书名" min-width="180" />
      <el-table-column prop="isbn" label="ISBN" min-width="150" />
      <el-table-column label="申请/借阅时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.borrowDate) }}</template>
      </el-table-column>
      <el-table-column label="应还时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.dueDate) }}</template>
      </el-table-column>
      <el-table-column label="归还时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.returnDate) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="resolveBorrowStatusType(row.status)" effect="light">{{ resolveBorrowStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="超期" width="90" align="center">
        <template #default="{ row }">{{ row.overdueDays > 0 ? `${row.overdueDays}天` : '--' }}</template>
      </el-table-column>
      <el-table-column label="罚款" width="90" align="center">
        <template #default="{ row }">{{ row.fineAmount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="操作" width="132" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 4" type="warning" link @click="handleApprove(row)">审核通过</el-button>
          <el-button v-else-if="row.status === 1 || row.status === 3" type="primary" link @click="handleReturn(row)">归还</el-button>
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
