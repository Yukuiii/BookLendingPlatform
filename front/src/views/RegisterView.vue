<script setup>
import { reactive, ref } from 'vue'
import { ChevronDown, Eye, EyeOff, IdCard, LibraryBig, LockKeyhole, Mail, Phone, UserRound } from 'lucide-vue-next'

import AuthFooter from '../components/auth/AuthFooter.vue'
import { USER_TYPE_OPTIONS } from '../constants/auth'

const registerForm = reactive({
  username: '',
  realName: '',
  password: '',
  phone: '',
  identityCard: '',
  email: '',
  userType: '1',
  agree: true,
})

const passwordVisible = ref(false)

/**
 * 切换注册密码框的明文显示状态。
 */
function togglePasswordVisible() {
  passwordVisible.value = !passwordVisible.value
}

/**
 * 处理注册表单提交。
 */
function handleRegisterSubmit() {
  console.info('注册表单已提交：', { ...registerForm })
}
</script>

<template>
  <main class="auth-shell">
    <section class="auth-card auth-card--register">
      <header class="auth-header">
        <div class="brand-icon" aria-hidden="true">
          <LibraryBig />
        </div>

        <h1 class="auth-title">图书借阅管理系统</h1>
        <p class="auth-subtitle">创建您的账户并开启便捷借阅之旅</p>
      </header>

      <form class="auth-form auth-form--register" @submit.prevent="handleRegisterSubmit">
        <label class="field-group" for="register-username">
          <span class="field-label">用户名 *</span>
          <div class="field-control">
            <span class="field-prefix" aria-hidden="true">
              <UserRound />
            </span>
            <input id="register-username" v-model="registerForm.username" type="text" placeholder="请输入用户名（必填）" />
          </div>
        </label>

        <label class="field-group" for="register-real-name">
          <span class="field-label">真实姓名 *</span>
          <div class="field-control">
            <span class="field-prefix" aria-hidden="true">
              <UserRound />
            </span>
            <input id="register-real-name" v-model="registerForm.realName" type="text" placeholder="请输入真实姓名" />
          </div>
        </label>

        <label class="field-group" for="register-password">
          <span class="field-label">密码 *</span>
          <div class="field-control">
            <span class="field-prefix" aria-hidden="true">
              <LockKeyhole />
            </span>
            <input
              id="register-password"
              v-model="registerForm.password"
              :type="passwordVisible ? 'text' : 'password'"
              placeholder="请输入密码"
            />
            <button class="field-action" type="button" aria-label="切换密码显示状态" @click="togglePasswordVisible">
              <EyeOff v-if="passwordVisible" />
              <Eye v-else />
            </button>
          </div>
        </label>

        <label class="field-group" for="register-phone">
          <span class="field-label">手机号 *</span>
          <div class="field-control">
            <span class="field-prefix" aria-hidden="true">
              <Phone />
            </span>
            <input id="register-phone" v-model="registerForm.phone" type="tel" placeholder="请输入手机号" />
          </div>
        </label>

        <label class="field-group" for="register-identity-card">
          <span class="field-label">身份证号 *</span>
          <div class="field-control">
            <span class="field-prefix" aria-hidden="true">
              <IdCard />
            </span>
            <input id="register-identity-card" v-model="registerForm.identityCard" type="text" placeholder="请输入身份证号" />
          </div>
        </label>

        <label class="field-group" for="register-email">
          <span class="field-label">邮箱 *</span>
          <div class="field-control">
            <span class="field-prefix" aria-hidden="true">
              <Mail />
            </span>
            <input id="register-email" v-model="registerForm.email" type="email" placeholder="请输入邮箱" />
          </div>
        </label>

        <label class="field-group" for="register-user-type">
          <span class="field-label">角色 *</span>
          <div class="field-control">
            <span class="field-prefix" aria-hidden="true">
              <UserRound />
            </span>
            <select id="register-user-type" v-model="registerForm.userType">
              <option v-for="option in USER_TYPE_OPTIONS" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
            <span class="field-action field-action--static" aria-hidden="true">
              <ChevronDown />
            </span>
          </div>
        </label>

        <label class="agreement-row">
          <input v-model="registerForm.agree" type="checkbox" />
          <span>
            我已阅读并同意
            <button class="switch-link" type="button">《服务条款》</button>
            和
            <button class="switch-link" type="button">《隐私协议》</button>
          </span>
        </label>

        <button class="submit-button" type="submit">立即注册</button>

        <p class="switch-text">
          已有账号？
          <RouterLink class="switch-link" to="/login">立即登录</RouterLink>
        </p>
      </form>
    </section>

    <AuthFooter />
  </main>
</template>
