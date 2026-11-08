<script setup lang="ts">
defineProps<{ term: string; tip: string }>()
</script>

<template>
  <span class="term-tip" tabindex="0">
    <span class="term-word">{{ term }}<span class="mark">ⓘ</span></span>
    <span class="bubble" role="tooltip">{{ tip }}</span>
  </span>
</template>

<style scoped>
.term-tip {
  position: relative;
  display: inline-block;
  cursor: help;
  outline: none;
}

.term-word {
  border-bottom: 1px dashed var(--primary);
  color: var(--primary);
}

.mark {
  font-size: 10px;
  margin-left: 2px;
  opacity: 0.85;
}

.bubble {
  position: absolute;
  bottom: calc(100% + 6px);
  left: 50%;
  transform: translateX(-50%);
  width: max-content;
  max-width: 260px;
  background: #202d28;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--text);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.5);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.15s;
  z-index: 50;
}

.term-tip:hover .bubble,
.term-tip:focus .bubble,
.term-tip:focus-within .bubble {
  opacity: 1;
}
</style>