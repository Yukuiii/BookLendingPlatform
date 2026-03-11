<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { pageMyComments } from '../api/comment'
import { formatDateTime } from '../utils/book'

/**
 * 我的评论页面，负责展示当前用户的图书评论记录。
 */

const loading = ref(false)
const submitError = ref('')
const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

/**
 * 页面挂载后加载评论数据。
 */
onMounted(() => {
  loadComments()
})

/**
 * 加载我的评论分页数据。
 */
async function loadComments() {
  loading.value = true
  submitError.value = ''

  try {
    const response = await pageMyComments({
      current: currentPage.value,
      size: pageSize.value,
    })
    records.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    submitError.value = error.message || '评论列表加载失败，请稍后重试'
    ElMessage.error(submitError.value)
  } finally {
    loading.value = false
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
 * 生成图书封面占位文本。
 *
 * @param {object} record 评论记录
 * @returns {string} 占位文本
 */
function buildCoverPlaceholder(record) {
  return (record?.bookName || '图书').slice(0, 2)
}

/**
 * 获取评论状态文案。
 *
 * @param {number} status 评论状态
 * @returns {string} 状态文案
 */
function resolveCommentStatusLabel(status) {
  if (status === 0) {
    return '已隐藏'
  }
  if (status === 1) {
    return '正常'
  }
  if (status === 2) {
    return '审核中'
  }
  return '未知'
}

/**
 * 获取评论状态标签类型。
 *
 * @param {number} status 评论状态
 * @returns {string} 标签类型
 */
function resolveCommentStatusType(status) {
  if (status === 0) {
    return 'info'
  }
  if (status === 1) {
    return 'success'
  }
  if (status === 2) {
    return 'warning'
  }
  return 'info'
}
</script>

<template>
  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>我的评论</strong>
          <p>查看你对已归还图书提交过的评论记录</p>
        </div>
        <el-button type="primary" plain @click="loadComments">刷新列表</el-button>
      </div>
    </template>
  </el-card>

  <el-alert v-if="submitError" class="home-alert" :closable="false" type="warning" :title="submitError" show-icon />

  <el-card shadow="never">
    <el-table
      v-loading="loading"
      :data="records"
      style="width: 100%"
      :empty-text="loading ? '加载中...' : '暂无评论记录'"
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

      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" effect="light" :type="resolveCommentStatusType(row.status)">
            {{ resolveCommentStatusLabel(row.status) }}
          </el-tag>
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
