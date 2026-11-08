<script setup lang="ts">
import { useToast } from '@/composables/useToast'

const { toasts, remove } = useToast()

const colors: Record<string, string> = {
  success: '#3ef2b0',
  error: '#ff6b79',
  warning: '#ffd06b',
  info: '#6fc8ff'
}
</script>

<template>
  <div class="toast-host">
    <TransitionGroup name="toast">
      <div
        v-for="t in toasts"
        :key="t.id"
        class="toast"
        :style="{ borderLeftColor: colors[t.type] }"
        @click="remove(t.id)"
      >
        {{ t.message }}
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-host {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 90vw;
}

.toast {
  background: #202d28;
  border: 1px solid var(--border);
  border-left: 4px solid #6fc8ff;
  border-radius: 8px;
  padding: 10px 16px;
  color: var(--text);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.55);
  font-size: 14px;
  font-family: var(--mono);
  cursor: pointer;
  max-width: 360px;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.25s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>