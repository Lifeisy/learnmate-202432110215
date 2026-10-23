<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import http from '../api'
import AppShell from '../components/AppShell.vue'
import AppIcon from '../components/AppIcon.vue'
import { notify } from '../toast'

const route = useRoute()
const messages = ref([
  {
    id: 1,
    role: 'assistant',
    answer: '你好，我是学习搭子。我会先从课程资料里检索，再基于检索结果回答，并标出来源。\n\n试试问我：RAG 的检索流程、JWT 校验、Git 冲突处理。',
    mode: 'INTRO',
    sources: []
  }
])
const input = ref('')
const loading = ref(false)
const scrollRef = ref(null)
const health = reactive({ knowledgeChunks: 0, llmConfigured: false, model: 'local-retrieval' })
let seq = 10

const suggestions = [
  'RAG 的检索流程是什么？',
  'JWT 如何防止被篡改？',
  'Git 合并冲突怎么解决？',
  '如何防止提示注入？'
]

const modes = {
  'RAG+LLM': { label: 'RAG + 大模型', tone: 'green' },
  RAG_ONLY: { label: '本地检索', tone: 'amber' },
  NO_CONTEXT: { label: '资料未覆盖', tone: 'red' },
  INTRO: { label: '引导', tone: 'slate' }
}

onMounted(async () => {
  try {
    const result = await http.get('/ai/health')
    Object.assign(health, result.data)
  } catch {
    // 健康检查失败不阻塞问答
  }
  if (route.query.q) {
    input.value = String(route.query.q)
    await send()
  }
})

function scrollToBottom() {
  nextTick(() => {
    if (scrollRef.value) scrollRef.value.scrollTop = scrollRef.value.scrollHeight
  })
}

async function send() {
  const question = input.value.trim()
  if (!question || loading.value) return
  input.value = ''
  messages.value.push({ id: ++seq, role: 'user', answer: question })
  loading.value = true
  scrollToBottom()
  try {
    const result = await http.post('/ai/chat', { question })
    messages.value.push({
      id: ++seq,
      role: 'assistant',
      answer: result.data.answer,
      mode: result.data.mode,
      notice: result.data.notice,
      sources: result.data.sources || []
    })
    if (result.data.mode === 'RAG_ONLY') {
      notify('当前为本地检索模式，配置 LLM_API_KEY 后可生成式回答', 'info')
    }
  } catch (err) {
    messages.value.push({
      id: ++seq,
      role: 'assistant',
      answer: `请求没有完成：${err.message}`,
      mode: 'NO_CONTEXT',
      sources: []
    })
    notify(err.message, 'error')
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function useSuggestion(text) {
  input.value = text
  send()
}

function resetChat() {
  messages.value = messages.value.slice(0, 1)
  input.value = ''
}

function modeOf(message) {
  return modes[message.mode] || modes.INTRO
}
</script>

<template>
  <AppShell>
    <div class="chat-page">
      <section class="chat-main panel">
        <div class="chat-head">
          <div class="chat-title">
            <div class="assistant-avatar"><AppIcon name="spark" :size="21" /></div>
            <div>
              <h2>学习搭子</h2>
              <span><i class="online-dot"></i> 资料库在线 · {{ health.knowledgeChunks }} 个片段</span>
            </div>
          </div>
          <div class="chat-actions">
            <span class="mode-pill" :class="health.llmConfigured ? 'green' : 'amber'">
              {{ health.llmConfigured ? 'RAG + LLM' : '本地检索模式' }}
            </span>
            <button class="btn btn-ghost btn-small" @click="resetChat">新会话</button>
          </div>
        </div>

        <div ref="scrollRef" class="message-scroll">
          <div v-for="message in messages" :key="message.id" class="message-row" :class="message.role">
            <div v-if="message.role === 'assistant'" class="message-avatar assistant-avatar small">
              <AppIcon name="spark" :size="16" />
            </div>
            <div class="message-body">
              <div class="message-bubble">
                <div class="message-text">{{ message.answer }}</div>
                <div v-if="message.sources?.length" class="source-block">
                  <div class="source-title"><AppIcon name="book" :size="14" /> 参考来源</div>
                  <div class="source-grid">
                    <div v-for="(source, index) in message.sources" :key="index" class="source-card">
                      <span class="source-index">[{{ index + 1 }}]</span>
                      <div>
                        <strong>{{ source.docTitle }}</strong>
                        <span>{{ source.section }}</span>
                      </div>
                      <small>{{ Math.round(source.score * 100) }}%</small>
                    </div>
                  </div>
                </div>
              </div>
              <div class="message-meta">
                <span v-if="message.role === 'assistant'" class="mode-tag" :class="modeOf(message).tone">
                  {{ modeOf(message).label }}
                </span>
                <span v-if="message.notice" class="message-notice">{{ message.notice }}</span>
              </div>
            </div>
            <div v-if="message.role === 'user'" class="message-avatar user-avatar-small">
              {{ (message.answer || '我').slice(0, 1) }}
            </div>
          </div>

          <div v-if="loading" class="message-row assistant">
            <div class="message-avatar assistant-avatar small"><AppIcon name="spark" :size="16" /></div>
            <div class="message-bubble typing"><span></span><span></span><span></span> 正在检索资料…</div>
          </div>
        </div>

        <div class="composer">
          <div class="composer-box">
            <textarea v-model="input" rows="1" maxlength="500"
                      placeholder="输入你的问题，Enter 发送，Shift + Enter 换行…"
                      @keydown.enter.exact.prevent="send"></textarea>
            <button class="send-button" :disabled="loading || !input.trim()" @click="send">
              <AppIcon name="send" :size="19" />
            </button>
          </div>
          <div class="composer-foot">
            <span>回答仅依据课程资料，请核对引用；敏感指令会被拦截。</span>
            <span>{{ input.length }}/500</span>
          </div>
        </div>
      </section>

      <aside class="chat-side">
        <section class="panel side-panel">
          <div class="panel-head compact">
            <div><div class="eyebrow">TRY ASKING</div><h3>可以这样问</h3></div>
          </div>
          <button v-for="item in suggestions" :key="item" class="suggest-row" @click="useSuggestion(item)">
            <span>{{ item }}</span><AppIcon name="arrow" :size="16" />
          </button>
        </section>

        <section class="panel side-panel">
          <div class="panel-head compact">
            <div><div class="eyebrow">KNOWLEDGE BASE</div><h3>已接入资料</h3></div>
            <span class="mini-badge">{{ health.knowledgeChunks }} 块</span>
          </div>
          <ul class="doc-list">
            <li><AppIcon name="book" :size="16" /><div><strong>RAG 检索增强生成</strong><span>流程 · 切块 · 引用</span></div></li>
            <li><AppIcon name="book" :size="16" /><div><strong>Spring Boot 开发要点</strong><span>分层 · 数据源 · 部署</span></div></li>
            <li><AppIcon name="book" :size="16" /><div><strong>登录、权限与 AI 安全</strong><span>JWT · RBAC · 注入</span></div></li>
            <li><AppIcon name="book" :size="16" /><div><strong>Git 协作与持续集成</strong><span>分支 · 冲突 · CI</span></div></li>
          </ul>
        </section>

        <div class="safety-note">
          <AppIcon name="shield" :size="19" />
          <div><strong>提示注入防护已开启</strong><span>资料中的指令只作为文本，不会被执行。</span></div>
        </div>
      </aside>
    </div>
  </AppShell>
</template>
