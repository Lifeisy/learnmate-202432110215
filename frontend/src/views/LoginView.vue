<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api'
import { saveSession } from '../store'
import { notify } from '../toast'
import AppIcon from '../components/AppIcon.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const error = ref('')
const form = reactive({ username: 'admin', password: 'Admin@123' })

const demos = [
  { label: '管理员', username: 'admin', password: 'Admin@123', hint: '可维护用户与角色' },
  { label: '普通用户', username: 'student', password: 'Student@123', hint: '可使用智能问答' }
]

function useDemo(item) {
  form.username = item.username
  form.password = item.password
  error.value = ''
}

async function submit() {
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const result = await http.post('/auth/login', {
      username: form.username.trim(),
      password: form.password
    })
    saveSession(result.data)
    notify(`欢迎回来，${result.data.user.displayName}`, 'success')
    router.push(route.query.redirect || '/')
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <section class="login-visual">
      <div class="visual-grid"></div>
      <div class="visual-glow"></div>
      <div class="visual-content">
        <div class="login-brand">
          <div class="brand-mark large">LM</div>
          <span>LearnMate</span>
        </div>
        <div class="visual-copy">
          <div class="eyebrow light">RAG · SPRING BOOT · VUE 3</div>
          <h1>把课程资料，<br /><em>变成会引用的答案。</em></h1>
          <p>一个面向学习场景的全栈原型：用户与权限、资料检索、提示注入防护，以及可核验的 AI 回答。</p>
        </div>

        <div class="answer-preview">
          <div class="preview-head">
            <span class="mini-avatar"><AppIcon name="spark" :size="15" /></span>
            <span>学习搭子</span>
            <span class="preview-mode">RAG + 引用</span>
          </div>
          <p>RAG 的检索流程包含文档采集、文本切块、向量化、相似度检索和提示词组装五个阶段。<span class="citation">[1]</span></p>
          <div class="preview-source">
            <AppIcon name="book" :size="14" />
            《RAG 检索增强生成》· 检索流程
          </div>
        </div>

        <div class="visual-footer">
          <span><AppIcon name="shield" :size="15" /> BCrypt + JWT</span>
          <span><AppIcon name="database" :size="15" /> MySQL / H2</span>
          <span><AppIcon name="zap" :size="15" /> OpenAI 兼容</span>
        </div>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-card">
        <div class="mobile-brand">
          <div class="brand-mark">LM</div>
          <span>LearnMate</span>
        </div>
        <div class="eyebrow">WELCOME BACK</div>
        <h2>登录学习空间</h2>
        <p class="muted">使用实训账号进入工作台，所有接口均经过 JWT 校验。</p>

        <form class="login-form" @submit.prevent="submit">
          <label class="field">
            <span>用户名</span>
            <input v-model="form.username" autocomplete="username" placeholder="请输入用户名" />
          </label>
          <label class="field">
            <span>密码</span>
            <input v-model="form.password" type="password" autocomplete="current-password" placeholder="请输入密码" />
          </label>
          <p v-if="error" class="form-error">{{ error }}</p>
          <button class="btn btn-primary btn-block" type="submit" :disabled="loading">
            <span v-if="loading" class="spinner"></span>
            {{ loading ? '正在验证…' : '进入 LearnMate' }}
            <AppIcon v-if="!loading" name="arrow" :size="18" />
          </button>
        </form>

        <div class="demo-divider"><span>实训演示账号</span></div>
        <div class="demo-grid">
          <button v-for="item in demos" :key="item.username" class="demo-card" type="button" @click="useDemo(item)">
            <span class="demo-role">{{ item.label }}</span>
            <strong>{{ item.username }}</strong>
            <small>{{ item.hint }}</small>
          </button>
        </div>
        <p class="login-footnote">密码仅用于本地演示，数据库中只保存 BCrypt 摘要。</p>
      </div>
    </section>
  </div>
</template>
