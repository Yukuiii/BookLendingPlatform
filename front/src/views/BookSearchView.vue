<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { DataAnalysis } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import { borrowBook } from '../api/borrow'
import { getBookDetail, pageBooks } from '../api/book'
import { collectBook, listMyCollectionCategories } from '../api/collection'
import { formatDateTime, formatLocation } from '../utils/book'

/**
 * 图书检索页面，负责筛选与展示图书分页数据。
 */

const loading = ref(false)
const submitError = ref('')
const books = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailError = ref('')
const detailBook = ref(null)
const favoriteDialogVisible = ref(false)
const favoriteCategoryLoading = ref(false)
const favoriteSaving = ref(false)
const favoriteCategories = ref([])

const queryForm = reactive({
  bookName: '',
  author: '',
  status: null,
})

const favoriteForm = reactive({
  bookId: null,
  bookName: '',
  collectionCategoryId: null,
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
async function handleBorrow(book) {
  const bookId = book?.bookId
  if (!bookId) {
    ElMessage.warning('图书信息不完整，暂无法借阅')
    return
  }

  try {
    const result = await borrowBook(bookId)
    const dueDate = result?.dueDate ? formatDateTime(result.dueDate) : ''
    ElMessage.success(dueDate ? `借阅成功，应还日期：${dueDate}` : '借阅成功')

    await loadBooks()

    if (detailDialogVisible.value && detailBook.value?.bookId === bookId) {
      detailBook.value = await getBookDetail(bookId)
    }
  } catch (error) {
    ElMessage.error(error.message || '借阅失败，请稍后重试')
  }
}

/**
 * 处理收藏按钮点击。
 *
 * @param {object} book 图书对象
 */
async function handleFavorite(book) {
  if (!book?.bookId) {
    ElMessage.warning('图书信息不完整，暂无法收藏')
    return
  }

  favoriteDialogVisible.value = true
  favoriteForm.bookId = book.bookId
  favoriteForm.bookName = book.bookName || '当前图书'
  favoriteForm.collectionCategoryId = null
  await loadFavoriteCategories()
}

/**
 * 查看图书详情。
 *
 * @param {object} book 图书对象
 */
async function handleViewDetail(book) {
  const bookId = book?.bookId
  if (!bookId) {
    ElMessage.warning('图书信息不完整，暂无法查看详情')
    return
  }

  detailDialogVisible.value = true
  detailLoading.value = true
  detailError.value = ''
  detailBook.value = null

  try {
    detailBook.value = await getBookDetail(bookId)
  } catch (error) {
    detailError.value = error.message || '图书详情加载失败，请稍后重试'
    ElMessage.error(detailError.value)
  } finally {
    detailLoading.value = false
  }
}

/**
 * 重置图书详情弹窗状态。
 */
function resetDetailState() {
  detailLoading.value = false
  detailError.value = ''
  detailBook.value = null
}

/**
 * 加载收藏分类列表。
 */
async function loadFavoriteCategories() {
  favoriteCategoryLoading.value = true
  try {
    favoriteCategories.value = await listMyCollectionCategories()
  } catch (error) {
    favoriteDialogVisible.value = false
    ElMessage.error(error.message || '收藏分类加载失败，请稍后重试')
  } finally {
    favoriteCategoryLoading.value = false
  }
}

/**
 * 提交收藏请求。
 */
async function submitFavorite() {
  if (!favoriteForm.bookId) {
    ElMessage.warning('图书信息不完整，暂无法收藏')
    return
  }
  if (!favoriteForm.collectionCategoryId) {
    ElMessage.warning('请选择收藏分类')
    return
  }

  favoriteSaving.value = true
  try {
    const result = await collectBook({
      bookId: favoriteForm.bookId,
      collectionCategoryId: favoriteForm.collectionCategoryId,
    })
    favoriteDialogVisible.value = false
    ElMessage.success(`已收藏到「${result?.collectionCategoryName || '所选分类'}」`)
  } catch (error) {
    ElMessage.error(error.message || '收藏失败，请稍后重试')
  } finally {
    favoriteSaving.value = false
  }
}

/**
 * 重置收藏弹窗状态。
 */
function resetFavoriteState() {
  favoriteCategoryLoading.value = false
  favoriteSaving.value = false
  favoriteForm.bookId = null
  favoriteForm.bookName = ''
  favoriteForm.collectionCategoryId = null
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
            <el-tag v-if="book.status !== 1" type="info" effect="light">已下架</el-tag>
          </div>

          <p class="book-card-author">{{ book.author || '未知作者' }} · {{ book.publisher || '未知出版社' }}</p>
          <p class="book-card-isbn">ISBN：{{ book.isbn || '暂无' }}</p>
          <p class="book-card-location">位置：{{ formatLocation(book) }}</p>
          <p class="book-card-scene">适用场景：{{ book.suitableScene || '暂无' }}</p>

          <div class="book-card-tags">
            <el-tag v-if="book.categoryName" effect="plain" size="small">{{ book.categoryName }}</el-tag>
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
            <div class="book-card-actions-left">
              <el-button type="primary" link @click="handleViewDetail(book)">查看详情</el-button>
              <el-button type="primary" link @click="handleFavorite(book)">收藏</el-button>
            </div>
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

  <el-dialog
    v-model="detailDialogVisible"
    :title="detailBook?.bookName || '图书详情'"
    width="720px"
    destroy-on-close
    @closed="resetDetailState"
  >
    <div v-loading="detailLoading">
      <el-alert v-if="detailError" :closable="false" type="error" :title="detailError" show-icon />

      <template v-else-if="detailBook">
        <div class="book-detail-head">
          <div class="book-detail-cover">
            <el-image v-if="detailBook.coverUrl" :src="detailBook.coverUrl" fit="cover" class="book-card-cover">
              <template #error>
                <div class="book-card-cover-placeholder">{{ buildCoverPlaceholder(detailBook) }}</div>
              </template>
            </el-image>
            <div v-else class="book-card-cover-placeholder">{{ buildCoverPlaceholder(detailBook) }}</div>
          </div>

          <div class="book-detail-meta">
            <h2 class="book-detail-title">{{ detailBook.bookName }}</h2>
            <p class="book-detail-subtitle">
              {{ detailBook.author || '未知作者' }} · {{ detailBook.publisher || '未知出版社' }}
            </p>

            <div class="book-detail-tags">
              <el-tag v-if="detailBook.categoryName" effect="plain" size="small">{{ detailBook.categoryName }}</el-tag>
              <el-tag v-if="detailBook.subField" effect="plain" size="small">{{ detailBook.subField }}</el-tag>
              <el-tag effect="light" size="small" :type="resolveDifficultyType(detailBook.difficultyLevel)">
                {{ resolveDifficultyLabel(detailBook.difficultyLevel) }}
              </el-tag>
            </div>
          </div>
        </div>

        <el-descriptions class="book-detail-desc" :column="2" border>
          <el-descriptions-item label="分类">{{ detailBook.categoryName || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="ISBN">{{ detailBook.isbn || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="位置">{{ formatLocation(detailBook) }}</el-descriptions-item>
          <el-descriptions-item label="出版日期">{{ detailBook.publishDate || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="适用场景">{{ detailBook.suitableScene || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="适用人群">{{ detailBook.targetAudience || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="馆藏">{{ detailBook.totalCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="可借">{{ detailBook.availableCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="借阅">{{ detailBook.borrowCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detailBook.status === 1 ? '正常' : '下架' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <h3 class="book-detail-section-title">简介</h3>
        <p class="book-detail-text">{{ detailBook.description || '暂无简介' }}</p>

        <h3 class="book-detail-section-title">目录</h3>
        <p class="book-detail-text">{{ detailBook.catalog || '暂无目录' }}</p>

        <h3 class="book-detail-section-title">作者简介</h3>
        <p class="book-detail-text">{{ detailBook.authorIntro || '暂无作者简介' }}</p>
      </template>
    </div>

    <template #footer>
      <el-button type="primary" plain :disabled="!detailBook" @click="handleFavorite(detailBook)">收藏</el-button>
      <el-button
        type="warning"
        :disabled="!detailBook || detailBook.status !== 1 || !(detailBook.availableCount > 0)"
        @click="handleBorrow(detailBook)"
      >
        立即借阅
      </el-button>
      <el-button @click="detailDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="favoriteDialogVisible"
    title="选择收藏分类"
    width="420px"
    destroy-on-close
    @closed="resetFavoriteState"
  >
    <div v-loading="favoriteCategoryLoading">
      <el-form label-width="88px">
        <el-form-item label="图书名称">
          <span class="favorite-dialog-book-name">{{ favoriteForm.bookName || '当前图书' }}</span>
        </el-form-item>
        <el-form-item label="收藏分类">
          <el-select
            v-model="favoriteForm.collectionCategoryId"
            placeholder="请选择收藏分类"
            style="width: 100%"
          >
            <el-option
              v-for="category in favoriteCategories"
              :key="category.collectionCategoryId"
              :label="`${category.categoryName}（${category.collectionCount || 0}）`"
              :value="category.collectionCategoryId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <p class="favorite-dialog-hint">如需新增分类，可前往“我的收藏”页面创建后再返回收藏。</p>
    </div>

    <template #footer>
      <el-button @click="favoriteDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="favoriteSaving" @click="submitFavorite">确认收藏</el-button>
    </template>
  </el-dialog>
</template>
