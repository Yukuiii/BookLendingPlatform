<script setup>
import { Bell, Management, SwitchButton } from '@element-plus/icons-vue'

/**
 * 控制台顶部栏组件。
 */
defineProps({
  title: {
    type: String,
    default: '',
  },
  subtitle: {
    type: String,
    default: '',
  },
  currentUser: {
    type: Object,
    default: null,
  },
  userTypeLabel: {
    type: String,
    default: '',
  },
  noticeCount: {
    type: Number,
    default: 3,
  },
})

const emit = defineEmits(['logout', 'profile'])

/**
 * 触发退出登录事件。
 */
function handleLogoutClick() {
  emit('logout')
}

/**
 * 触发个人中心事件。
 */
function handleProfileClick() {
  emit('profile')
}
</script>

<template>
  <el-header class="home-header">
    <div>
      <h1 class="home-page-title">{{ title }}</h1>
      <p class="home-page-subtitle">{{ subtitle }}</p>
    </div>

    <div class="home-header-actions">
      <el-badge :value="noticeCount" class="home-notice-badge">
        <el-button circle>
          <el-icon><Bell /></el-icon>
        </el-button>
      </el-badge>

      <el-dropdown>
        <div class="home-user-trigger">
          <el-avatar :size="36">{{ (currentUser?.realName || currentUser?.username || '读').slice(0, 1) }}</el-avatar>
          <div>
            <strong class="home-user-name">{{ currentUser?.realName || currentUser?.username }}</strong>
            <p class="home-user-role">{{ userTypeLabel }}</p>
          </div>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleProfileClick">
              <el-icon><Management /></el-icon>
              <span>个人中心</span>
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogoutClick">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

