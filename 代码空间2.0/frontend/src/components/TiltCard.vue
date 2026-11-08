<script setup lang="ts">
import { ref } from 'vue'
import { useReducedMotion } from '@/composables/useReducedMotion'

const el = ref<HTMLElement | null>(null)
const reduced = useReducedMotion()
let raf = 0

function onMove(e: MouseEvent) {
  if (reduced.value || window.matchMedia('(pointer: coarse)').matches) return
  const node = el.value
  if (!node) return
  const rect = node.getBoundingClientRect()
  const px = (e.clientX - rect.left) / rect.width - 0.5
  const py = (e.clientY - rect.top) / rect.height - 0.5
  if (raf) cancelAnimationFrame(raf)
  raf = requestAnimationFrame(() => {
    node.style.transform = `perspective(700px) rotateY(${(px * 6).toFixed(2)}deg) rotateX(${(-py * 5).toFixed(2)}deg) translateY(-2px)`
  })
}

function onLeave() {
  if (raf) cancelAnimationFrame(raf)
  const node = el.value
  if (node) node.style.transform = ''
}
</script>

<template>
  <div ref="el" class="tilt" @mousemove="onMove" @mouseleave="onLeave">
    <slot />
  </div>
</template>

<style scoped>
.tilt {
  transition: transform 0.15s ease-out;
  will-change: transform;
}

@media (prefers-reduced-motion: reduce) {
  .tilt {
    transition: none;
  }
}
</style>