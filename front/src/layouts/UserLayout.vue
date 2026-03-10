<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Collection, DataAnalysis, Reading, Search, Star, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import ConsoleFooter from '../components/layout/ConsoleFooter.vue'
import ConsoleHeader from '../components/layout/ConsoleHeader.vue'
import ConsoleSidebar from '../components/layout/ConsoleSidebar.vue'
import { USER_TYPE_OPTIONS } from '../constants/auth'
import { clearCurrentUser, getCurrentUser } from '../utils/auth'

/**
 * 用户端控制台布局壳，统一承载侧边栏、头部与内容区。
 */

const router = useRouter()
const route = useRoute()

const currentUser = getCurrentUser()

const menuItems = [
  { key: 'books', label: '图书检索', icon: Search },
  { key: 'borrow', label: '我的借阅', icon: Reading },
  { key: 'reservation', label: '我的预约', icon: Collection },
  { key: 'favorite', label: '我的收藏', icon: Star },
  { key: 'analysis', label: '借阅分析', icon: DataAnalysis },
  { key: 'profile', label: '个人信息', icon: User },
]

const activeMenu = computed(() => {
  return typeof route.name === 'string' ? route.name : 'books'
})

const pageTitle = computed(() => {
  return typeof route.meta?.title === 'string' ? route.meta.title : '图书借阅管理系统'
})

const pageSubtitle = computed(() => {
  const name = currentUser?.realName || currentUser?.username || ''
  return name ? `欢迎回来，${name}` : '欢迎回来'
})

const userTypeLabel = computed(() => {
  return USER_TYPE_OPTIONS.find((item) => item.value === String(currentUser?.userType))?.label || '未知角色'
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

