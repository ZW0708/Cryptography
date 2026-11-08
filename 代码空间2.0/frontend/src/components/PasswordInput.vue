<script setup lang="ts">
import { ref, useAttrs } from 'vue'

defineOptions({ inheritAttrs: false })

const model = defineModel<string>({ required: true })
const attrs = useAttrs()
const show = ref(false)
</script>

<template>
  <div class="pwd-wrap">
    <input
      v-bind="attrs"
      v-model="model"
      :type="show ? 'text' : 'password'"
      class="pwd-input"
    />
    <button
      type="button"
      class="pwd-eye"
      :aria-label="show ? '隐藏密码' : '显示密码'"
      :title="show ? '隐藏密码' : '显示密码'"
      @click="show = !show"
    >
      <svg
        v-if="show"
        width="20"
        height="20"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
        aria-hidden="true"
      >
        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
        <line x1="1" y1="1" x2="23" y2="23" />
      </svg>
      <svg
        v-else
        width="20"
        height="20"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
        aria-hidden="true"
      >
        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
        <circle cx="12" cy="12" r="3" />
      </svg>
    </button>
  </div>
</template>

<style scoped>
.pwd-wrap {
  position: relative;
  width: 100%;
}

.pwd-input {
  padding-right: 40px;
}

.pwd-eye {
  position: absolute;
  top: 50%;
  right: 8px;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2px;
  background: none;
  border: none;
  border-radius: 4px;
  color: var(--text-muted);
  line-height: 0;
}

.pwd-eye:hover {
  color: var(--primary);
}
</style>