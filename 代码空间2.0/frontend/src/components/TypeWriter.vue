<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useReducedMotion } from '@/composables/useReducedMotion'

const props = withDefaults(defineProps<{ text: string; speed?: number; play?: boolean }>(), {
  speed: 42,
  play: true
})

const shown = ref('')
const done = ref(false)
const reduced = useReducedMotion()
let timer: number | null = null

function skip() {
  if (timer) clearInterval(timer)
  timer = null
  shown.value = props.text
  done.value = true
}

onMounted(() => {
  if (!props.play || reduced.value) {
    skip()
    return
  }
  let i = 0
  timer = window.setInterval(() => {
    i++
    shown.value = props.text.slice(0, i)
    if (i >= props.text.length) skip()
  }, props.speed)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})

import { onBeforeUnmount } from 'vue'
</script>

<template>
  <span class="typewriter" title="点击跳过" @click="skip">
    {{ shown }}<span v-if="!done" class="caret">▍</span>
  </span>
</template>

<style scoped>
.typewriter {
  cursor: pointer;
}

.caret {
  color: var(--primary);
  animation: tw-blink 1s steps(2, start) infinite;
}

@keyframes tw-blink {
  50% {
    opacity: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .caret {
    animation: none;
  }
}
</style>