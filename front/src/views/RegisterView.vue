<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { CreditCard, Iphone, Lock, Message, Reading, School, User, UserFilled } from '@element-plus/icons-vue'

import { register } from '../api/auth'
import AuthFooter from '../components/auth/AuthFooter.vue'

const router = useRouter()

const registerForm = reactive({
  username: '',
  realName: '',
  major: '',
  password: '',
  confirmPassword: '',
  phone: '',
  identityCard: '',
  email: '',
  agree: true,
})

const formRef = ref(null)
const submitting = ref(false)
const submitMessage = ref('')
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

/**
 * 校验协议勾选状态。
 *
 * @param {object} _rule 校验规则
 * @param {boolean} value 当前值
 * @param {(error?: Error) => void} callback 回调函数
 */
function validateAgreement(_rule, value, callback) {
  if (!value) {
    callback(new Error('请先阅读并同意服务条款与隐私协议'))
    return
  }
  callback()
}

/**
 * 校验确认密码是否与密码一致。
 *
 * @param {object} _rule 校验规则
 * @param {string} value 当前值
 * @param {(error?: Error) => void} callback 回调函数
 */
function validateConfirmPassword(_rule, value, callback) {
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, message: '用户名长度不能少于 4 位', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  major: [{ max: 50, message: '专业长度不能超过 50 位', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' },
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  identityCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^(\d{15}|\d{17}[\dXx])$/, message: '请输入正确的身份证号', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
  agree: [{ validator: validateAgreement, trigger: 'change' }],
}

/**
 * 处理注册表单提交。
 */
async function handleRegisterSubmit() {
  if (submitting.value) {
    return
  }

  updateSubmitMessage('', false)

  try {
    await formRef.value?.validate()
  } catch {
    updateSubmitMessage('请完善注册信息', true)
    return
  }

  submitting.value = true

  try {
    const response = await register({
      username: registerForm.username.trim(),
      realName: registerForm.realName.trim(),
      major: registerForm.major.trim() || null,
      password: registerForm.password,
      phone: registerForm.phone.trim(),
      identityCard: registerForm.identityCard.trim(),
      email: registerForm.email.trim(),
    })
    updateSubmitMessage(response.message || '注册成功', false)
    await router.push({
      name: 'login',
      query: {
        username: registerForm.username.trim(),
        registered: '1',
      },
    })
  } catch (error) {
    updateSubmitMessage(error.message || '注册失败，请稍后重试', true)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="auth-shell">
    <section class="auth-card auth-card--register">
      <header class="auth-header">
        <div class="brand-icon" aria-hidden="true">
          <el-icon><Reading /></el-icon>
        </div>

        <h1 class="auth-title">图书借阅管理系统</h1>
        <p class="auth-subtitle">创建您的账户并开启便捷借阅之旅</p>
      </header>

      <el-form
        ref="formRef"
        class="auth-form auth-form--register auth-form--element"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        @submit.prevent="handleRegisterSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名（必填）" clearable>
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" clearable>
            <template #prefix>
              <el-icon><UserFilled /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="专业" prop="major">
          <el-input v-model="registerForm.major" placeholder="请输入专业（可选）" clearable>
            <template #prefix>
              <el-icon><School /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" show-password placeholder="请输入密码" clearable>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" show-password placeholder="请再次输入密码" clearable>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" clearable>
            <template #prefix>
              <el-icon><Iphone /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="身份证号" prop="identityCard">
          <el-input v-model="registerForm.identityCard" placeholder="请输入身份证号" clearable>
            <template #prefix>
              <el-icon><CreditCard /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" clearable>
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="agree" class="auth-agreement">
          <el-checkbox v-model="registerForm.agree">
            我已阅读并同意
            <el-link type="primary" :underline="false">《服务条款》</el-link>
            和
            <el-link type="primary" :underline="false">《隐私协议》</el-link>
          </el-checkbox>
        </el-form-item>

        <el-alert
          v-if="submitMessage"
          class="auth-alert"
          :closable="false"
          show-icon
          :type="submitError ? 'error' : 'success'"
          :title="submitMessage"
        />

        <el-button class="auth-submit" type="primary" :loading="submitting" native-type="submit">立即注册</el-button>

        <p class="switch-text">
          已有账号？
          <RouterLink class="switch-link" to="/login">立即登录</RouterLink>
        </p>
      </el-form>
    </section>

    <AuthFooter />
  </main>
</template>
