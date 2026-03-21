<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bell, ChatDotRound, Clock, Reading, Search, Setting, Star, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import ConsoleFooter from '../components/layout/ConsoleFooter.vue'
import ConsoleHeader from '../components/layout/ConsoleHeader.vue'
import ConsoleSidebar from '../components/layout/ConsoleSidebar.vue'
import { USER_TYPE_OPTIONS } from '../constants/auth'
import { clearCurrentUser, getCurrentUser, onCurrentUserChange } from '../utils/auth'

/**
 * 用户端控制台布局壳，统一承载侧边栏、头部与内容区。
 */

const router = useRouter()
const route = useRoute()

const currentUser = ref(getCurrentUser())
let disposeCurrentUserListener = null

/**
 * 页面挂载后监听登录用户变化。
 */
onMounted(() => {
  disposeCurrentUserListener = onCurrentUserChange(() => {
    currentUser.value = getCurrentUser()
  })
})

/**
 * 页面销毁时清理监听。
 */
onUnmounted(() => {
  disposeCurrentUserListener?.()
})

const menuItems = [
  { key: 'books', label: '图书检索', icon: Search },
  { key: 'borrow', label: '我的借阅', icon: Reading },
  { key: 'reservations', label: '我的预约', icon: Clock },
  { key: 'favorite', label: '我的收藏', icon: Star },
  { key: 'notifications', label: '信息通知', icon: Bell },
  { key: 'comments', label: '我的评论', icon: ChatDotRound },
  { key: 'profile', label: '个人信息', icon: User },
  { key: 'preferences', label: '个性化设置', icon: Setting },
]

const activeMenu = computed(() => {
  return typeof route.name === 'string' ? route.name : 'books'
})

const pageTitle = computed(() => {
  return typeof route.meta?.title === 'string' ? route.meta.title : '图书借阅管理系统'
})

const pageSubtitle = computed(() => {
  const name = currentUser.value?.realName || currentUser.value?.username || ''
  return name ? `欢迎回来，${name}` : '欢迎回来'
})

const userTypeLabel = computed(() => {
  return USER_TYPE_OPTIONS.find((item) => item.value === String(currentUser.value?.userType))?.label || '未知角色'
})

/**
 * 处理左侧菜单点击并切换路由。
 *
 * @param {string} menuKey 菜单键值
 */
function handleMenuSelect(menuKey) {
  router.push({ name: menuKey })
}

/**
 * 跳转到个人信息页。
 */
function handleProfile() {
  router.push({ name: 'profile' })
}

/**
 * 退出登录并返回登录页。
 */
function handleLogout() {
  clearCurrentUser()
  ElMessage.success('已退出登录')
  router.push({ name: 'login' })
}
</script>

<template>
  <main class="home-shell">
    <el-container class="home-layout">
      <el-aside class="home-aside" width="220px">
        <ConsoleSidebar :active-menu="activeMenu" :menu-items="menuItems" @menu-select="handleMenuSelect" />
      </el-aside>

      <el-container direction="vertical">
        <ConsoleHeader
          :title="pageTitle"
          :subtitle="pageSubtitle"
          :current-user="currentUser"
          :user-type-label="userTypeLabel"
          @logout="handleLogout"
          @profile="handleProfile"
        />

        <el-main class="home-main">
          <RouterView />
        </el-main>

        <ConsoleFooter />
      </el-container>
    </el-container>
  </main>
</template>
