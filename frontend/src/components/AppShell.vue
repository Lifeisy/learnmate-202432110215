<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api'
import { session, isAdmin, clearSession, updateCurrentUser } from '../store'
import AppIcon from './AppIcon.vue'

const route = useRoute()
const router = useRouter()

const navItems = computed(() => [
  { name: 'dashboard', label: '学习总览', icon: 'home', to: '/' },
  { name: 'chat', label: '智能问答', icon: 'chat', to: '/chat' },
  ...(isAdmin.value ? [{ name: 'users', label: '用户管理', icon: 'users', to: '/users' }] : [])
])

const pageTitle = computed(() => route.meta.title || 'LearnMate')
const initials = computed(() => (session.user?.displayName || 'L').slice(0, 1))

onMounted(async () => {
  if (!session.token) return
  try {
    const result = await http.get('/auth/me')
    if (result.data) updateCurrentUser(result.data)
  } catch {
    // 401 已由 axios 拦截器统一处理
  }
})

function logout() {
  clearSession()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="app-frame">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">LM</div>
        <div>
          <div class="brand-name">LearnMate</div>
          <div class="brand-sub">学习搭子 · AI Lab</div>
        </div>
      </div>

      <div class="workspace-pill">
        <span class="pulse-dot"></span>
        <span>课程知识库已连接</span>
        <span class="workspace-tag">v1.0</span>
      </div>

      <nav class="nav-list" aria-label="主导航">
        <RouterLink v-for="item in navItems" :key="item.name" :to="item.to" class="nav-item"
                    :class="{ active: route.name === item.name }">
          <AppIcon :name="item.icon" :size="19" />
          <span>{{ item.label }}</span>
          <span v-if="item.name === 'chat'" class="nav-badge">AI</span>
        </RouterLink>
      </nav>

      <div class="sidebar-note">
        <AppIcon name="shield" :size="18" />
        <div>
          <strong>安全模式已开启</strong>
          <span>JWT + RBAC · 输入防护</span>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="user-avatar">{{ initials }}</div>
        <div class="user-meta">
          <strong>{{ session.user?.displayName || '学习者' }}</strong>
          <span>{{ session.user?.role === 'ADMIN' ? '管理员' : '普通用户' }} · {{ session.user?.username }}</span>
        </div>
        <button class="icon-button" title="退出登录" @click="logout">
          <AppIcon name="logout" :size="18" />
        </button>
      </div>
    </aside>

    <main class="main-panel">
      <header class="topbar">
        <div>
          <div class="eyebrow">LEARNMATE / WORKSPACE</div>
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="topbar-actions">
          <div class="status-chip"><span class="status-dot"></span> 服务正常</div>
          <div class="date-chip">{{ new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'short' }) }}</div>
        </div>
      </header>
      <div class="page-body">
        <slot />
      </div>
    </main>
  </div>
</template>
