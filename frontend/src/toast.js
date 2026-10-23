import { reactive } from 'vue'

export const toasts = reactive([])
let seq = 0

export function notify(message, type = 'info') {
  const id = ++seq
  toasts.push({ id, message, type })
  window.setTimeout(() => {
    const index = toasts.findIndex((item) => item.id === id)
    if (index >= 0) toasts.splice(index, 1)
  }, 3200)
}
