<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

import { USER_TYPE_OPTIONS } from '../constants/auth'
import { clearCurrentUser, getCurrentUser } from '../utils/auth'

const router = useRouter()
const currentUser = getCurrentUser()

const userTypeLabel = computed(() => {
  return USER_TYPE_OPTIONS.find((item) => item.value === String(currentUser?.userType))?.label || '未知角色'
})

/**
 * 退出登录并返回登录页。
 */
function handleLogout() {
  clearCurrentUser()
  router.push({ name: 'login' })
}
</script>

<template>
  <main class="home-shell">
    <section class="home-card">
      <p class="home-badge">联调成功</p>
      <h1 class="home-title">欢迎回来，{{ currentUser?.realName || currentUser?.username }}</h1>
      <p class="home-subtitle">登录状态已接通前后端，后续可以在这里继续接图书业务页面。</p>

      <div class="home-info-list">
        <div class="home-info-item">
          <span class="home-info-label">用户名</span>
          <strong class="home-info-value">{{ currentUser?.username || '-' }}</strong>
        </div>
        <div class="home-info-item">
          <span class="home-info-label">角色</span>
          <strong class="home-info-value">{{ userTypeLabel }}</strong>
        </div>
        <div class="home-info-item">
          <span class="home-info-label">邮箱</span>
          <strong class="home-info-value">{{ currentUser?.email || '-' }}</strong>
        </div>
        <div class="home-info-item">
          <span class="home-info-label">手机号</span>
          <strong class="home-info-value">{{ currentUser?.phone || '-' }}</strong>
        </div>
      </div>

      <button class="submit-button home-action-button" type="button" @click="handleLogout">退出登录</button>
    </section>
  </main>
</template>
