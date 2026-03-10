<script setup>
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
</script>

<template>
  <div class="home-brand">
    <div class="home-brand-logo">图</div>
    <div>
      <strong class="home-brand-title">图书借阅管理系统</strong>
      <p class="home-brand-subtitle">Library Console</p>
    </div>
  </div>

  <el-menu class="home-menu" :default-active="activeMenu" @select="handleSelect">
    <el-menu-item v-for="item in menuItems" :key="item.key" :index="item.key">
      <el-icon><component :is="item.icon" /></el-icon>
      <span>{{ item.label }}</span>
    </el-menu-item>
  </el-menu>
</template>
