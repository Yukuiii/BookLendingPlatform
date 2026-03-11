<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ChatDotRound, Management, MapLocation, Reading, User } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import ConsoleFooter from '../components/layout/ConsoleFooter.vue'
import ConsoleHeader from '../components/layout/ConsoleHeader.vue'
import ConsoleSidebar from '../components/layout/ConsoleSidebar.vue'
import { USER_TYPE_OPTIONS } from '../constants/auth'
import { clearCurrentUser, getCurrentUser, onCurrentUserChange } from '../utils/auth'

/**
 * 管理端控制台布局壳，统一承载菜单、头部与内容区。
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

const activeMenu = computed(() => {
  return typeof route.name === 'string' ? route.name : resolveAdminHomeRouteName()
})

const pageTitle = computed(() => {
  return typeof route.meta?.title === 'string' ? route.meta.title : '后台管理'
})

const pageSubtitle = computed(() => {
  const name = currentUser.value?.realName || currentUser.value?.username || ''
  return name ? `当前管理账号：${name}` : '当前管理账号'
})

const userTypeLabel = computed(() => {
  return USER_TYPE_OPTIONS.find((item) => item.value === String(currentUser.value?.userType))?.label || '未知角色'
})

const menuItems = computed(() => {
  const items = [
    { key: 'admin-books', label: '图书管理', icon: Management },
    { key: 'admin-locations', label: '图书位置管理', icon: MapLocation },
    { key: 'admin-borrows', label: '借阅管理', icon: Reading },
    { key: 'admin-comments', label: '评论管理', icon: ChatDotRound },
  ]

  if (Number(currentUser.value?.userType) === 3) {
    return [{ key: 'admin-users', label: '用户管理', icon: User }, ...items]
  }
  return items
})

/**
 * 解析管理端默认首页。
 *
 * @returns {string} 路由名称
 */
function resolveAdminHomeRouteName() {
  return Number(currentUser.value?.userType) === 3 ? 'admin-users' : 'admin-books'
}

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
  router.push({ name: 'admin-profile' })
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
