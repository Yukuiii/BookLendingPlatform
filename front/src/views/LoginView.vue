<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Lock, Reading, User } from '@element-plus/icons-vue'

import { login } from '../api/auth'
import AuthFooter from '../components/auth/AuthFooter.vue'
import { USER_TYPE_OPTIONS } from '../constants/auth'
import { setCurrentUser } from '../utils/auth'

const router = useRouter()
const route = useRoute()

const loginForm = reactive({
  username: typeof route.query.username === 'string' ? route.query.username : '',
  password: '',
  userType: '1',
})

const formRef = ref(null)
const submitting = ref(false)
const submitMessage = ref(typeof route.query.registered === 'string' ? '注册成功，请使用新账号登录。' : '')
const submitError = ref(false)

/**
 * 更新提交反馈消息。
 *
 * @param {string} message 提示文案
 * @param {boolean} isError 是否为错误提示
 */
function updateSubmitMessage(message, isError) {
  submitMessage.value = message
  submitError.value = isError
}

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
}

/**
 * 处理登录表单提交。
 */
async function handleLoginSubmit() {
  if (submitting.value) {
    return
  }

  updateSubmitMessage('', false)

  try {
    await formRef.value?.validate()
  } catch {
    updateSubmitMessage('请完善登录信息', true)
    return
  }

  submitting.value = true

  try {
    const response = await login({
      username: loginForm.username.trim(),
      password: loginForm.password,
      userType: Number(loginForm.userType),
    })
    setCurrentUser(response.data)
    updateSubmitMessage(response.message || '登录成功', false)
    const targetRouteName = resolveLoginTargetRouteName(response.data)
    await router.push({ name: targetRouteName })
  } catch (error) {
    updateSubmitMessage(error.message || '登录失败，请稍后重试', true)
  } finally {
    submitting.value = false
  }
}

/**
 * 解析登录后的默认跳转页面。
 *
 * @param {object} currentUser 当前登录用户
 * @returns {string} 目标路由名
 */
function resolveLoginTargetRouteName(currentUser) {
  const userType = Number(currentUser?.userType)
  if (userType === 3) {
    return 'admin-users'
  }
  if (userType === 2) {
    return 'admin-books'
  }
  return 'books'
}
</script>

<template>
  <main class="auth-shell">
    <section class="auth-card">
      <header class="auth-header">
        <div class="brand-icon" aria-hidden="true">
          <el-icon><Reading /></el-icon>
        </div>

        <h1 class="auth-title">图书借阅管理系统</h1>
        <p class="auth-subtitle">请输入您的账户信息进行登录</p>
      </header>

      <el-form
        ref="formRef"
        class="auth-form auth-form--element"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        @submit.prevent="handleLoginSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" clearable>
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" clearable>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="用户类型" prop="userType">
          <el-radio-group v-model="loginForm.userType" class="auth-radio-group">
            <el-radio v-for="option in USER_TYPE_OPTIONS" :key="option.value" :label="option.value">
              {{ option.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-alert
          v-if="submitMessage"
          class="auth-alert"
          :closable="false"
          show-icon
          :type="submitError ? 'error' : 'success'"
          :title="submitMessage"
        />

        <el-button class="auth-submit" type="primary" :loading="submitting" native-type="submit">登录系统</el-button>

        <p class="switch-text">
          还没有账号？
          <RouterLink class="switch-link" to="/register">立即注册</RouterLink>
        </p>
      </el-form>
    </section>

    <AuthFooter />
  </main>
</template>
