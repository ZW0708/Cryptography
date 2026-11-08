<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useReducedMotion } from '@/composables/useReducedMotion'

const canvasRef = ref<HTMLCanvasElement | null>(null)
const reduced = useReducedMotion()

let raf = 0
let last = 0
let cols: number[] = []
let w = 0
let h = 0
const FONT = 13
const GAP = 16
const CHARS = '0123456789abcdef'

function resize() {
  const cv = canvasRef.value
  if (!cv) return
  const dpr = Math.min(window.devicePixelRatio || 1, 2)
  w = window.innerWidth
  h = window.innerHeight
  cv.width = w * dpr
  cv.height = h * dpr
  cv.style.width = w + 'px'
  cv.style.height = h + 'px'
  const ctx = cv.getContext('2d')
  if (!ctx) return
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  const n = Math.ceil(w / GAP)
  cols = Array.from({ length: n }, () => Math.floor(Math.random() * -h))
}

function draw(ts: number) {
  raf = requestAnimationFrame(draw)
  if (ts - last < 50) return // 约 20fps，足够呈现雨感且省电
  last = ts
  const cv = canvasRef.value
  if (!cv) return
  const ctx = cv.getContext('2d')
  if (!ctx) return
  ctx.clearRect(0, 0, w, h)
  ctx.font = FONT + 'px ui-monospace, monospace'
  for (let i = 0; i < cols.length; i++) {
    const y = cols[i]
    ctx.fillStyle = 'rgba(62, 242, 176, 0.32)'
    ctx.fillText(CHARS[Math.floor(Math.random() * CHARS.length)], i * GAP, y)
    for (let t = 1; t <= 3; t++) {
      ctx.fillStyle = `rgba(62, 242, 176, ${0.15 - t * 0.042})`
      ctx.fillText(CHARS[Math.floor(Math.random() * CHARS.length)], i * GAP, y - t * FONT)
    }
    cols[i] = y > h + Math.random() * 240 ? Math.floor(Math.random() * -120) : y + FONT
  }
}

function stop() {
  if (raf) cancelAnimationFrame(raf)
  raf = 0
  const cv = canvasRef.value
  cv?.getContext('2d')?.clearRect(0, 0, w, h)
}

function start() {
  if (reduced.value || raf) return
  resize()
  raf = requestAnimationFrame(draw)
}

function onResize() {
  if (!reduced.value) resize()
}

watch(reduced, (v) => {
  if (v) stop()
  else start()
})

onMounted(start)
onBeforeUnmount(() => {
  stop()
  window.removeEventListener('resize', onResize)
})

window.addEventListener('resize', onResize)
</script>

<template>
  <canvas ref="canvasRef" class="hex-rain" aria-hidden="true"></canvas>
</template>

<style scoped>
.hex-rain {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  opacity: 0.5;
}

@media (max-width: 768px) {
  .hex-rain {
    opacity: 0.28;
  }
}
</style>