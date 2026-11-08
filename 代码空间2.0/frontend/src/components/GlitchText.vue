<script setup lang="ts">
defineProps<{ text: string }>()
</script>

<template>
  <span class="glitch" :data-text="text">{{ text }}</span>
</template>

<style scoped>
.glitch {
  position: relative;
  display: inline-block;
}

.glitch::before,
.glitch::after {
  content: attr(data-text);
  position: absolute;
  inset: 0;
  opacity: 0;
  pointer-events: none;
}

/* 每 5 秒自动闪一次（前 6% 时间可见），hover 时持续错位 */
.glitch::before {
  color: var(--danger);
  animation: glitchA 5s steps(1) infinite;
}

.glitch::after {
  color: var(--info);
  animation: glitchB 5s steps(1) infinite;
}

.glitch:hover::before,
.glitch:hover::after {
  opacity: 0.85;
  animation-duration: 0.32s;
}

@keyframes glitchA {
  0% { opacity: 0.85; transform: translate(-2px, -1px); clip-path: inset(0 0 78% 0); }
  25% { opacity: 0.85; transform: translate(2px, 1px); clip-path: inset(32% 0 42% 0); }
  50% { opacity: 0.85; transform: translate(-2px, 1px); clip-path: inset(62% 0 8% 0); }
  75% { opacity: 0.85; transform: translate(1px, -1px); clip-path: inset(12% 0 68% 0); }
  100% { opacity: 0; transform: none; clip-path: inset(0 0 0 0); }
}

@keyframes glitchB {
  0% { opacity: 0.85; transform: translate(2px, 1px); clip-path: inset(66% 0 6% 0); }
  25% { opacity: 0.85; transform: translate(-2px, -1px); clip-path: inset(8% 0 74% 0); }
  50% { opacity: 0.85; transform: translate(1px, -1px); clip-path: inset(38% 0 38% 0); }
  75% { opacity: 0.85; transform: translate(-1px, 1px); clip-path: inset(80% 0 4% 0); }
  100% { opacity: 0; transform: none; clip-path: inset(0 0 0 0); }
}

@media (prefers-reduced-motion: reduce) {
  .glitch::before,
  .glitch::after {
    animation: none;
    opacity: 0;
  }
}
</style>