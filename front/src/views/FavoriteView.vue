<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import {
  createCollectionCategory,
  listMyCollectionCategories,
  pageMyCollections,
  removeCollection,
  updateCollectionCategory,
} from '../api/collection'
import { formatDateTime, formatLocation } from '../utils/book'

/**
 * 我的收藏页面，负责管理收藏图书与收藏分类。
 */

const loading = ref(false)
const categoryLoading = ref(false)
const categoryDialogVisible = ref(false)
const savingCategory = ref(false)
const records = ref([])
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const activeCategoryTab = ref('all')

const categoryForm = reactive({
  categoryName: '',
})

const categoryRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

const categoryFormRef = ref(null)

/**
 * 页面挂载后初始化收藏数据。
 */
onMounted(async () => {
  await loadCategories()
  await loadCollections()
})

const categoryOptions = computed(() => {
  return categories.value.map((category) => ({
    label: category.categoryName,
    value: category.collectionCategoryId,
  }))
})

/**
 * 加载收藏分类。
 */
async function loadCategories() {
  categoryLoading.value = true
  try {
    categories.value = await listMyCollectionCategories()
  } catch (error) {
    ElMessage.error(error.message || '收藏分类加载失败，请稍后重试')
  } finally {
    categoryLoading.value = false
  }
}

/**
 * 加载收藏分页数据。
 */
async function loadCollections() {
  loading.value = true
  try {
    const response = await pageMyCollections({
      current: currentPage.value,
      size: pageSize.value,
      collectionCategoryId: resolveActiveCategoryId(),
    })
    records.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error(error.message || '收藏列表加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 处理分类标签切换。
 */
async function handleCategoryChange() {
  currentPage.value = 1
  await loadCollections()
}

/**
 * 打开新建分类弹窗。
 */
function openCreateCategoryDialog() {
  categoryForm.categoryName = ''
  categoryDialogVisible.value = true
}

/**
 * 创建收藏分类。
 */
async function handleCreateCategory() {
  try {
    await categoryFormRef.value?.validate()
  } catch {
    return
  }

  savingCategory.value = true
  try {
    const category = await createCollectionCategory({
      categoryName: categoryForm.categoryName.trim(),
    })
    categoryDialogVisible.value = false
    await loadCategories()
    activeCategoryTab.value = String(category.collectionCategoryId)
    currentPage.value = 1
    await loadCollections()
    ElMessage.success('收藏分类新建成功')
  } catch (error) {
    ElMessage.error(error.message || '收藏分类新建失败，请稍后重试')
  } finally {
    savingCategory.value = false
  }
}

/**
 * 调整收藏分类。
 *
 * @param {object} record 收藏记录
 * @param {number|string} categoryId 收藏分类ID
 */
async function handleCollectionCategoryUpdate(record, categoryId) {
  try {
    const result = await updateCollectionCategory(record.collectionId, {
      collectionCategoryId: categoryId,
    })
    record.collectionCategoryId = result.collectionCategoryId
    record.collectionCategoryName = result.collectionCategoryName
    await loadCategories()
    ElMessage.success('收藏分类调整成功')
  } catch (error) {
    ElMessage.error(error.message || '收藏分类调整失败，请稍后重试')
    await loadCollections()
  }
}

/**
 * 取消收藏。
 *
 * @param {object} record 收藏记录
 */
async function handleRemoveCollection(record) {
  try {
    await ElMessageBox.confirm(`确认取消收藏《${record.bookName || '当前图书'}》吗？`, '取消收藏', {
      type: 'warning',
      confirmButtonText: '确认取消',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  try {
    await removeCollection(record.collectionId)
    await loadCategories()
    await loadCollections()
    ElMessage.success('已取消收藏')
  } catch (error) {
    ElMessage.error(error.message || '取消收藏失败，请稍后重试')
  }
}

/**
 * 切换分页页码。
 *
 * @param {number} page 页码
 */
function handlePageChange(page) {
  currentPage.value = page
  loadCollections()
}

/**
 * 修改每页展示条数。
 *
 * @param {number} size 每页条数
 */
function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  loadCollections()
}

/**
 * 解析当前激活的分类ID。
 *
 * @returns {number|null} 当前分类ID
 */
function resolveActiveCategoryId() {
  return activeCategoryTab.value === 'all' ? null : Number(activeCategoryTab.value)
}

/**
 * 生成图书封面占位文本。
 *
 * @param {object} record 收藏记录
 * @returns {string} 占位文本
 */
function buildCoverPlaceholder(record) {
  return (record?.bookName || '图书').slice(0, 2)
}
</script>

<template>
  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>我的收藏</strong>
          <p>管理个人收藏分类，并在分类内整理已收藏图书</p>
        </div>
        <el-button type="primary" @click="openCreateCategoryDialog">新建收藏分类</el-button>
      </div>
    </template>

    <el-tabs v-model="activeCategoryTab" class="favorite-tabs" @tab-change="handleCategoryChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane
        v-for="category in categories"
        :key="category.collectionCategoryId"
        :label="`${category.categoryName}（${category.collectionCount || 0}）`"
        :name="String(category.collectionCategoryId)"
      />
    </el-tabs>
  </el-card>

  <el-card v-loading="loading || categoryLoading" shadow="never">
    <el-table :data="records" style="width: 100%" :empty-text="loading ? '加载中...' : '暂无收藏图书'">
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
              <p class="borrow-book-extra">图书分类：{{ row.categoryName || '暂无' }} · 收藏分类：{{ row.collectionCategoryName || '暂无' }}</p>
              <p class="borrow-book-extra">位置：{{ formatLocation(row) }} · 场景：{{ row.suitableScene || '暂无' }}</p>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="收藏分类" width="220">
        <template #default="{ row }">
          <el-select
            :model-value="row.collectionCategoryId"
            placeholder="请选择分类"
            @update:model-value="(value) => handleCollectionCategoryUpdate(row, value)"
          >
            <el-option v-for="category in categoryOptions" :key="category.value" :label="category.label" :value="category.value" />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column label="收藏时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.collectionDate) }}</template>
      </el-table-column>

      <el-table-column label="图书状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">{{ row.status === 1 ? '正常' : '下架' }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" link @click="handleRemoveCollection(row)">取消收藏</el-button>
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

  <el-dialog v-model="categoryDialogVisible" title="新建收藏分类" width="420px" destroy-on-close>
    <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-width="88px">
      <el-form-item label="分类名称" prop="categoryName">
        <el-input v-model="categoryForm.categoryName" maxlength="50" placeholder="请输入分类名称" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="categoryDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="savingCategory" @click="handleCreateCategory">确定</el-button>
    </template>
  </el-dialog>
</template>

