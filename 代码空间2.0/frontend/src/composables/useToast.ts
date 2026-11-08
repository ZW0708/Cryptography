import { ref } from 'vue'

export type ToastType = 'success' | 'error' | 'info' | 'warning'

export interface ToastItem {
  id: number
  message: string
  type: ToastType
}

const toasts = ref<ToastItem[]>([])
let seq = 0

function remove(id: number) {
  toasts.value = toasts.value.filter((t) => t.id !== id)
}

function toast(message: string, type: ToastType = 'info', duration = 3000) {
  const id = ++seq
  toasts.value.push({ id, message, type })
  if (duration > 0) {
    setTimeout(() => remove(id), duration)
  }
  return id
}

export function useToast() {
  return { toasts, toast, remove }
}