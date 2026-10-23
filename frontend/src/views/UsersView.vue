<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../api'
import AppShell from '../components/AppShell.vue'
import AppIcon from '../components/AppIcon.vue'
import { notify } from '../toast'

const users = ref([])
const loading = ref(false)
const keyword = ref('')
const modalOpen = ref(false)
const saving = ref(false)
const editId = ref(null)
const formError = ref('')

const form = reactive({
  username: '',
  displayName: '',
  password: '',
  role: 'USER',
  enabled: true
})

onMounted(loadUsers)

async function loadUsers() {
  loading.value = true
  try {
    const result = await http.get('/users', { params: { keyword: keyword.value || undefined } })
    users.value = result.data || []
  } catch (err) {
    notify(err.message, 'error')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.username = ''
  form.displayName = ''
  form.password = ''
  form.role = 'USER'
  form.enabled = true
  formError.value = ''
}

function openCreate() {
  editId.value = null
  resetForm()
  modalOpen.value = true
}

function openEdit(user) {
  editId.value = user.id
  form.username = user.username
  form.displayName = user.displayName
  form.password = ''
  form.role = user.role
  form.enabled = user.enabled
  formError.value = ''
  modalOpen.value = true
}

async function save() {
  formError.value = ''
  if (!form.displayName.trim()) {
    formError.value = '昵称不能为空'
    return
  }
  if (!editId.value && (!form.username || !form.password)) {
    formError.value = '新增用户需要填写用户名和初始密码'
    return
  }
  saving.value = true
  try {
    if (editId.value) {
      await http.put(`/users/${editId.value}`, {
        displayName: form.displayName,
        role: form.role,
        enabled: form.enabled,
        password: form.password
      })
      notify('用户信息已更新', 'success')
    } else {
      await http.post('/users', {
        username: form.username,
        displayName: form.displayName,
        password: form.password,
        role: form.role,
        enabled: form.enabled
      })
      notify('新用户已创建', 'success')
    }
    modalOpen.value = false
    await loadUsers()
  } catch (err) {
    formError.value = err.message
  } finally {
    saving.value = false
  }
}

async function remove(user) {
  if (!window.confirm(`确认删除用户「${user.displayName}」吗？该操作不可撤销。`)) return
  try {
    await http.delete(`/users/${user.id}`)
    notify('用户已删除', 'success')
    await loadUsers()
  } catch (err) {
    notify(err.message, 'error')
  }
}

function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function initials(name) {
  return (name || '?').slice(0, 1)
}
</script>

<template>
  <AppShell>
    <div class="users-page">
      <div class="page-lead">
        <div>
          <div class="eyebrow">ACCESS CONTROL</div>
          <h2>用户与角色</h2>
          <p>仅 ADMIN 可进入。新增、修改、删除都在服务端做角色校验，前端隐藏按钮不是安全边界。</p>
        </div>
        <button class="btn btn-primary" @click="openCreate"><AppIcon name="plus" :size="18" /> 新增用户</button>
      </div>

      <section class="panel table-panel">
        <div class="table-toolbar">
          <div class="search-box">
            <AppIcon name="search" :size="17" />
            <input v-model="keyword" placeholder="搜索用户名或昵称" @keyup.enter="loadUsers" />
          </div>
          <button class="btn btn-ghost" @click="loadUsers">刷新</button>
          <span class="table-count">共 {{ users.length }} 位用户</span>
        </div>

        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>用户</th><th>角色</th><th>状态</th><th>创建时间</th><th class="align-right">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading"><td colspan="5" class="empty-cell">正在加载…</td></tr>
              <tr v-else-if="users.length === 0"><td colspan="5" class="empty-cell">没有匹配的用户</td></tr>
              <tr v-for="user in users" :key="user.id">
                <td>
                  <div class="user-cell">
                    <div class="table-avatar" :class="user.role === 'ADMIN' ? 'admin' : ''">{{ initials(user.displayName) }}</div>
                    <div><strong>{{ user.displayName }}</strong><span>@{{ user.username }}</span></div>
                  </div>
                </td>
                <td><span class="role-chip" :class="user.role.toLowerCase()">{{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}</span></td>
                <td><span class="state-text" :class="{ off: !user.enabled }"><i></i>{{ user.enabled ? '启用' : '停用' }}</span></td>
                <td class="muted-cell">{{ formatDate(user.createdAt) }}</td>
                <td class="align-right">
                  <button class="icon-button" title="编辑" @click="openEdit(user)"><AppIcon name="edit" :size="17" /></button>
                  <button class="icon-button danger" title="删除" @click="remove(user)"><AppIcon name="trash" :size="17" /></button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="table-note"><AppIcon name="shield" :size="15" /> 密码字段不会通过任何查询接口返回；修改时留空表示保持原密码。</div>
      </section>

      <Teleport to="body">
        <div v-if="modalOpen" class="modal-backdrop" @click.self="modalOpen = false">
          <div class="modal-card">
            <div class="modal-head">
              <div>
                <div class="eyebrow">{{ editId ? 'EDIT USER' : 'NEW USER' }}</div>
                <h3>{{ editId ? '编辑用户' : '新增用户' }}</h3>
              </div>
              <button class="icon-button" @click="modalOpen = false">✕</button>
            </div>
            <form class="modal-form" @submit.prevent="save">
              <label class="field">
                <span>用户名</span>
                <input v-model="form.username" :disabled="!!editId" placeholder="字母开头，4-20 位" />
              </label>
              <label class="field">
                <span>显示昵称</span>
                <input v-model="form.displayName" placeholder="例如：李同学" />
              </label>
              <label class="field">
                <span>{{ editId ? '新密码（留空不修改）' : '初始密码' }}</span>
                <input v-model="form.password" type="password" placeholder="至少 8 位，含字母和数字" />
              </label>
              <div class="field-row">
                <label class="field">
                  <span>角色</span>
                  <select v-model="form.role">
                    <option value="USER">普通用户 USER</option>
                    <option value="ADMIN">管理员 ADMIN</option>
                  </select>
                </label>
                <label class="switch-field">
                  <span>账号状态</span>
                  <button type="button" class="switch" :class="{ on: form.enabled }" @click="form.enabled = !form.enabled">
                    <i></i>{{ form.enabled ? '启用' : '停用' }}
                  </button>
                </label>
              </div>
              <p v-if="formError" class="form-error">{{ formError }}</p>
              <div class="modal-actions">
                <button type="button" class="btn btn-ghost" @click="modalOpen = false">取消</button>
                <button type="submit" class="btn btn-primary" :disabled="saving">{{ saving ? '保存中…' : '保存' }}</button>
              </div>
            </form>
          </div>
        </div>
      </Teleport>
    </div>
  </AppShell>
</template>
