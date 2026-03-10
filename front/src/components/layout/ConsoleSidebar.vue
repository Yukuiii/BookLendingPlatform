<script setup>
import { Star } from '@element-plus/icons-vue'

/**
 * 左侧控制台菜单栏组件。
 */
defineProps({
  activeMenu: {
    type: String,
    required: true,
  },
  menuItems: {
    type: Array,
    required: true,
  },
  currentUser: {
    type: Object,
    default: null,
  },
  userTypeLabel: {
    type: String,
    default: '',
  },
  recommendations: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['menu-select'])

/**
 * 处理菜单选择事件。
 *
 * @param {string} menuKey 菜单键值
 */
function handleSelect(menuKey) {
  emit('menu-select', menuKey)
}

/**
 * 生成图书封面占位文本。
 *
 * @param {object} book 图书对象
 * @returns {string} 占位文本
 */
function buildCoverPlaceholder(book) {
  return (book?.bookName || '图书').slice(0, 2)
}
</script>

<template>
  <div class="home-brand">
    <div class="home-brand-logo">图</div>
    <div>
      <strong class="home-brand-title">图书馆智能借阅</strong>
      <p class="home-brand-subtitle">Library Console</p>
    </div>
  </div>

  <el-menu class="home-menu" :default-active="activeMenu" @select="handleSelect">
    <el-menu-item v-for="item in menuItems" :key="item.key" :index="item.key">
      <el-icon><component :is="item.icon" /></el-icon>
      <span>{{ item.label }}</span>
    </el-menu-item>
  </el-menu>

  <el-card class="home-side-card" shadow="never">
    <template #header>
      <div class="home-side-card-header">
        <span>当前用户</span>
        <el-tag type="primary" effect="light">{{ userTypeLabel }}</el-tag>
      </div>
    </template>

    <div class="home-profile-row">
      <el-avatar class="home-user-avatar" :size="42">{{ (currentUser?.realName || currentUser?.username || '读').slice(0, 1) }}</el-avatar>
      <div>
        <strong class="home-profile-name">{{ currentUser?.realName || currentUser?.username }}</strong>
        <p class="home-profile-meta">{{ currentUser?.email || '暂无邮箱信息' }}</p>
      </div>
    </div>
  </el-card>

  <el-card class="home-side-card" shadow="never">
    <template #header>
      <div class="home-side-card-header">
        <span>热门推荐</span>
        <el-icon><Star /></el-icon>
      </div>
    </template>

    <div v-if="recommendations.length" class="home-side-list">
      <div v-for="book in recommendations" :key="book.bookId" class="home-side-book">
        <div class="home-side-book-cover">{{ buildCoverPlaceholder(book) }}</div>
        <div class="home-side-book-content">
          <strong class="home-side-book-title">{{ book.bookName }}</strong>
          <p class="home-side-book-meta">{{ book.author || '未知作者' }}</p>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无推荐" :image-size="64" />
  </el-card>
</template>

