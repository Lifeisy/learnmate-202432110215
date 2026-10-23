import { createRouter, createWebHashHistory } from 'vue-router'
import { session } from './store'
import LoginView from './views/LoginView.vue'
import DashboardView from './views/DashboardView.vue'
import UsersView from './views/UsersView.vue'
import ChatView from './views/ChatView.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView, meta: { public: true, title: '登录' } },
    { path: '/', name: 'dashboard', component: DashboardView, meta: { title: '学习总览' } },
    { path: '/chat', name: 'chat', component: ChatView, meta: { title: '智能问答' } },
    { path: '/users', name: 'users', component: UsersView, meta: { title: '用户管理', admin: true } },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · LearnMate` : 'LearnMate'
  if (!to.meta.public && !session.token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.admin && session.user?.role !== 'ADMIN') {
    return { name: 'dashboard' }
  }
  if (to.name === 'login' && session.token) {
    return { name: 'dashboard' }
  }
  return true
})

export default router
