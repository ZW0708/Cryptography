<script setup lang="ts">
withDefaults(defineProps<{ rows?: number }>(), { rows: 3 })
</script>

<template>
  <div class="skeleton" aria-hidden="true">
    <div
      v-for="i in rows"
      :key="i"
      class="sk-row"
      :style="{ width: 92 - ((i - 1) % 3) * 17 + '%' }"
    ></div>
  </div>
</template>