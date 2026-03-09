<script setup>
import { LibraryBig } from 'lucide-vue-next'

defineProps({
  title: {
    type: String,
    required: true,
  },
  subtitle: {
    type: String,
    required: true,
  },
  mode: {
    type: String,
    default: 'login',
  },
})
</script>

<template>
  <main class="auth-shell">
    <section class="auth-card" :class="{ 'auth-card--register': mode === 'register' }">
      <div class="brand-icon" aria-hidden="true">
        <LibraryBig />
      </div>

      <h1 class="auth-title">{{ title }}</h1>
      <p class="auth-subtitle">{{ subtitle }}</p>

      <slot />
    </section>

    <p class="copyright-text">© 2026 图书馆借阅管理系统 版权所有</p>
  </main>
</template>
