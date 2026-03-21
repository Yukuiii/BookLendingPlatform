<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { createAdminBook, listAdminBookCategories, pageAdminBooks, updateAdminBook } from '../../api/admin'
import { getBookDetail } from '../../api/book'
import { BOOK_STATUS, BOOK_STATUS_LABEL_MAP, BOOK_STATUS_OPTIONS, BOOK_STATUS_TAG_TYPE_MAP } from '../../constants/status'
import { formatLocation } from '../../utils/book'

/**
 * 管理端图书管理页面，负责查询、新增与修改图书资料。
 */

const loading = ref(false)
const saving = ref(false)
const records = ref([])
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const editingBookId = ref(null)

const queryForm = reactive({
  bookName: '',
  author: '',
  status: null,
})

const bookForm = reactive({
  isbn: '',
  bookName: '',
  author: '',
  publisher: '',
  publishDate: '',
  categoryId: null,
  subField: '',
  difficultyLevel: 1,
  suitableScene: '',
  coverUrl: '',
  description: '',
  catalog: '',
  authorIntro: '',
  targetAudience: '',
  totalCount: 0,
  availableCount: 0,
  status: BOOK_STATUS.NORMAL,
})

const difficultyOptions = [
  { label: '入门', value: 1 },
  { label: '进阶', value: 2 },
  { label: '专家', value: 3 },
]

const statusOptions = BOOK_STATUS_OPTIONS

const bookFormRef = ref(null)
const bookRules = {
  isbn: [{ required: true, message: '请输入 ISBN', trigger: 'blur' }],
  bookName: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  publisher: [{ required: true, message: '请输入出版社', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

/**
 * 页面挂载后初始化图书与分类数据。
 */
onMounted(async () => {
  await loadCategories()
  await loadBooks()
})

/**
 * 加载图书分类。
 */
async function loadCategories() {
  try {
    categories.value = await listAdminBookCategories()
  } catch (error) {
    ElMessage.error(error.message || '图书分类加载失败，请稍后重试')
  }
}

/**
 * 加载图书分页数据。
 */
async function loadBooks() {
  loading.value = true
  try {
    const response = await pageAdminBooks({
      current: currentPage.value,
      size: pageSize.value,
      bookName: queryForm.bookName.trim(),
      author: queryForm.author.trim(),
      status: queryForm.status,
    })
    records.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error(error.message || '图书列表加载失败，请稍后重试')
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
 * 重置筛选条件。
 */
function handleReset() {
  queryForm.bookName = ''
  queryForm.author = ''
  queryForm.status = null
  currentPage.value = 1
  loadBooks()
}

/**
 * 打开新增图书弹窗。
 */
function openCreateDialog() {
  editingBookId.value = null
  resetBookForm()
  dialogVisible.value = true
}

/**
 * 打开编辑图书弹窗。
 *
 * @param {object} row 图书数据
 */
async function openEditDialog(row) {
  editingBookId.value = row.bookId
  dialogVisible.value = true

  try {
    const detail = await getBookDetail(row.bookId)
    bookForm.isbn = detail.isbn || ''
    bookForm.bookName = detail.bookName || ''
    bookForm.author = detail.author || ''
    bookForm.publisher = detail.publisher || ''
    bookForm.publishDate = detail.publishDate || ''
    bookForm.categoryId = detail.categoryId ?? null
    bookForm.subField = detail.subField || ''
    bookForm.difficultyLevel = detail.difficultyLevel ?? 1
    bookForm.suitableScene = detail.suitableScene || ''
    bookForm.coverUrl = detail.coverUrl || ''
    bookForm.description = detail.description || ''
    bookForm.catalog = detail.catalog || ''
    bookForm.authorIntro = detail.authorIntro || ''
    bookForm.targetAudience = detail.targetAudience || ''
    bookForm.totalCount = detail.totalCount ?? 0
    bookForm.availableCount = detail.availableCount ?? 0
    bookForm.status = detail.status ?? BOOK_STATUS.NORMAL
  } catch (error) {
    dialogVisible.value = false
    ElMessage.error(error.message || '图书详情加载失败，请稍后重试')
  }
}

/**
 * 保存图书。
 */
async function handleSave() {
  try {
    await bookFormRef.value?.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    const payload = {
      isbn: bookForm.isbn.trim(),
      bookName: bookForm.bookName.trim(),
      author: bookForm.author.trim(),
      publisher: bookForm.publisher.trim(),
      publishDate: bookForm.publishDate || null,
      categoryId: bookForm.categoryId,
      subField: bookForm.subField.trim() || null,
      difficultyLevel: Number(bookForm.difficultyLevel),
      suitableScene: bookForm.suitableScene.trim() || null,
      coverUrl: bookForm.coverUrl.trim() || null,
      description: bookForm.description.trim() || null,
      catalog: bookForm.catalog.trim() || null,
      authorIntro: bookForm.authorIntro.trim() || null,
      targetAudience: bookForm.targetAudience.trim() || null,
      totalCount: Number(bookForm.totalCount),
      availableCount: Number(bookForm.availableCount),
      status: Number(bookForm.status),
    }

    if (editingBookId.value) {
      await updateAdminBook(editingBookId.value, payload)
    } else {
      await createAdminBook(payload)
    }

    dialogVisible.value = false
    await loadBooks()
    ElMessage.success(editingBookId.value ? '图书信息修改成功' : '图书新增成功')
  } catch (error) {
    ElMessage.error(error.message || '图书保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

/**
 * 重置图书表单。
 */
function resetBookForm() {
  bookForm.isbn = ''
  bookForm.bookName = ''
  bookForm.author = ''
  bookForm.publisher = ''
  bookForm.publishDate = ''
  bookForm.categoryId = null
  bookForm.subField = ''
  bookForm.difficultyLevel = 1
  bookForm.suitableScene = ''
  bookForm.coverUrl = ''
  bookForm.description = ''
  bookForm.catalog = ''
  bookForm.authorIntro = ''
  bookForm.targetAudience = ''
  bookForm.totalCount = 0
  bookForm.availableCount = 0
  bookForm.status = 1
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
</script>

<template>
  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>图书管理</strong>
          <p>维护图书基础资料、库存数量与上架状态</p>
        </div>
        <el-button type="primary" @click="openCreateDialog">新增图书</el-button>
      </div>
    </template>

    <el-form class="home-filter-form" :model="queryForm" label-width="70px" @submit.prevent>
      <el-form-item label="书名">
        <el-input v-model="queryForm.bookName" clearable placeholder="请输入书名" />
      </el-form-item>
      <el-form-item label="作者">
        <el-input v-model="queryForm.author" clearable placeholder="请输入作者" />
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
    <el-table v-loading="loading" :data="records" style="width: 100%" :empty-text="loading ? '加载中...' : '暂无图书数据'">
      <el-table-column prop="bookName" label="书名" min-width="180" />
      <el-table-column prop="author" label="作者" min-width="120" />
      <el-table-column prop="categoryName" label="分类" min-width="120" />
      <el-table-column prop="isbn" label="ISBN" min-width="150" />
      <el-table-column label="库存" width="150">
        <template #default="{ row }">{{ row.availableCount ?? 0 }} / {{ row.totalCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="位置" min-width="160">
        <template #default="{ row }">{{ formatLocation(row) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="BOOK_STATUS_TAG_TYPE_MAP[Number(row.status)] || 'info'" effect="light">
            {{ BOOK_STATUS_LABEL_MAP[Number(row.status)] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
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

  <el-dialog v-model="dialogVisible" :title="editingBookId ? '编辑图书' : '新增图书'" width="920px" destroy-on-close>
    <el-form ref="bookFormRef" class="admin-form-grid admin-form-grid--wide" :model="bookForm" :rules="bookRules" label-width="88px">
      <el-form-item label="ISBN" prop="isbn">
        <el-input v-model="bookForm.isbn" />
      </el-form-item>
      <el-form-item label="书名" prop="bookName">
        <el-input v-model="bookForm.bookName" />
      </el-form-item>
      <el-form-item label="作者" prop="author">
        <el-input v-model="bookForm.author" />
      </el-form-item>
      <el-form-item label="出版社" prop="publisher">
        <el-input v-model="bookForm.publisher" />
      </el-form-item>
      <el-form-item label="出版日期">
        <el-date-picker v-model="bookForm.publishDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择出版日期" />
      </el-form-item>
      <el-form-item label="图书分类" prop="categoryId">
        <el-select v-model="bookForm.categoryId" placeholder="请选择图书分类">
          <el-option v-for="item in categories" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
        </el-select>
      </el-form-item>
      <el-form-item label="子领域">
        <el-input v-model="bookForm.subField" />
      </el-form-item>
      <el-form-item label="难度等级">
        <el-select v-model="bookForm.difficultyLevel">
          <el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="适用场景">
        <el-input v-model="bookForm.suitableScene" />
      </el-form-item>
      <el-form-item label="适用人群">
        <el-input v-model="bookForm.targetAudience" />
      </el-form-item>
      <el-form-item label="馆藏总数">
        <el-input-number v-model="bookForm.totalCount" :min="0" controls-position="right" />
      </el-form-item>
      <el-form-item label="可借数量">
        <el-input-number v-model="bookForm.availableCount" :min="0" controls-position="right" />
      </el-form-item>
      <el-form-item label="封面地址" class="admin-form-item--full">
        <el-input v-model="bookForm.coverUrl" />
      </el-form-item>
      <el-form-item label="图书状态">
        <el-radio-group v-model="bookForm.status">
          <el-radio-button :label="1">正常</el-radio-button>
          <el-radio-button :label="0">下架</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="简介" class="admin-form-item--full">
        <el-input v-model="bookForm.description" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="目录" class="admin-form-item--full">
        <el-input v-model="bookForm.catalog" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item label="作者简介" class="admin-form-item--full">
        <el-input v-model="bookForm.authorIntro" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>
