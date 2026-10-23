<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api'
import { session } from '../store'
import AppShell from '../components/AppShell.vue'
import AppIcon from '../components/AppIcon.vue'
import { notify } from '../toast'

const router = useRouter()
const stats = reactive({ totalUsers: 0, adminUsers: 0, knowledgeChunks: 0, chatsToday: 0 })
const ai = reactive({ llmConfigured: false, model: 'local-retrieval' })
const loading = ref(true)
const question = ref('')

const quickQuestions = [
  'RAG 的检索流程是什么？',
  'JWT 如何防止篡改？',
  'Git 冲突怎么解决？',
  'Spring Boot 如何配置数据源？'
]

const capabilities = [
  { icon: 'database', title: '资料检索', text: '23 个课程片段已向量化入库', tint: 'mint' },
  { icon: 'shield', title: '权限边界', text: 'ADMIN / USER 双角色隔离', tint: 'indigo' },
  { icon: 'spark', title: '可核验回答', text: '每个结论都带来源编号', tint: 'amber' }
]

onMounted(async () => {
  try {
    const [statsResult, aiResult] = await Promise.all([
      http.get('/dashboard/stats'),
      http.get('/ai/health')
    ])
    Object.assign(stats, statsResult.data)
    Object.assign(ai, aiResult.data)
  } catch (err) {
    notify(err.message, 'error')
  } finally {
    loading.value = false
  }
})

function ask(value) {
  const text = (value || question.value || '').trim()
  if (!text) return
  router.push({ name: 'chat', query: { q: text } })
}

const greeting = (() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})()
</script>

<template>
  <AppShell>
    <div class="dashboard">
      <section class="hero-card">
        <div class="hero-copy">
          <div class="eyebrow light">TODAY'S STUDY</div>
          <h2>{{ greeting }}，{{ session.user?.displayName || '同学' }}。</h2>
          <p>课程资料已经准备好了。把一个问题交给我，我会先检索、再回答，并把出处一起给你。</p>
          <div class="hero-search">
            <AppIcon name="search" :size="19" />
            <input v-model="question" placeholder="例如：RAG 的检索流程是什么？" @keyup.enter="ask()" />
            <button class="btn btn-mint" @click="ask()">开始提问 <AppIcon name="arrow" :size="16" /></button>
          </div>
          <div class="chip-row">
            <button v-for="item in quickQuestions.slice(0, 3)" :key="item" class="soft-chip" @click="ask(item)">
              {{ item }}
            </button>
          </div>
        </div>
        <div class="hero-orbit" aria-hidden="true">
          <div class="orbit-ring ring-one"></div>
          <div class="orbit-ring ring-two"></div>
          <div class="orbit-core"><AppIcon name="spark" :size="34" /></div>
          <span class="orbit-node node-a">RAG</span>
          <span class="orbit-node node-b">JWT</span>
          <span class="orbit-node node-c">SQL</span>
        </div>
      </section>

      <section class="stat-grid">
        <article class="stat-card">
          <div class="stat-icon indigo"><AppIcon name="users" :size="20" /></div>
          <div class="stat-meta"><span>系统用户</span><strong>{{ loading ? '—' : stats.totalUsers }}</strong></div>
          <small>{{ stats.adminUsers }} 位管理员</small>
        </article>
        <article class="stat-card">
          <div class="stat-icon mint"><AppIcon name="database" :size="20" /></div>
          <div class="stat-meta"><span>知识片段</span><strong>{{ loading ? '—' : stats.knowledgeChunks }}</strong></div>
          <small>已完成向量化</small>
        </article>
        <article class="stat-card">
          <div class="stat-icon amber"><AppIcon name="chat" :size="20" /></div>
          <div class="stat-meta"><span>今日问答</span><strong>{{ loading ? '—' : stats.chatsToday }}</strong></div>
          <small>含本地检索</small>
        </article>
        <article class="stat-card">
          <div class="stat-icon rose"><AppIcon name="zap" :size="20" /></div>
          <div class="stat-meta"><span>生成模式</span><strong class="stat-word">{{ ai.llmConfigured ? 'LLM' : 'RAG' }}</strong></div>
          <small>{{ ai.llmConfigured ? ai.model : '本地检索模式' }}</small>
        </article>
      </section>

      <div class="content-grid">
        <section class="panel">
          <div class="panel-head">
            <div><div class="eyebrow">CAPABILITIES</div><h3>这套原型能做什么</h3></div>
            <span class="mini-badge">可演示</span>
          </div>
          <div class="capability-list">
            <div v-for="item in capabilities" :key="item.title" class="capability-row">
              <div class="capability-icon" :class="item.tint"><AppIcon :name="item.icon" :size="19" /></div>
              <div><strong>{{ item.title }}</strong><span>{{ item.text }}</span></div>
              <AppIcon name="arrow" :size="17" class="row-arrow" />
            </div>
          </div>
        </section>

        <section class="panel">
          <div class="panel-head">
            <div><div class="eyebrow">SECURITY CHECK</div><h3>上线前检查</h3></div>
            <AppIcon name="shield" :size="22" class="muted-icon" />
          </div>
          <ul class="check-list">
            <li><span class="check-dot done"><AppIcon name="check" :size="12" /></span><div><strong>密码 BCrypt 摘要</strong><span>数据库不保存明文</span></div></li>
            <li><span class="check-dot done"><AppIcon name="check" :size="12" /></span><div><strong>JWT 签名与过期校验</strong><span>无效令牌返回 401</span></div></li>
            <li><span class="check-dot done"><AppIcon name="check" :size="12" /></span><div><strong>角色访问控制</strong><span>普通用户无法管理用户</span></div></li>
            <li><span class="check-dot done"><AppIcon name="check" :size="12" /></span><div><strong>提示注入拦截</strong><span>敏感指令在入口被拒绝</span></div></li>
          </ul>
        </section>
      </div>
    </div>
  </AppShell>
</template>
