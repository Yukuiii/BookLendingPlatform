<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { USER_STATUS, USER_STATUS_LABEL_MAP, USER_STATUS_TAG_TYPE_MAP } from '../constants/status'
import { changeCurrentUserPassword, getCurrentUserProfile, updateCurrentUserProfile } from '../api/user'
import { getCurrentUser, setCurrentUser } from '../utils/auth'

/**
 * 个人信息页面，负责展示与修改当前用户资料。
 */

const loading = ref(false)
const profileSaving = ref(false)
const passwordSaving = ref(false)
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const profileForm = reactive({
  username: '',
  realName: '',
  major: '',
  email: '',
  phone: '',
  identityCard: '',
  userType: '',
  maxBorrowCount: 0,
  status: USER_STATUS.NORMAL,
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const profileRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请输入确认密码', trigger: 'blur' }],
}

/**
 * 页面挂载后加载个人信息。
 */
onMounted(() => {
  loadProfile()
})

/**
 * 加载当前用户个人信息。
 */
async function loadProfile() {
  loading.value = true

  try {
    const profile = await getCurrentUserProfile()
    profileForm.username = profile.username || ''
    profileForm.realName = profile.realName || ''
    profileForm.major = profile.major || ''
    profileForm.email = profile.email || ''
    profileForm.phone = profile.phone || ''
    profileForm.identityCard = profile.identityCard || ''
    profileForm.userType = resolveUserTypeLabel(profile.userType)
    profileForm.maxBorrowCount = profile.maxBorrowCount ?? 0
    profileForm.status = profile.status ?? USER_STATUS.NORMAL
  } catch (error) {
    ElMessage.error(error.message || '个人信息加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 保存个人信息。
 */
async function handleProfileSave() {
  try {
    await profileFormRef.value?.validate()
  } catch {
    return
  }

  profileSaving.value = true
  try {
    const profile = await updateCurrentUserProfile({
      realName: profileForm.realName.trim(),
      major: profileForm.major.trim() || null,
      email: profileForm.email.trim(),
      phone: profileForm.phone.trim(),
    })

    // 更新本地登录态，确保头部昵称与角色信息立即同步。
    setCurrentUser({
      ...(getCurrentUser() || {}),
      userId: profile.userId,
      username: profile.username,
      realName: profile.realName,
      email: profile.email,
      phone: profile.phone,
      userType: profile.userType,
      major: profile.major,
      maxBorrowCount: profile.maxBorrowCount,
      status: profile.status,
    })
    await loadProfile()
    ElMessage.success('个人信息修改成功')
  } catch (error) {
    ElMessage.error(error.message || '个人信息修改失败，请稍后重试')
  } finally {
    profileSaving.value = false
  }
}

/**
 * 保存密码。
 */
async function handlePasswordSave() {
  try {
    await passwordFormRef.value?.validate()
  } catch {
    return
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  passwordSaving.value = true
  try {
    await changeCurrentUserPassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value?.clearValidate?.()
    ElMessage.success('密码修改成功')
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败，请稍后重试')
  } finally {
    passwordSaving.value = false
  }
}

/**
 * 获取用户类型文案。
 *
 * @param {number} userType 用户类型
 * @returns {string} 用户类型文案
 */
function resolveUserTypeLabel(userType) {
  if (userType === 2) {
    return '图书管理员'
  }
  if (userType === 3) {
    return '系统管理员'
  }
  return '用户'
}
</script>

<template>
  <div v-loading="loading" class="profile-grid">
    <el-card class="profile-card" shadow="never">
      <template #header>
        <div class="home-section-header">
          <div>
            <strong>基础资料</strong>
            <p>维护当前账号的联系方式与专业信息</p>
          </div>
        </div>
      </template>

      <el-form ref="profileFormRef" class="profile-form" :model="profileForm" :rules="profileRules" label-width="88px">
        <el-form-item label="用户名">
          <el-input v-model="profileForm.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="profileForm.realName" maxlength="50" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="profileForm.major" maxlength="50" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" maxlength="100" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileForm.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="profileForm.identityCard" disabled />
        </el-form-item>
        <el-form-item label="账号角色">
          <el-input v-model="profileForm.userType" disabled />
        </el-form-item>
        <el-form-item label="借阅上限">
          <el-input :model-value="String(profileForm.maxBorrowCount)" disabled />
        </el-form-item>
        <el-form-item label="账号状态">
          <el-tag :type="USER_STATUS_TAG_TYPE_MAP[Number(profileForm.status)] || 'danger'">
            {{ USER_STATUS_LABEL_MAP[Number(profileForm.status)] || '未知' }}
          </el-tag>
        </el-form-item>
        <el-form-item class="profile-form-actions">
          <el-button type="primary" :loading="profileSaving" @click="handleProfileSave">保存资料</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="profile-card" shadow="never">
      <template #header>
        <div class="home-section-header">
          <div>
            <strong>修改密码</strong>
            <p>建议定期更换密码，避免账号被他人误用</p>
          </div>
        </div>
      </template>

      <el-form ref="passwordFormRef" class="profile-form" :model="passwordForm" :rules="passwordRules" label-width="88px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item class="profile-form-actions">
          <el-button type="primary" :loading="passwordSaving" @click="handlePasswordSave">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
