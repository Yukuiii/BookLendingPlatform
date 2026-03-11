<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { createAdminBookLocation, pageAdminBookLocations, pageAdminBooks, updateAdminBookLocation } from '../../api/admin'
import { formatDateTime } from '../../utils/book'

/**
 * 管理端图书位置页面，负责维护图书位置数据。
 */

const loading = ref(false)
const saving = ref(false)
const records = ref([])
const bookOptions = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const editingLocationId = ref(null)

const queryForm = reactive({
  bookName: '',
  isbn: '',
})

const locationForm = reactive({
  bookId: null,
  floor: 1,
  area: '',
  shelfNo: '',
  layer: 1,
  rfidCode: '',
})

const locationFormRef = ref(null)
const locationRules = {
  bookId: [{ required: true, message: '请选择图书', trigger: 'change' }],
  area: [{ required: true, message: '请输入区域', trigger: 'blur' }],
  shelfNo: [{ required: true, message: '请输入书架号', trigger: 'blur' }],
}

/**
 * 页面挂载后初始化数据。
 */
onMounted(async () => {
  await loadBookOptions()
  await loadLocations()
})

/**
 * 加载图书选项。
 */
async function loadBookOptions() {
  try {
    const response = await pageAdminBooks({
      current: 1,
      size: 100,
    })
    bookOptions.value = response.records || []
  } catch (error) {
    ElMessage.error(error.message || '图书选项加载失败，请稍后重试')
  }
}

/**
 * 加载图书位置列表。
 */
async function loadLocations() {
  loading.value = true
  try {
    const response = await pageAdminBookLocations({
      current: currentPage.value,
      size: pageSize.value,
      bookName: queryForm.bookName.trim(),
      isbn: queryForm.isbn.trim(),
    })
    records.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error(error.message || '图书位置加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 执行筛选查询。
 */
function handleSearch() {
  currentPage.value = 1
  loadLocations()
}

/**
 * 重置筛选条件。
 */
function handleReset() {
  queryForm.bookName = ''
  queryForm.isbn = ''
  currentPage.value = 1
  loadLocations()
}

/**
 * 打开新增位置弹窗。
 */
function openCreateDialog() {
  editingLocationId.value = null
  resetLocationForm()
  dialogVisible.value = true
}

/**
 * 打开编辑位置弹窗。
 *
 * @param {object} row 位置数据
 */
function openEditDialog(row) {
  editingLocationId.value = row.locationId
  locationForm.bookId = row.bookId
  locationForm.floor = row.floor ?? 1
  locationForm.area = row.area || ''
  locationForm.shelfNo = row.shelfNo || ''
  locationForm.layer = row.layer ?? 1
  locationForm.rfidCode = row.rfidCode || ''
  dialogVisible.value = true
}

/**
 * 保存图书位置。
 */
async function handleSave() {
  try {
    await locationFormRef.value?.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    const payload = {
      bookId: Number(locationForm.bookId),
      floor: Number(locationForm.floor),
      area: locationForm.area.trim(),
      shelfNo: locationForm.shelfNo.trim(),
      layer: Number(locationForm.layer),
      rfidCode: locationForm.rfidCode.trim() || null,
    }
    if (editingLocationId.value) {
      await updateAdminBookLocation(editingLocationId.value, payload)
    } else {
      await createAdminBookLocation(payload)
    }
    dialogVisible.value = false
    await loadLocations()
    ElMessage.success(editingLocationId.value ? '位置修改成功' : '位置新增成功')
  } catch (error) {
    ElMessage.error(error.message || '位置保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

/**
 * 重置位置表单。
 */
function resetLocationForm() {
  locationForm.bookId = null
  locationForm.floor = 1
  locationForm.area = ''
  locationForm.shelfNo = ''
  locationForm.layer = 1
  locationForm.rfidCode = ''
}

/**
 * 切换分页页码。
 *
 * @param {number} page 页码
 */
function handlePageChange(page) {
  currentPage.value = page
  loadLocations()
}

/**
 * 修改每页展示条数。
 *
 * @param {number} size 每页条数
 */
function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  loadLocations()
}
</script>

<template>
  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>位置管理</strong>
          <p>维护图书楼层、区域、书架、层数与 RFID 编码</p>
        </div>
        <el-button type="primary" @click="openCreateDialog">新增位置</el-button>
      </div>
    </template>

    <el-form class="home-filter-form" :model="queryForm" label-width="70px" @submit.prevent>
      <el-form-item label="书名">
        <el-input v-model="queryForm.bookName" clearable placeholder="请输入书名" />
      </el-form-item>
      <el-form-item label="ISBN">
        <el-input v-model="queryForm.isbn" clearable placeholder="请输入 ISBN" />
      </el-form-item>
      <el-form-item class="home-filter-actions">
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <el-card shadow="never">
    <el-table v-loading="loading" :data="records" style="width: 100%" :empty-text="loading ? '加载中...' : '暂无位置数据'">
      <el-table-column prop="bookName" label="书名" min-width="180" />
      <el-table-column prop="isbn" label="ISBN" min-width="150" />
      <el-table-column prop="floor" label="楼层" width="80" align="center" />
      <el-table-column prop="area" label="区域" width="100" align="center" />
      <el-table-column prop="shelfNo" label="书架号" width="110" align="center" />
      <el-table-column prop="layer" label="层数" width="80" align="center" />
      <el-table-column prop="rfidCode" label="RFID" min-width="150" />
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
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

  <el-dialog v-model="dialogVisible" :title="editingLocationId ? '编辑位置' : '新增位置'" width="640px" destroy-on-close>
    <el-form ref="locationFormRef" class="admin-form-grid" :model="locationForm" :rules="locationRules" label-width="88px">
      <el-form-item label="图书" prop="bookId">
        <el-select v-model="locationForm.bookId" filterable placeholder="请选择图书">
          <el-option v-for="item in bookOptions" :key="item.bookId" :label="`${item.bookName}（${item.isbn}）`" :value="item.bookId" />
        </el-select>
      </el-form-item>
      <el-form-item label="楼层">
        <el-input-number v-model="locationForm.floor" :min="1" controls-position="right" />
      </el-form-item>
      <el-form-item label="区域" prop="area">
        <el-input v-model="locationForm.area" />
      </el-form-item>
      <el-form-item label="书架号" prop="shelfNo">
        <el-input v-model="locationForm.shelfNo" />
      </el-form-item>
      <el-form-item label="层数">
        <el-input-number v-model="locationForm.layer" :min="1" controls-position="right" />
      </el-form-item>
      <el-form-item label="RFID">
        <el-input v-model="locationForm.rfidCode" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

