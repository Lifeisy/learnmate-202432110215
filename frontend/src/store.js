import { reactive, computed } from 'vue'

const TOKEN_KEY = 'learnmate_token'
const USER_KEY = 'learnmate_user'

function readUser() {
  try {
    return JSON.parse(sessionStorage.getItem(USER_KEY) || 'null')
  } catch {
    return null
  }
}

// JWT 放在 sessionStorage，关闭标签页即清除；生产环境建议改为 HttpOnly Cookie。
export const session = reactive({
  token: sessionStorage.getItem(TOKEN_KEY) || '',
  user: readUser()
})

export const isAdmin = computed(() => session.user?.role === 'ADMIN')

export function saveSession(payload) {
  session.token = payload.token
  session.user = payload.user
  sessionStorage.setItem(TOKEN_KEY, payload.token)
  sessionStorage.setItem(USER_KEY, JSON.stringify(payload.user))
}

export function clearSession() {
  session.token = ''
  session.user = null
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USER_KEY)
}

export function updateCurrentUser(user) {
  session.user = user
  sessionStorage.setItem(USER_KEY, JSON.stringify(user))
}
