<script setup lang="ts">
import { computed } from 'vue'
import RollingNumber from '@/components/RollingNumber.vue'

const props = withDefaults(
  defineProps<{ value: number; size?: number; label?: string }>(),
  { size: 96, label: '进度' }
)

const radius = computed(() => (props.size - 12) / 2)
const circumference = computed(() => 2 * Math.PI * radius.value)
const offset = computed(
  () => circumference.value * (1 - Math.min(100, Math.max(0, props.value)) / 100)
)
</script>

<template>
  <div
    class="ring"
    :style="{ width: size + 'px', height: size + 'px' }"
    role="img"
    :aria-label="label + ' ' + value + '%'"
  >
    <svg :width="size" :height="size">
      <circle
        class="track"
        :cx="size / 2"
        :cy="size / 2"
        :r="radius"
        :stroke-width="8"
        fill="none"
      />
      <circle
        class="bar"
        :cx="size / 2"
        :cy="size / 2"
        :r="radius"
        :stroke-width="8"
        fill="none"
        stroke-linecap="round"
        :stroke-dasharray="circumference"
        :stroke-dashoffset="offset"
      />
    </svg>
    <div class="center">
      <RollingNumber :value="value" /><span class="pct">%</span>
    </div>
  </div>
</template>

<style scoped>
.ring {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

svg {
  transform: rotate(-90deg);
}

.track {
  stroke: var(--border);
}

.bar {
  stroke: var(--primary);
  filter: drop-shadow(0 0 6px rgba(62, 242, 176, 0.5));
  transition: stroke-dashoffset 0.6s ease;
}

.center {
  position: absolute;
  display: flex;
  align-items: baseline;
  font-family: var(--mono);
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}

.pct {
  font-size: 11px;
  color: var(--text-muted);
  margin-left: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .bar {
    transition: none;
  }
}
</style>