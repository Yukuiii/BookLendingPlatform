<script setup>
import ConsoleSidebar from '../components/layout/ConsoleSidebar.vue'

/**
 * 控制台整体布局组件。
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
 * 转发侧边栏菜单选择事件。
 *
 * @param {string} menuKey 菜单键值
 */
function handleMenuSelect(menuKey) {
  emit('menu-select', menuKey)
}
</script>

<template>
  <el-container class="home-layout">
    <el-aside class="home-aside" width="220px">
      <ConsoleSidebar
        :active-menu="activeMenu"
        :menu-items="menuItems"
        :current-user="currentUser"
        :user-type-label="userTypeLabel"
        :recommendations="recommendations"
        @menu-select="handleMenuSelect"
      />
    </el-aside>

    <el-container>
      <slot name="header" />
      <el-main class="home-main">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

