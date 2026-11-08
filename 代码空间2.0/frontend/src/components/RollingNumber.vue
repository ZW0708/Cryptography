<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useReducedMotion } from '@/composables/useReducedMotion'

const props = withDefaults(defineProps<{ value: number; duration?: number }>(), {
  duration: 600
})

const shown = ref(props.value)
const reduced = useReducedMotion()
let raf = 0

watch(
  () => props.value,
  (nv) => {
    if (reduced.value) {
      shown.value = nv
      return
    }
    const from = shown.value
    const start = performance.now()
    if (raf) cancelAnimationFrame(raf)
    const tick = (t: number) => {
      const p = Math.min(1, (t - start) / props.duration)
      const eased = 1 - Math.pow(1 - p, 3)
      shown.value = Math.round(from + (nv - from) * eased)
      if (p < 1) raf = requestAnimationFrame(tick)
    }
    raf = requestAnimationFrame(tick)
  }
)

onMounted(() => {
  shown.value = props.value
})
</script>

<template>
  <span>{{ shown }}</span>
</template>