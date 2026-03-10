<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Eye, EyeOff, LibraryBig, LockKeyhole, UserRound } from 'lucide-vue-next'

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

const passwordVisible = ref(false)
const submitting = ref(false)
const submitMessage = ref(typeof route.query.registered === 'string' ? '注册成功，请使用新账号登录。' : '')
const submitError = ref(false)

/**
 * 切换登录密码框的明文显示状态。
 */
function togglePasswordVisible() {
  passwordVisible.value = !passwordVisible.value
}

/**
 * 校验登录表单。
 *
 * @returns {string} 校验结果消息
 */
function validateLoginForm() {
  if (!loginForm.username.trim()) {
    return '请输入用户名'
  }
  if (!loginForm.password) {
    return '请输入密码'
  }
  return ''
}

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

/**
 * 处理登录表单提交。
 */
async function handleLoginSubmit() {
  const validationMessage = validateLoginForm()
  if (validationMessage) {
    updateSubmitMessage(validationMessage, true)
    return
  }

  submitting.value = true
  updateSubmitMessage('', false)

  try {
    const response = await login({
      username: loginForm.username.trim(),
      password: loginForm.password,
      userType: Number(loginForm.userType),
    })
    setCurrentUser(response.data)
    updateSubmitMessage(response.message || '登录成功', false)
    await router.push({ name: 'home' })
  } catch (error) {
    updateSubmitMessage(error.message || '登录失败，请稍后重试', true)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="auth-shell">
    <section class="auth-card">
      <header class="auth-header">
        <div class="brand-icon" aria-hidden="true">
          <LibraryBig />
        </div>

        <h1 class="auth-title">图书借阅管理系统</h1>
        <p class="auth-subtitle">请输入您的账户信息进行登录</p>
      </header>

      <form class="auth-form" @submit.prevent="handleLoginSubmit">
        <label class="field-group" for="login-username">
          <span class="field-label">用户名</span>
          <div class="field-control">
            <span class="field-prefix" aria-hidden="true">
              <UserRound />
            </span>
            <input id="login-username" v-model="loginForm.username" type="text" placeholder="请输入用户名" />
          </div>
        </label>

        <label class="field-group" for="login-password">
          <span class="field-label">密码</span>
          <div class="field-control">
            <span class="field-prefix" aria-hidden="true">
              <LockKeyhole />
            </span>
            <input
              id="login-password"
              v-model="loginForm.password"
              :type="passwordVisible ? 'text' : 'password'"
              placeholder="请输入密码"
            />
            <button class="field-action" type="button" aria-label="切换密码显示状态" @click="togglePasswordVisible">
              <EyeOff v-if="passwordVisible" />
              <Eye v-else />
            </button>
          </div>
        </label>

        <div class="field-group">
          <span class="field-label">用户类型</span>
          <div class="radio-group">
            <label v-for="option in USER_TYPE_OPTIONS" :key="option.value" class="radio-item">
              <input v-model="loginForm.userType" type="radio" name="login-user-type" :value="option.value" />
              <span class="radio-dot"></span>
              <span>{{ option.label }}</span>
            </label>
          </div>
        </div>

        <p v-if="submitMessage" :class="['form-message', submitError ? 'form-message--error' : 'form-message--success']">
          {{ submitMessage }}
        </p>

        <button class="submit-button" type="submit" :disabled="submitting">
          {{ submitting ? '登录中...' : '登录系统' }}
        </button>

        <p class="switch-text">
          还没有账号？
          <RouterLink class="switch-link" to="/register">立即注册</RouterLink>
        </p>
      </form>
    </section>

    <AuthFooter />
  </main>
</template>
