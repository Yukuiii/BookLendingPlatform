<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { pageAdminUsers, updateAdminUser } from '../../api/admin'
import { USER_TYPE_OPTIONS } from '../../constants/auth'
import { USER_STATUS, USER_STATUS_LABEL_MAP, USER_STATUS_OPTIONS, USER_STATUS_TAG_TYPE_MAP } from '../../constants/status'
import { formatDateTime } from '../../utils/book'

/**
 * 管理端用户管理页面，负责查询与修改用户信息。
 */

const loading = ref(false)
const saving = ref(false)
const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const editDialogVisible = ref(false)
const editingUserId = ref(null)

const queryForm = reactive({
  username: '',
  realName: '',
  userType: null,
  status: null,
})

const editForm = reactive({
  realName: '',
  major: '',
  email: '',
  phone: '',
  userType: 1,
  maxBorrowCount: 5,
  status: USER_STATUS.NORMAL,
})

const editFormRef = ref(null)
const editRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
}

const statusOptions = USER_STATUS_OPTIONS

/**
 * 页面挂载后加载用户列表。
 */
onMounted(() => {
  loadUsers()
})

/**
 * 加载用户分页数据。
 */
async function loadUsers() {
  loading.value = true
  try {
    const response = await pageAdminUsers({
      current: currentPage.value,
      size: pageSize.value,
      username: queryForm.username.trim(),
      realName: queryForm.realName.trim(),
      userType: queryForm.userType,
      status: queryForm.status,
    })
    records.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error(error.message || '用户列表加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 执行筛选查询。
 */
function handleSearch() {
  currentPage.value = 1
  loadUsers()
}

/**
 * 重置筛选条件。
 */
function handleReset() {
  queryForm.username = ''
  queryForm.realName = ''
  queryForm.userType = null
  queryForm.status = null
  currentPage.value = 1
  loadUsers()
}

/**
 * 打开编辑用户弹窗。
 *
 * @param {object} row 用户数据
 */
function openEditDialog(row) {
  editingUserId.value = row.userId
  editForm.realName = row.realName || ''
  editForm.major = row.major || ''
  editForm.email = row.email || ''
  editForm.phone = row.phone || ''
  editForm.userType = row.userType ?? 1
  editForm.maxBorrowCount = row.maxBorrowCount ?? 5
  editForm.status = row.status ?? USER_STATUS.NORMAL
  editDialogVisible.value = true
}

/**
 * 保存用户修改。
 */
async function handleSave() {
  try {
    await editFormRef.value?.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    await updateAdminUser(editingUserId.value, {
      realName: editForm.realName.trim(),
      major: editForm.major.trim() || null,
      email: editForm.email.trim(),
      phone: editForm.phone.trim(),
      userType: Number(editForm.userType),
      maxBorrowCount: Number(editForm.maxBorrowCount),
      status: Number(editForm.status),
    })
    editDialogVisible.value = false
    await loadUsers()
    ElMessage.success('用户信息修改成功')
  } catch (error) {
    ElMessage.error(error.message || '用户信息修改失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

/**
 * 切换分页页码。
 *
 * @param {number} page 页码
 */
function handlePageChange(page) {
  currentPage.value = page
  loadUsers()
}

/**
 * 修改每页展示条数。
 *
 * @param {number} size 每页条数
 */
function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  loadUsers()
}

/**
 * 获取用户状态标签类型。
 *
 * @param {number} status 用户状态
 * @returns {string} 标签类型
 */
function resolveStatusType(status) {
  return USER_STATUS_TAG_TYPE_MAP[Number(status)] || 'danger'
}
</script>

<template>
  <el-card class="home-filter-card" shadow="never">
    <template #header>
      <div class="home-section-header">
        <div>
          <strong>用户管理</strong>
          <p>筛选系统用户，并维护角色、借阅上限与账号状态</p>
        </div>
        <el-button type="primary" plain @click="handleSearch">刷新列表</el-button>
      </div>
    </template>

    <el-form class="home-filter-form" :model="queryForm" label-width="70px" @submit.prevent>
      <el-form-item label="用户名">
        <el-input v-model="queryForm.username" clearable placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="queryForm.realName" clearable placeholder="请输入真实姓名" />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="queryForm.userType" clearable placeholder="全部角色">
          <el-option v-for="item in USER_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="Number(item.value)" />
        </el-select>
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
    <el-table v-loading="loading" :data="records" style="width: 100%" :empty-text="loading ? '加载中...' : '暂无用户数据'">
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="realName" label="真实姓名" min-width="120" />
      <el-table-column prop="major" label="专业" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          {{ USER_TYPE_OPTIONS.find((item) => Number(item.value) === row.userType)?.label || '未知角色' }}
        </template>
      </el-table-column>
      <el-table-column prop="maxBorrowCount" label="借阅上限" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="resolveStatusType(row.status)" effect="light">
            {{ USER_STATUS_LABEL_MAP[Number(row.status)] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
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

  <el-dialog v-model="editDialogVisible" title="编辑用户" width="640px" destroy-on-close>
    <el-form ref="editFormRef" class="admin-form-grid" :model="editForm" :rules="editRules" label-width="88px">
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="editForm.realName" />
      </el-form-item>
      <el-form-item label="专业">
        <el-input v-model="editForm.major" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="editForm.email" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="editForm.phone" />
      </el-form-item>
      <el-form-item label="用户角色">
        <el-select v-model="editForm.userType">
          <el-option v-for="item in USER_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="Number(item.value)" />
        </el-select>
      </el-form-item>
      <el-form-item label="借阅上限">
        <el-input-number v-model="editForm.maxBorrowCount" :min="1" :max="50" controls-position="right" />
      </el-form-item>
      <el-form-item label="账号状态">
        <el-radio-group v-model="editForm.status">
          <el-radio-button :label="1">正常</el-radio-button>
          <el-radio-button :label="0">禁用</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="editDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>
