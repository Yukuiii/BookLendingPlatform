<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { pageAdminComments, updateAdminCommentStatus } from '../../api/admin'
import { COMMENT_STATUS_LABEL_MAP, COMMENT_STATUS_OPTIONS, COMMENT_STATUS_TAG_TYPE_MAP } from '../../constants/status'
import { formatDateTime } from '../../utils/book'

/**
 * 管理端评论管理页面，负责查询评论并维护评论显示状态。
 */

const loading = ref(false)
const statusSaving = ref(false)
const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const queryForm = reactive({
  username: '',
  bookName: '',
  status: null,
})

const statusOptions = COMMENT_STATUS_OPTIONS

/**
 * 页面挂载后加载评论数据。
 */
onMounted(() => {
  loadComments()
})

/**
 * 加载评论分页数据。
 */
async function loadComments() {
  loading.value = true
  try {
    const response = await pageAdminComments({
      current: currentPage.value,
      size: pageSize.value,
      username: queryForm.username.trim(),
      bookName: queryForm.bookName.trim(),
      status: queryForm.status,
    })
    records.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error(error.message || '评论列表加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 执行筛选查询。
 */
function handleSearch() {
  currentPage.value = 1
  loadComments()
}

/**
 * 重置筛选条件。
 */
function handleReset() {
  queryForm.username = ''
  queryForm.bookName = ''
  queryForm.status = null
  currentPage.value = 1
  loadComments()
}

/**
 * 切换评论状态。
 *
 * @param {object} row 评论记录
 * @param {number} status 评论状态
 */
async function handleStatusChange(row, status) {
  const previousStatus = row?.status
  if (!row?.commentId || status == null || status === previousStatus) {
    return
  }

  statusSaving.value = true
  try {
    const result = await updateAdminCommentStatus(row.commentId, { status })
    row.status = result.status
    ElMessage.success('评论状态更新成功')
  } catch (error) {
    row.status = previousStatus
    ElMessage.error(error.message || '评论状态更新失败，请稍后重试')
  } finally {
    statusSaving.value = false
  }
}

/**
 * 切换分页页码。
 *
 * @param {number} page 页码
 */
function handlePageChange(page) {
  currentPage.value = page
  loadComments()
}

/**
 * 修改每页展示条数。
 *
 * @param {number} size 每页条数
 */
function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  loadComments()
}

/**
 * 获取评论状态文案。
 *
 * @param {number} status 评论状态
 * @returns {string} 状态文案
 */
function resolveCommentStatusLabel(status) {
  return COMMENT_STATUS_LABEL_MAP[Number(status)] || '未知'
}

/**
 * 获取评论状态标签类型。
 *
 * @param {number} status 评论状态
 * @returns {string} 标签类型
 */
function resolveCommentStatusType(status) {
  return COMMENT_STATUS_TAG_TYPE_MAP[Number(status)] || 'info'
}
</script>

<template>
  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>评论管理</strong>
          <p>查看用户评论，并维护评论的审核通过、隐藏与审核中状态</p>
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
    <el-table v-loading="loading" :data="records" style="width: 100%" :empty-text="loading ? '加载中...' : '暂无评论数据'">
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="realName" label="真实姓名" min-width="120" />
      <el-table-column prop="bookName" label="书名" min-width="180" />
      <el-table-column prop="isbn" label="ISBN" min-width="150" />
      <el-table-column label="评分" width="180" align="center">
        <template #default="{ row }">
          <el-rate :model-value="row.rating || 0" disabled show-score text-color="#f59e0b" />
        </template>
      </el-table-column>
      <el-table-column label="评论内容" min-width="320">
        <template #default="{ row }">
          <p class="comment-content">{{ row.content || '暂无评论内容' }}</p>
        </template>
      </el-table-column>
      <el-table-column label="评论时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="resolveCommentStatusType(row.status)" effect="light">{{ resolveCommentStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态维护" width="148" align="center" fixed="right">
        <template #default="{ row }">
          <el-select
            :model-value="row.status"
            :disabled="statusSaving"
            size="small"
            style="width: 120px"
            @change="(value) => handleStatusChange(row, value)"
          >
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
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
