<script setup lang="ts">
import { useRouter } from 'vue-router'

defineProps<{ title: string; text: string }>()
const emit = defineEmits<{ close: [] }>()
const router = useRouter()

function onContinue() {
  emit('close')
  router.push('/levels')
}
</script>

<template>
  <div
    style="position: fixed; inset: 0; background: rgba(0, 0, 0, 0.65); display: flex; align-items: center; justify-content: center; z-index: 1000"
    @click.self="emit('close')"
  >
    <div class="card" style="width: 420px; max-width: 90vw; margin: 0">
      <h3>
        📋 原理总结与现实应用
        <button class="secondary sm" style="float: right" @click="emit('close')">✕</button>
      </h3>
      <h4>{{ title }}</h4>
      <p>{{ text }}</p>
      <p class="muted" style="font-size: 12px">简化演示，实际算法更复杂。概念表述经校对清单核校。</p>
      <div class="row wrap" style="margin-top: 4px">
        <button @click="onContinue">继续闯关</button>
        <button class="secondary" @click="emit('close')">留在本关</button>
      </div>
    </div>
  </div>
</template>