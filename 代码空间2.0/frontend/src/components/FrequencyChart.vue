<script setup lang="ts">
import { ref, computed } from 'vue'

const ciphertext = ref('KHOOR ZRUOG')
const shift = ref(3)
const plaintext = computed(() =>
  ciphertext.value.replace(/[A-Z]/g, (c) =>
    String.fromCharCode(((c.charCodeAt(0) - 65 - shift.value + 26) % 26) + 65)
  )
)

const letters = 'ETAOINSHRDLCUMWFGYPBVKJXQZ'.split('')
const freq = computed(() => {
  const counts: Record<string, number> = {}
  ciphertext.value.toUpperCase().replace(/[^A-Z]/g, '').split('').forEach((c) => {
    counts[c] = (counts[c] || 0) + 1
  })
  return letters.map((l) => ({ letter: l, count: counts[l] || 0 }))
})
const maxCount = computed(() => Math.max(1, ...freq.value.map((f) => f.count)))
</script>

<template>
  <div class="card">
    <h3>频率分析柱状图（SVG）</h3>
    <p>密文：<input v-model="ciphertext" /></p>
    <p>位移：<input type="range" min="0" max="25" v-model.number="shift" /> {{ shift }}</p>
    <svg
      width="320"
      height="160"
      style="background: #131d19; border: 1px solid var(--border); border-radius: 8px"
    >
      <line x1="0" y1="159" x2="320" y2="159" stroke="#3b4f47" stroke-width="1" />
      <g v-for="(f, i) in freq" :key="f.letter">
        <rect
          :x="i * 12"
          :y="159 - (f.count / maxCount) * 140"
          width="10"
          :height="(f.count / maxCount) * 140"
          :fill="i < 5 ? '#3ef2b0' : '#3b4f47'"
          rx="1"
        />
        <text :x="i * 12 + 2" y="156" font-size="9" fill="#b6c9c0" font-family="monospace">
          {{ f.letter }}
        </text>
      </g>
    </svg>
    <p>还原明文：<strong style="color: var(--primary)">{{ plaintext }}</strong></p>
    <p class="muted" style="font-size: 12px">简化演示：拖动位移观察明文，实际频率分析需统计更大量文本。</p>
  </div>
</template>