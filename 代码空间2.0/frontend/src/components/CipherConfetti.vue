<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useReducedMotion } from '@/composables/useReducedMotion'

const reduced = useReducedMotion()
const chars = ref<
  { ch: string; left: number; delay: number; dur: number; size: number; rot: number; color: string }[]
>([])

const COLORS = ['#3ef2b0', '#6fc8ff', '#ffd06b', '#ff6b79', '#7c9cff']

onMounted(() => {
  if (reduced.value) return
  const arr: typeof chars.value = []
  for (let i = 0; i < 70; i++) {
    arr.push({
      ch: '0123456789abcdef'[Math.floor(Math.random() * 16)],
      left: Math.random() * 100,
      delay: Math.random() * 0.8,
      dur: 1.6 + Math.random() * 1.2,
      size: 12 + Math.random() * 14,
      rot: Math.round((Math.random() * 2 - 1) * 360),
      color: COLORS[Math.floor(Math.random() * COLORS.length)]
    })
  }
  chars.value = arr
  window.setTimeout(() => {
    chars.value = []
  }, 3400)
})
</script>

<template>
  <div class="confetti-layer" aria-hidden="true">
    <span
      v-for="(c, i) in chars"
      :key="i"
      class="cf"
      :style="{
        left: c.left + '%',
        animationDelay: c.delay + 's',
        animationDuration: c.dur + 's',
        fontSize: c.size + 'px',
        color: c.color,
        '--rot': c.rot + 'deg'
      }"
    >{{ c.ch }}</span>
  </div>
</template>

<style scoped>
.confetti-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 1200;
  overflow: hidden;
}

.cf {
  position: absolute;
  top: -32px;
  font-family: var(--mono);
  font-weight: 700;
  text-shadow: 0 0 10px currentColor;
  animation: cf-fall linear both;
}

@keyframes cf-fall {
  from {
    transform: translateY(-6vh) rotate(0deg);
    opacity: 1;
  }
  to {
    transform: translateY(112vh) rotate(var(--rot));
    opacity: 0.5;
  }
}
</style>