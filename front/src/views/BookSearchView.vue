<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { DataAnalysis } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import { pageBooks } from '../api/book'

/**
 * 图书检索页面，负责筛选与展示图书分页数据。
 */

const loading = ref(false)
const submitError = ref('')
const books = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)

const queryForm = reactive({
  bookName: '',
  author: '',
  status: null,
})

const statusOptions = [
  { label: '在馆可借', value: 1 },
  { label: '已下架', value: 0 },
]

const statisticCards = computed(() => {
  const availableCopies = books.value.reduce((totalCount, currentBook) => totalCount + (currentBook.availableCount || 0), 0)
  const currentPageBorrowCount = books.value.reduce((totalCount, currentBook) => totalCount + (currentBook.borrowCount || 0), 0)
  const onShelfCount = books.value.filter((currentBook) => currentBook.status === 1).length

  return [
    { label: '图书总量', value: total.value, helper: '匹配当前筛选条件的图书总数' },
    { label: '本页在架', value: onShelfCount, helper: '当前页面中可检索到的上架图书' },
    { label: '可借册数', value: availableCopies, helper: '当前页图书可借库存总和' },
    { label: '借阅热度', value: currentPageBorrowCount, helper: '当前页图书累计借阅次数' },
  ]
})

/**
 * 页面挂载后初始化图书数据。
 */
onMounted(() => {
  loadBooks()
})

/**
 * 加载图书分页数据。
 */
async function loadBooks() {
  loading.value = true
  submitError.value = ''

  try {
    const response = await pageBooks({
      current: currentPage.value,
      size: pageSize.value,
      bookName: queryForm.bookName.trim(),
      author: queryForm.author.trim(),
      status: queryForm.status,
    })

    books.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    submitError.value = error.message || '图书列表加载失败，请稍后重试'
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
  loadBooks()
}

/**
 * 重置筛选条件并重新加载数据。
 */
function handleReset() {
  queryForm.bookName = ''
  queryForm.author = ''
  queryForm.status = null
  currentPage.value = 1
  loadBooks()
}

/**
 * 切换分页页码。
 *
 * @param {number} page 页码
 */
function handlePageChange(page) {
  currentPage.value = page
  loadBooks()
}

/**
 * 修改每页展示条数。
 *
 * @param {number} size 每页条数
 */
function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  loadBooks()
}

/**
 * 处理借阅按钮点击。
 *
 * @param {object} book 图书对象
 */
function handleBorrow(book) {
  ElMessage.success(`已为你准备《${book.bookName}》的借阅入口，后续可继续接借阅接口`)
}

/**
 * 生成图书封面占位文本。
 *
 * @param {object} book 图书对象
 * @returns {string} 占位文本
 */
function buildCoverPlaceholder(book) {
  return (book.bookName || '图书').slice(0, 2)
}

/**
 * 获取图书展示领域。
 *
 * @param {object} book 图书对象
 * @returns {string} 领域名称
 */
function resolveSubField(book) {
  return book.subField || '综合领域'
}

/**
 * 获取难度标签文本。
 *
 * @param {number} difficultyLevel 难度等级
 * @returns {string} 难度文案
 */
function resolveDifficultyLabel(difficultyLevel) {
  if (difficultyLevel === 1) {
    return '入门'
  }
  if (difficultyLevel === 2) {
    return '进阶'
  }
  if (difficultyLevel === 3) {
    return '专家'
  }
  return '未分级'
}

/**
 * 获取难度标签类型。
 *
 * @param {number} difficultyLevel 难度等级
 * @returns {string} 标签类型
 */
function resolveDifficultyType(difficultyLevel) {
  if (difficultyLevel === 1) {
    return 'success'
  }
  if (difficultyLevel === 2) {
    return 'warning'
  }
  if (difficultyLevel === 3) {
    return 'danger'
  }
  return 'info'
}
</script>

<template>
  <el-row :gutter="16" class="home-stat-row">
    <el-col v-for="card in statisticCards" :key="card.label" :xs="24" :sm="12" :xl="6">
      <el-card class="home-stat-card" shadow="hover">
        <div class="home-stat-card-head">
          <span class="home-stat-card-label">{{ card.label }}</span>
          <el-icon><DataAnalysis /></el-icon>
        </div>
        <strong class="home-stat-card-value">{{ card.value }}</strong>
        <p class="home-stat-card-helper">{{ card.helper }}</p>
      </el-card>
    </el-col>
  </el-row>

  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>图书检索</strong>
          <p>按书名、作者和状态快速筛选馆藏图书</p>
        </div>
        <el-button type="primary" plain @click="handleSearch">刷新列表</el-button>
      </div>
    </template>

    <el-form class="home-filter-form" :model="queryForm" label-width="70px" @submit.prevent>
      <el-form-item label="书名">
        <el-input v-model="queryForm.bookName" clearable placeholder="请输入图书名称" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="作者">
        <el-input v-model="queryForm.author" clearable placeholder="请输入作者名称" @keyup.enter="handleSearch" />
      </el-form-item>
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

  <div v-loading="loading" class="home-book-grid">
    <template v-if="books.length">
      <el-card v-for="book in books" :key="book.bookId" class="book-card" shadow="hover">
        <div class="book-card-cover-wrap">
          <el-image v-if="book.coverUrl" :src="book.coverUrl" fit="cover" class="book-card-cover">
            <template #error>
              <div class="book-card-cover-placeholder">{{ buildCoverPlaceholder(book) }}</div>
            </template>
          </el-image>
          <div v-else class="book-card-cover-placeholder">{{ buildCoverPlaceholder(book) }}</div>
        </div>

        <div class="book-card-content">
          <div class="book-card-head">
            <h3 class="book-card-title">{{ book.bookName }}</h3>
            <el-tag :type="book.status === 1 ? 'success' : 'info'" effect="light">
              {{ book.status === 1 ? '在馆可借' : '已下架' }}
            </el-tag>
          </div>

          <p class="book-card-author">{{ book.author || '未知作者' }} · {{ book.publisher || '未知出版社' }}</p>

          <div class="book-card-tags">
            <el-tag effect="plain" size="small">{{ resolveSubField(book) }}</el-tag>
            <el-tag effect="light" size="small" :type="resolveDifficultyType(book.difficultyLevel)">
              {{ resolveDifficultyLabel(book.difficultyLevel) }}
            </el-tag>
          </div>

          <p class="book-card-desc">{{ book.description || '暂无图书简介，后续可继续补详情接口。' }}</p>

          <div class="book-card-meta">
            <span>馆藏：{{ book.totalCount || 0 }}</span>
            <span>可借：{{ book.availableCount || 0 }}</span>
            <span>借阅：{{ book.borrowCount || 0 }}</span>
          </div>

          <div class="book-card-actions">
            <el-button type="primary" link>查看详情</el-button>
            <el-button type="warning" :disabled="book.status !== 1 || !(book.availableCount > 0)" @click="handleBorrow(book)">
              立即借阅
            </el-button>
          </div>
        </div>
      </el-card>
    </template>

    <el-empty v-else description="暂无匹配图书数据" />
  </div>

  <div class="home-pagination">
    <el-pagination
      background
      layout="total, sizes, prev, pager, next"
      :current-page="currentPage"
      :page-size="pageSize"
      :page-sizes="[8, 12, 20]"
      :total="total"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

