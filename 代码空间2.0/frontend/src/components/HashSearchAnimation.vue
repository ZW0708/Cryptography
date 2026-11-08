<script setup lang="ts">
import { ref } from 'vue'

const input = ref('hello')
const targetPrefix = ref('a1b2c3')
const searching = ref(false)
const attempts = ref(0)
const found = ref<string | null>(null)
let timer: number | null = null

async function sha256Hex(text: string): Promise<string> {
  const data = new TextEncoder().encode(text)
  const digest = await crypto.subtle.digest('SHA-256', data)
  return Array.from(new Uint8Array(digest)).map((b) => b.toString(16).padStart(2, '0')).join('')
}

async function computeHash() {
  return await sha256Hex(input.value)
}

async function startSearch() {
  searching.value = true
  attempts.value = 0
  found.value = null
  let i = 0
  timer = window.setInterval(async () => {
    const candidate = String(i)
    const h = await sha256Hex(candidate)
    attempts.value = i
    if (h.startsWith(targetPrefix.value)) {
      found.value = candidate
      searching.value = false
      if (timer) clearInterval(timer)
    }
    i++
    if (i > 20000000 && timer) {
      clearInterval(timer)
      searching.value = false
    }
  }, 0)
}

function stop() {
  if (timer) clearInterval(timer)
  searching.value = false
}
</script>

<template>
  <div class="card">
    <h3>哈希搜索动画（Canvas/真实 SHA-256）</h3>
    <label>输入文本观察哈希变化</label>
    <input v-model="input" />
    <p>
      SHA-256：
      <code style="word-break: break-all; color: var(--primary)">{{ computeHash() }}</code>
    </p>
    <hr style="border: none; border-top: 1px solid var(--border); margin: 14px 0" />
    <label>目标哈希前缀（6 位）</label>
    <input v-model="targetPrefix" />
    <div style="margin-top: 12px">
      <button :disabled="searching" @click="startSearch">开始穷举</button>
      <button class="secondary" style="margin-left: 8px" :disabled="!searching" @click="stop">停止</button>
    </div>
    <p>已尝试：<span style="color: var(--info)">{{ attempts.toLocaleString() }}</span> 次</p>
    <p v-if="found" style="color: var(--primary)">命中！输入 = {{ found }}</p>
    <p v-else-if="searching" style="color: var(--warning)">穷举中…体会 16^6≈1600 万次的成本</p>
    <p class="muted" style="font-size: 12px">简化演示：仅展示前缀碰撞，实际碰撞攻击需更大空间。</p>
  </div>
</template>